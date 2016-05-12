package tk.jackyliao123.slither.controller;

import tk.jackyliao123.slither.Controller;
import tk.jackyliao123.slither.Slither;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class SpinController extends Controller {

	public double speedMult = 1;

	public SpinController(Slither slither) {
		super(slither);
	}

	public void update() {
		double f = slither.mamu * slither.slitherClient.vfr * slither.player.scang * slither.player.spang;
		slither.angle += f * speedMult;
	}

	public void keyPress(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_EQUALS) {
			speedMult += 0.1;
		}
		else if(e.getKeyCode() == KeyEvent.VK_MINUS) {
			speedMult -= 0.1;
		}
		else if(e.getKeyCode() == KeyEvent.VK_R) {
			speedMult *= -1;
		}
	}

	public void render(Graphics g, double ox, double oy) {
		Rectangle bounds = g.getClipBounds();
	}

	public void drawText(ArrayList<String> text) {
		text.add("Speed: " + speedMult);
	}
}
