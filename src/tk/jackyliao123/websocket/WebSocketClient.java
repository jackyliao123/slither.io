package tk.jackyliao123.websocket;

import tk.jackyliao123.base64.Base64;

import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WebSocketClient {
	public final String host;
	public final String url;

	public HashMap<String, String> requestHeaders = new HashMap<String, String>();
	public HashMap<String, ArrayList<String>> responseHeaders;
	private String secKey;

	private DataInputStream input;
	private DataOutputStream output;

	private SecureRandom random;

	public WebSocketClient(InputStream input, OutputStream output, String host, String url, HashMap<String, String> headers) throws IOException {

		this.host = host;
		this.url = url;

		this.input = new DataInputStream(new BufferedInputStream(input));
		this.output = new DataOutputStream(new BufferedOutputStream(output));

		byte[] b = new byte[16];

		random = new SecureRandom();
		random.nextBytes(b);
		secKey = Base64.encode(b);

		requestHeaders.put("Connection", "upgrade");
		requestHeaders.put("Host", host);
		requestHeaders.put("Upgrade", "websocket");
		requestHeaders.put("Sec-WebSocket-Key", secKey);
		requestHeaders.put("Sec-WebSocket-Version", "13");

		if (headers != null) {
			requestHeaders.putAll(headers);
		}

		connect();

	}

	public void connect() throws IOException {
		sendRequest();
		ArrayList<String> response = waitForResponse();
		if (response.size() < 1) {
			throw new IOException("Response too short");
		}
		String[] responseString = response.get(0).split(" ", 3);
		if (responseString.length != 3)
			throw new IOException("Invalid header received: " + response.get(0));
		if (!responseString[0].startsWith("HTTP/1."))
			throw new IOException("Invalid http version: " + responseString[0]);
		int code = Integer.parseInt(responseString[1]);
		if (code != 101)
			throw new IOException("Invalid response code received: " + code);

		responseHeaders = new HashMap<String, ArrayList<String>>();
		for (int i = 1; i < response.size(); ++i) {
			String[] splitted = response.get(i).split(":", 2);
			if (splitted.length == 2) {
				ArrayList<String> header = responseHeaders.get(splitted[0]);
				if (header == null) {
					header = new ArrayList<String>();
					responseHeaders.put(splitted[0], header);
				}
				header.add(splitted[1].trim());
			}
		}

		ArrayList<String> recvSecKey = responseHeaders.get("Sec-WebSocket-Accept");
		if (recvSecKey == null)
			throw new IOException("No Sec-WebSocket-Accept found");
		if (recvSecKey.size() > 1)
			throw new IOException("Multiple Sec-WebSocket-Accept found");

		String key = recvSecKey.get(0);

		String expectedKey;

		try {
			expectedKey = Base64.encode(MessageDigest.getInstance("SHA-1").digest((secKey + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes()));
		} catch (NoSuchAlgorithmException e) {
			throw new IOException("Unable to generate SHA-1", e);
		}

		if (!key.equals(expectedKey)) {
			throw new IOException("Sec-WebSocket-Accept does not match");
		}

	}

	private void sendRequest() throws IOException {
		StringBuilder requestString = new StringBuilder("GET " + url + " HTTP/1.1\r\n");
		for (Map.Entry<String, String> header : requestHeaders.entrySet()) {
			requestString.append(header.getKey() + ": " + header.getValue() + "\r\n");
		}
		requestString.append("\r\n");
		output.write(requestString.toString().getBytes());
		output.flush();
	}

	private ArrayList<String> waitForResponse() throws IOException {
		ArrayList<String> response = new ArrayList<String>();
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		int c;
		while ((c = input.read()) != -1) {
			if (c == '\n') {
				String s = new String(output.toByteArray(), "UTF-8");
				if (s.length() == 0)
					break;
				response.add(s);
				output.reset();
			} else if (c != '\r') {
				output.write(c);
			}
		}
		if (c == -1) {
			throw new EOFException("Server closed connection");
		}
		return response;
	}

	public Frame read() throws IOException {
		int b;
		b = input.readUnsignedByte();
		boolean fin = b >= 128;
		int rsv = (b >>> 4) & 0x7;
		if (rsv != 0)
			throw new IOException("Non-zero reserved bits: " + rsv);

		int opcode = b & 0xF;
		b = input.readUnsignedByte();
		boolean mask = b >= 128;
		long payloadLength = b & 0x7F;
		if (payloadLength == 126) {
			payloadLength = input.readUnsignedShort();
		} else if (payloadLength == 127) {
			payloadLength = input.readLong();
		}

		if (payloadLength > Integer.MAX_VALUE) {
			throw new RuntimeException("Payload Length exceeds Integer.MAX_VALUE: " + payloadLength);
		}

		byte[] maskingKey = new byte[4];

		if (mask) {
			input.readFully(maskingKey);
		}

		byte[] buffer = new byte[(int) payloadLength];

		int count;
		for (int n = 0; n < payloadLength; n += count) {
			count = input.read(buffer, n, (int) payloadLength - n);
			if (count < 0) {
				throw new EOFException();
			}
			if (mask) {
				for (int i = n; i < n + count; ++i) {
					buffer[i] ^= maskingKey[i % 4];
				}
			}
		}

		return new Frame(fin, opcode, mask, payloadLength, buffer);
	}

	public void write(Frame frame) throws IOException {
		int b;
		b = (frame.fin ? 0x80 : 0x00) | frame.opcode;
		output.write(b);

		byte[] maskingKey = new byte[4];
		if (frame.mask)
			random.nextBytes(maskingKey);

		b = frame.mask ? 0x80 : 0x00;

		if (frame.length > 125) {
			if (frame.length > 65535) {
				output.write(127 | b);
				output.writeLong(frame.length);
			} else {
				output.write(126 | b);
				output.writeShort((int) frame.length);
			}
		} else {
			output.write((int) frame.length | b);
		}

		if (frame.mask) {
			output.write(maskingKey);
		}

		byte[] buffer = frame.data.clone();

		for (int i = 0; i < frame.length; ++i) {
			buffer[i] ^= maskingKey[i % 4];
		}

		output.write(buffer);
		output.flush();
	}

	public void close() {
		try {
			input.close();
		} catch (IOException e) {
		}
		try {
			output.close();
		} catch (IOException e) {
		}
	}

	public static void main(String[] args) throws IOException {
		Socket socket = new Socket("echo.websocket.org", 80);


		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Origin", "http://www.websocket.org/echo.html");
		headers.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.94 Safari/537.36");
		WebSocketClient client = new WebSocketClient(socket.getInputStream(), socket.getOutputStream(), "echo.websocket.org", "/?encoding=sendText", headers);
		byte[] data = "a".getBytes();
		client.write(new Frame(true, 1, true, data.length, data));
		System.out.println(new String(client.read().data));
	}
}
