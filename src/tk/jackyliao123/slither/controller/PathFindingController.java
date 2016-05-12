package tk.jackyliao123.slither.controller;

import tk.jackyliao123.slither.Controller;
import tk.jackyliao123.slither.Slither;
import tk.jackyliao123.slither.Snake;

import java.awt.*;
import java.awt.geom.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;


// TODO INEFFICIENT
public class PathFindingController extends Controller {

	public Controller subController;

	public double mergeThresholdSq = 2500;
	public double dangerMultiplier = 100;

	public double pathFindThreashold = 50;

	public long pathFindingTime;
	public double distance;
	public double dangerLevel;
	public int snakeCount;

	public double gridSize = 100;

	public double targetX = 20000;
	public double targetY = 20000;

	public PathFindNode path;

	public Snake player;

	public PathFindingController(Slither slither) {
		super(slither);
	}

	public PathFindingController(Slither slither, Snake player) {
		super(slither);
		this.player = player;
	}

//	public static class QuadTree {
//		public double x1;
//		public double x2;
//		public double y1;
//		public double y2;
//		public QuadTree[] subTree = new QuadTree[4];
//		public ArrayList<Snake> toPropagate = new ArrayList<Snake>();
//		public void propagate() {
//
//		}
//	}

	public static class SnakePoints {
		public double minX;
		public double minY;
		public double maxX;
		public double maxY;
		public Point2D.Double head;
		public Point2D.Double tail;
		public ArrayList<Point2D.Double> points = new ArrayList<Point2D.Double>();
	}

	public static class PathFindNode implements Comparable<PathFindNode> {
		public double x;
		public double y;
		public double length;
		public PathFindNode previousNode;
		public double dist;

		public PathFindNode(double x, double y, double length, PathFindNode previous) {
			this.x = x;
			this.y = y;
			this.length = length;
			this.previousNode = previous;
		}

		@Override
		public int compareTo(PathFindNode pathFindNode) {
			return Double.compare(length, pathFindNode.length);
		}
	}

	public static double hypSq(double x, double y) {
		return x * x + y * y;
	}

	public double distance(double x, double y, Snake snake) {
		double snakeSize = 29 * snake.sc;
		double minDist = Double.POSITIVE_INFINITY;
		if (snake.customData instanceof SnakePoints) {

			SnakePoints points = (SnakePoints) snake.customData;
			minDist = Math.min(minDist, hypSq(x - points.head.x, y - points.head.y));
			for(Point2D.Double p : points.points) {
				minDist = Math.min(minDist, hypSq(x - p.x, y - p.y));
			}
			if(points.tail != null) {
				minDist = Math.min(minDist, hypSq(x - points.tail.x, y - points.tail.y));
			}
		}
		return Math.max(Math.sqrt(minDist) - snakeSize / 2, 0.1);
	}

	public void drawText(ArrayList<String> text) {
		text.add("Distance: " + distance);
		text.add("Danger Level: " + dangerLevel);
		text.add("Time: " + pathFindingTime + "ms");
		text.add("Nearby Snakes: " + snakeCount);
	}

