package tk.jackyliao123.slither.controller;

import tk.jackyliao123.slither.Controller;
import tk.jackyliao123.slither.Food;
import tk.jackyliao123.slither.Slither;
import tk.jackyliao123.slither.Snake;

public class CollectFoodController extends Controller {
	public CollectFoodController(Slither slither) {
		super(slither);
	}

	@Override
	public void update() {
		Snake player = slither.player;
		if(player != null) {
			double x = player.x + player.fx;
			double y = player.y + player.fy;
			Food nearestFood = null;
			double distSq = Double.POSITIVE_INFINITY;
			for(Food food : slither.food.values()) {
				double deltaX = food.x - x;
				double deltaY = food.y - y;
				double dist = deltaX * deltaX + deltaY * deltaY;
				if(dist > 10000 && dist < distSq) {
					distSq = dist;
					nearestFood = food;
				}
			}
			if(nearestFood != null) {
				double deltaX = nearestFood.x - x;
				double deltaY = nearestFood.y - y;
				slither.angle = Math.atan2(deltaY, deltaX);
			}
		}
	}
}
