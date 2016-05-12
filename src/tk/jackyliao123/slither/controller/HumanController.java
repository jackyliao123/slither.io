package tk.jackyliao123.slither.controller;

import tk.jackyliao123.slither.Controller;
import tk.jackyliao123.slither.Slither;
import tk.jackyliao123.slither.SlitherUI;

public class HumanController extends Controller {

	public SlitherUI slitherUI;

	public HumanController(Slither slither, SlitherUI slitherUI) {
		super(slither);
		this.slitherUI = slitherUI;
	}

	public void update() {
		slither.boost = slitherUI.slitherCanvas.mouseDown;
		slither.angle = slitherUI.slitherCanvas.mouseAngle;
	}
}
