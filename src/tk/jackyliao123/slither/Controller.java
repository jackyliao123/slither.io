package tk.jackyliao123.slither;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public abstract class Controller {
	public Slither slither;
	public Controller(Slither slither) {
		this.slither = slither;
	}

	public abstract void update();

	public void keyPress(KeyEvent e) {
	}

	public void render(Graphics g, double ox, double oy) {
	}

	public void drawText(ArrayList<String> text) {
	}
}
