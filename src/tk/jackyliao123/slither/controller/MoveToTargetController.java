package tk.jackyliao123.slither.controller;

import tk.jackyliao123.slither.Controller;
import tk.jackyliao123.slither.Slither;
import tk.jackyliao123.slither.Snake;

import java.awt.*;
import java.awt.geom.Ellipse2D;

public class MoveToTargetController extends Controller {
	public double targetX = 0;
	public double targetY = 0;

	public Snake targetPlayer;

	public MoveToTargetController(Slither slither) {
		super(slither);
	}
	public MoveToTargetController(Slither slither, double x, double y) {
		super(slither);
		targetX = x;
		targetY = y;
	}
	public MoveToTargetController(Slither slither, Snake targetSnake) {
		super(slither);
		targetPlayer = targetSnake;
	}

	public void render(Graphics g, double ox, double oy) {
		if (targetPlayer != null) {
			targetX = targetPlayer.x + targetPlayer.fx;
			targetY = targetPlayer.y + targetPlayer.fy;
		}
		Graphics2D g2d = (Graphics2D)g;
		g2d.fill(new Ellipse2D.Double((targetX - ox) * slither.gsc, (targetY - oy) * slither.gsc, 10, 10));
	}

	@Override
	public void update() {
		if (targetPlayer != null) {
			targetX = targetPlayer.x + targetPlayer.fx;
			targetY = targetPlayer.y + targetPlayer.fy;
		}
		slither.angle = Math.atan2(targetY - slither.player.y, targetX - slither.player.x);
	}
}
