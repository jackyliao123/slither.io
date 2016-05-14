package tk.jackyliao123.websocket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayDeque;

public class WebSocketClientProcessor {
	public final WebSocketClient client;
	private ReadThread readThread;
	private WriteThread writeThread;

	private boolean isOpen;
	private boolean closing;

	public WebSocketListener listener;

	private final ArrayDeque<Frame> dataQueue = new ArrayDeque<Frame>();
	private final ArrayDeque<Frame> controlQueue = new ArrayDeque<Frame>();
	private final boolean reassembleFragments;

	private int opcode = -1;
	private ByteArrayOutputStream reassembly = new ByteArrayOutputStream();

	public boolean useMask = false;

	private final ArrayDeque<Long> pingQueue = new ArrayDeque<Long>();

	private final Object threadLock = new Object();

	public WebSocketClientProcessor(WebSocketClient client, WebSocketListener listener, boolean reassembleFragments) {
		if (client == null) {
			throw new NullPointerException();
		}
		this.client = client;
		this.listener = listener;
		readThread = new ReadThread();
		writeThread = new WriteThread();
		this.reassembleFragments = reassembleFragments;
		isOpen = true;
	}

	public void start() {
		readThread.start();
		writeThread.start();
	}

	public class ReadThread extends Thread {
		public ReadThread() {
			setName("WebSocket ReadThread");
		}

		public void run() {
			try {
				while (isOpen) {
					Frame f = client.read();
					switch (f.opcode) {
						case 0:
							if(reassembleFragments) {
								if(f.fin) {
									switch(opcode) {
										case 1:
											String s = new String(reassembly.toByteArray(), "UTF-8");
											try {
												listener.onText(s, false);
											} catch(RuntimeException e) {
											}
											break;
										case 2:
											try {
												listener.onBinary(reassembly.toByteArray(), false);
											} catch(RuntimeException e) {
											}
											break;
									}
									opcode = -1;
									reassembly.reset();
								} else {
									reassembly.write(f.data);
								}
							} else {
								try {
									listener.onFragment(f.data, f.fin);
								} catch(RuntimeException e) {
								}
							}
							break;
						case 1: {
							if (reassembleFragments) {
								if (f.fin) {
									String s = new String(f.data, "UTF-8");
									try {
										listener.onText(s, false);
									} catch (RuntimeException e) {
									}
								} else {
									if(opcode != -1)
										throw new IOException("Creating new fragment while previous fragments not finished");
									opcode = 1;
									reassembly.write(f.data);
								}
							} else {
								String s = new String(f.data, "UTF-8");
								try {
									listener.onText(s, !f.fin);
								} catch (RuntimeException e) {
								}
							}
							break;
						}
						case 2:
							if (reassembleFragments) {
								if (f.fin) {
									try {
										listener.onBinary(f.data, false);
									} catch (RuntimeException e) {
									}
								} else {
									if(opcode != -1)
										throw new IOException("Creating new fragment while previous fragments not finished");
									opcode = 2;
									reassembly.write(f.data);
								}
							} else {
								try {
									listener.onBinary(f.data, !f.fin);
								} catch (RuntimeException e) {
								}
							}
							break;
						case 8:
							closing = true;
							close();
							break;
						case 9:
							listener.onPing(f.data);
							System.out.println("ping received");
							sendPong(f.data);
							break;
						case 10: {
							long td = -1;
							synchronized (pingQueue) {
								if (pingQueue.size() > 0) {
									long l = pingQueue.removeFirst();
									td = System.currentTimeMillis() - l;
								}
							}
							listener.onPong(f.data, td);
							break;
						}
					}
				}
			} catch (Exception e) {
				close();
			}
		}
	}

	public void close() {
		isOpen = false;
		listener.onClose();
		client.close();
	}

	public void sendText(String s) {
		byte[] b = s.getBytes(Charset.forName("UTF-8"));
		Frame frame = new Frame(true, 1, useMask, b.length, b);
		synchronized(threadLock) {
			dataQueue.add(frame);
		}
		writeThread.interrupt();
	}

	public void sendBinary(byte[] b) {
		Frame frame = new Frame(true, 2, useMask, b.length, b);
		synchronized (threadLock) {
			dataQueue.add(frame);
		}
		writeThread.interrupt();
	}

	public void sendClose(int status, String reason) {
		closing = true;
		byte[] b;
		if(status != -1) {
			byte[] str = reason.getBytes(Charset.forName("UTF-8"));
			b = new byte[str.length + 2];
			System.arraycopy(str, 0, b, 2, str.length);
			b[0] = (byte)(status >>> 8);
			b[1] = (byte)(status & 0xFF);
		} else {
			b = new byte[0];
		}
		Frame frame = new Frame(true, 8, useMask, b.length, b);
		synchronized (threadLock) {
			controlQueue.add(frame);
		}
		writeThread.interrupt();
	}

	public void sendPing(byte[] data) {
		Frame frame = new Frame(true, 9, useMask, data.length, data);
		synchronized (pingQueue) {
			pingQueue.add(System.currentTimeMillis());
		}
		synchronized (threadLock) {
			controlQueue.addLast(frame);
		}
		writeThread.interrupt();
	}

	private void sendPong(byte[] data) {
		Frame frame = new Frame(true, 10, useMask, data.length, data);
		synchronized (threadLock) {
			controlQueue.addLast(frame);
		}
		writeThread.interrupt();
	}

	public class WriteThread extends Thread {
		public WriteThread() {
			setName("WebSocket WriteThread");
		}

		public void run() {
			try {
				while (isOpen) {
					synchronized(threadLock) {
						while (controlQueue.size() > 0 || dataQueue.size() > 0) {
							if(controlQueue.size() > 0) {
								Frame f = controlQueue.removeFirst();
								client.write(f);
								if(f.opcode == 8) {
									close();
								}
							} else if(dataQueue.size() > 0) {
								Frame f = dataQueue.removeFirst();
								client.write(f);
							}
						}
					}
					try {
						Thread.sleep(Long.MAX_VALUE);
					} catch(InterruptedException e) {};
				}
			} catch(Exception e) {
				close();
			}
		}
	}
}
