package tk.jackyliao123.slither.controller;

import tk.jackyliao123.slither.Controller;
import tk.jackyliao123.slither.Food;
import tk.jackyliao123.slither.Slither;
import tk.jackyliao123.slither.Snake;

import javax.sound.sampled.Line;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class CollectFoodController extends Controller {

	public ArrayList<Line2D.Double> deltas = new ArrayList<Line2D.Double>();
	public double dx;
	public double dy;

	public CollectFoodController(Slither slither) {
		super(slither);
	}

	@Override
	public void update() {
		Snake player = slither.player;
		if(player != null) {
			double x = player.x + player.fx;
			double y = player.y + player.fy;
			double distSq = Double.POSITIVE_INFINITY;

			dx = 0;
			dy = 0;

			deltas.clear();

			for(Food food : slither.food.values()) {
				double deltaX = food.x - x;
				double deltaY = food.y - y;
				double dist = deltaX * deltaX + deltaY * deltaY;
				double dis = Math.sqrt(dist);
				if(dist > 7500 && dist < distSq) {
					double dxC = deltaX / Math.pow(1.3, dis / 10) * Math.pow(1.3, food.size) * 10;
					double dyC = deltaY / Math.pow(1.3, dis / 10) * Math.pow(1.3, food.size) * 10;

					deltas.add(new Line2D.Double(food.x, food.y, dxC, dyC));

					dx -= dxC;
					dy -= dyC;
				}
			}
			slither.angle = Math.atan2(-dy, -dx);
		}
	}

	public void render(Graphics g, double ox, double oy) {
		Graphics2D g2d = (Graphics2D)g;
		g2d.setStroke(new BasicStroke(2));
		g2d.setColor(Color.white);
		for(Line2D.Double p : deltas) {
			g2d.draw(new Line2D.Double((p.x1 - ox) * slither.gsc, (p.y1 - oy) * slither.gsc, (p.x1 + p.x2 - ox) * slither.gsc, (p.y1 + p.y2 - oy) * slither.gsc));
		}

		double px = slither.player.x + slither.player.fx;
		double py = slither.player.y + slither.player.fy;

		g2d.setColor(Color.blue);

		g2d.draw(new Line2D.Double((px - ox) * slither.gsc, (py - oy) * slither.gsc, (px - ox - dx) * slither.gsc, (py - oy - dy) * slither.gsc));
	}

}
