package tk.jackyliao123.slither;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class SlitherServerList {
	public static class Server {
		public String ip;
		public int port;
		public int ac;
		public int clu;

		public Server(String ip, int port, int ac, int clu) {
			this.ip = ip;
			this.port = port;
			this.ac = ac;
			this.clu = clu;
		}
	}
	public static ArrayList<Server> downloadServerList() throws IOException {
		URL url = new URL("http://slither.io/i33628.txt");
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.75 Safari/537.36");
		connection.connect();
		InputStream input = connection.getInputStream();
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] buffer = new byte[4096];
		int read;
		while((read = input.read(buffer, 0, buffer.length)) != -1) {
			output.write(buffer, 0, read);
		}
		input.close();

		byte[] b = output.toByteArray();
		byte[] newBytes = new byte[(b.length - 1) / 2];

		for(int i = 1; i < b.length; ++i) {
			b[i] = (byte)((((b[i] & 0xFF) - 97 - (i - 1) * 7) % 26 + 26) % 26);
		}

		for(int i = 0; i < newBytes.length; ++i) {
			newBytes[i] = (byte)((b[i * 2 + 1] & 0xFF) * 16 + b[i * 2 + 2]);
		}

		ArrayList<Server> servers = new ArrayList<Server>();

		ByteBuffer bytes = ByteBuffer.wrap(newBytes);

		while(bytes.hasRemaining()) {

			String ip = "";
			int port = 0;
			int ac = 0;
			int clu;

			for(int i = 0; i < 4; ++i) {
				ip += (bytes.get() & 0xFF) + (i == 3 ? "" : ".");
			}
			for(int i = 0; i < 3; ++i) {
				port = port * 256 + (bytes.get() & 0xFF);
			}
			for(int i = 0; i < 3; ++i) {
				ac = ac * 256 + (bytes.get() & 0xFF);
			}
			clu = bytes.get() & 0xFF;

			servers.add(new Server(ip, port, ac, clu));

		}

		return servers;
	}

	public static void main(String[] args) throws Throwable {
		ArrayList<Server> servers = downloadServerList();
		for(Server server : servers) {
			System.out.println(server.ip + ":" + server.port);
		}
	}

}
