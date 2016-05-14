package tk.jackyliao123.websocket;

public class WebSocketListener {
	public void onFragment(byte[] b, boolean isFinal) {}
	public void onText(String t) {}
	public void onText(String t, boolean fragment) {
		onText(t);
	}
	public void onBinary(byte[] b) {}
	public void onBinary(byte[] b, boolean fragment) {
		onBinary(b);
	}
	public void onClose() {}
	public void onPing(byte[] data) {}
	public void onPong(byte[] data, long latency) {}
}