	public void updatePointsAndBounds() {

		snakeCount = 0;

		for (Snake s : slither.snakes.values()) {

			++snakeCount;

			SnakePoints points = new SnakePoints();

			double snakeSize = 29 * s.sc;

			double minX = Integer.MAX_VALUE;
			double minY = Integer.MAX_VALUE;
			double maxX = Integer.MIN_VALUE;
			double maxY = Integer.MIN_VALUE;

			ArrayList<Snake.BodySection> bodySections = s.snakeBody;

			double px = s.x + s.fx;
			double py = s.y + s.fy;
			double lpx;
			double lpy;

			boolean segments = false;

			double ax = 0;
			double ay = 0;
			double lax;
			double lay;

			double lastMergeX = Double.NEGATIVE_INFINITY;
			double lastMergeY = Double.NEGATIVE_INFINITY;

			minX = Math.min(px, minX);
			minY = Math.min(py, minY);
			maxX = Math.max(px, maxX);
			maxY = Math.max(py, maxY);

			points.head = new Point2D.Double(px, py);

			double z = s.cfl;

			for (int i = bodySections.size() - 1; i >= 0; --i) {
				Snake.BodySection loc = bodySections.get(i);

				lpx = px;
				lpy = py;
				px = loc.x;
				py = loc.y;

				if (z > 0) {

					px += loc.fx;
					py += loc.fy;

					lax = ax;
					lay = ay;

					ax = (px + lpx) / 2.0;
					ay = (py + lpy) / 2.0;

					if (!segments) {
						lax = ax;
						lay = ay;
					}
					if (z < 1) {
						double f = 1 - z;
						ax += (lax - ax) * f;
						ay += (lay - ay) * f;
					}

					if (segments) {
						--z;
					} else {
						z -= s.chl + s.fchl;
						segments = true;
					}

					minX = Math.min(ax, minX);
					minY = Math.min(ay, minY);
					maxX = Math.max(ax, maxX);
					maxY = Math.max(ay, maxY);

					if (z > 0) {
						if(lastMergeX == Double.NEGATIVE_INFINITY) {
							lastMergeX = ax;
							lastMergeY = ay;
						}

						if(hypSq(ax - lastMergeX, ay - lastMergeY) > mergeThresholdSq) {
							points.points.add(new Point2D.Double(ax, ay));
							lastMergeX = ax;
							lastMergeY = ay;
						}
					} else {
						points.tail = new Point2D.Double(ax, ay);
					}

				}
			}
			points.minX = minX - snakeSize / 2;
			points.minY = minY - snakeSize / 2;
			points.maxX = maxX + snakeSize / 2;
			points.maxY = maxY + snakeSize / 2;

			s.customData = points;

		}


	}

	public double dist(double x, double y) {
		double minDist = Double.POSITIVE_INFINITY;
		for (Snake s : slither.snakes.values()) {
			if (s != slither.player) {
				minDist = Math.min(minDist, distance(x, y, s));
			}
		}
		return minDist;
	}

	public int snapGrid(double c) {
		return (int) (c / gridSize);
	}
	public double expandGrid(int c) {
		return c * gridSize;
	}

	@Override
	public void update() {

		long start = System.currentTimeMillis();

		updatePointsAndBounds();

		Snake player = slither.player;
		if (player != null) {

			double snakeSize = 29 * player.sc;

			double x = player.x + player.fx;
			double y = player.y + player.fy;

			distance = Math.max(dist(x, y) - snakeSize / 2, 0.1);
			double eDistanceSq = hypSq(x - targetX, y - targetY);
			double eDistance = Math.sqrt(eDistanceSq);

			dangerLevel = dangerMultiplier / distance;

//			while(hypSq(x - targetX, y - targetY) < 4000000) {
//				targetX = Math.random() * 40000;
//				targetY = Math.random() * 40000;
//			}
			targetX = 20000;
			targetY = 20000;

			if(this.player != null) {
				targetX = this.player.x + this.player.fx;
				targetY = this.player.y + this.player.fy;
			}

			double tempTargetX = (targetX - x) / eDistance * 100 * snakeSize + x;
			double tempTargetY = (targetY - y) / eDistance * 100 * snakeSize + y;

			HashMap<Point, PathFindNode> visited = new HashMap<Point, PathFindNode>();

			PriorityQueue<PathFindNode> queue = new PriorityQueue<PathFindNode>();
			PathFindNode pfn = new PathFindNode(x, y, 0, null);
			pfn.dist = distance;
			queue.add(pfn);
			while(!queue.isEmpty()) {
				PathFindNode node = queue.poll();
				Point pg = new Point(snapGrid(node.x), snapGrid(node.y));

				if(hypSq(node.x - tempTargetX, node.y - tempTargetY) < eDistanceSq / 100) {
					path = node;
					break;
				}

//				if(!slither.sectors.contains(new Point((int)(expandGrid(p.x) / slither.sectorSize), (int)(expandGrid(p.y) / slither.sectorSize)))) {
//					path = node;
//					break;
//				}

				if(node.dist < pathFindThreashold && (node.previousNode != null && node.dist < node.previousNode.dist)) {
					continue;
				}

				if(!visited.containsKey(pg)) {
					visited.put(pg, node);
				} else {
//					PathFindNode prev = visited.get(p);
//					if(prev.length > node.length) {
//						visited.put(p, node);
//					} else {
//						continue;
//					}
					continue;
				}

				double moveDist = Math.max(gridSize / 2, Math.min(node.dist / 2, Math.sqrt(hypSq(tempTargetX - node.x, tempTargetY - node.y)) + 100));

				int sections = 32;

				for(int i = 0; i < sections; ++i) {
					double dx = Math.cos(Math.PI * 2 * i / sections);
					double dy = Math.sin(Math.PI * 2 * i / sections);
					queue.add(nextNode(moveDist * dx, moveDist * dy, node, tempTargetX, tempTargetY, snakeSize));
				}

			}

			if(queue.isEmpty()) {
				path = null;
			}

			pathFindingTime = System.currentTimeMillis() - start;

			if(path != null) {

				PathFindNode lastNode = path;

				PathFindNode p = path;
				while (p.previousNode != null) {
					lastNode = p;
					p = p.previousNode;
				}

				slither.angle = Math.atan2(lastNode.y - y, lastNode.x - x);
			}

		}


	}

