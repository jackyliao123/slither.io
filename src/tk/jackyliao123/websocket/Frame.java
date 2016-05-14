package tk.jackyliao123.websocket;

public class Frame {
	public boolean fin;
	public int opcode;
	public boolean mask;
	public long length;
	public byte[] data;

	public Frame(boolean fin, int opcode, boolean mask, long length, byte[] data) {
		this.fin = fin;
		this.opcode = opcode;
		this.mask = mask;
		this.length = length;
		this.data = data;
	}

}