	public PathFindNode nextNode(double dx, double dy, PathFindNode node, double targetX, double targetY, double snakeSize) {

		double dist = Math.sqrt(hypSq(node.x + dx - targetX, node.y + dy - targetY));

		PathFindNode n = new PathFindNode(node.x + dx, node.y + dy, node.length + Math.sqrt(hypSq(dx, dy)) + dist, node);

		double nodeDist = dist(node.x + dx, node.y + dy);
		n.dist = nodeDist;
//		if(nodeDist != Double.POSITIVE_INFINITY) {
//			n.length -= nodeDist / 10000;
//		}

		return n;

	}

	public void render(Graphics g, double ox, double oy) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setStroke(new BasicStroke(5));
		for (Snake s : slither.snakes.values()) {
			if (s.customData instanceof SnakePoints) {

				SnakePoints points = (SnakePoints)s.customData;

				g.setColor(new Color(255, 255, 0, 127));
				g2d.fill(new Ellipse2D.Double((points.head.x - ox) * slither.gsc - 5, (points.head.y - oy) * slither.gsc - 5, 10, 10));

				g.setColor(new Color(255, 255, 255, 127));

				for (Point2D.Double p : points.points) {
					g2d.fill(new Ellipse2D.Double((p.x - ox) * slither.gsc - 5, (p.y - oy) * slither.gsc - 5, 10, 10));
				}

				if(points.tail != null) {
					g.setColor(new Color(0, 255, 255, 127));
					g2d.fill(new Ellipse2D.Double((points.tail.x - ox) * slither.gsc - 5, (points.tail.y - oy) * slither.gsc - 5, 10, 10));
				}


				g.setColor(new Color(255, 255, 255, 127));

				g2d.draw(new Rectangle2D.Double((points.minX - ox) * slither.gsc, (points.minY - oy) * slither.gsc, (points.maxX - points.minX) * slither.gsc, (points.maxY - points.minY) * slither.gsc));

				double x = (s.x + s.fx - ox) * slither.gsc;
				double y = (s.y + s.fy - oy) * slither.gsc;
				g2d.draw(new Line2D.Double(x, y, x + Math.cos(s.eang) * 100, y + Math.sin(s.eang) * 100));

				PathFindNode node = path;

				if(node != null) {

					Path2D.Double p2d = new Path2D.Double();

					p2d.moveTo((node.x - ox) * slither.gsc, (node.y - oy) * slither.gsc);

					while(node.previousNode != null) {
						node = node.previousNode;
						p2d.lineTo((node.x - ox) * slither.gsc, (node.y - oy) * slither.gsc);
					}

					g2d.setColor(new Color(255, 128, 64, 128));
					g2d.draw(p2d);
				}
			}
		}
	}
}
