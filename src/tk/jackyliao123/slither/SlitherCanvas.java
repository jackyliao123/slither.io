package tk.jackyliao123.slither;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

public class SlitherCanvas extends Canvas implements MouseListener, MouseMotionListener, MouseWheelListener {
	public Slither slither;

	public int frameCount = 0;
	public int fps;
	public double targScale = 0.9;

	public double mouseAngle;
	public boolean mouseDown;

	public BufferStrategy bs;
//	public BufferedImage backgroundImage;

	public Color[] colors = new Color[]{
			new Color(192, 128, 255),
			new Color(144, 153, 255),
			new Color(128, 208, 208),
			new Color(128, 255, 128),
			new Color(238, 238, 112),
			new Color(255, 160, 96),
			new Color(255, 144, 144),
			new Color(255, 64, 64),
			new Color(224, 48, 224),
			new Color(255, 255, 255),
			new Color(144, 153, 255),
			new Color(80, 80, 80),
			new Color(255, 192, 80),
			new Color(40, 136, 96),
			new Color(100, 117, 255),
			new Color(120, 134, 255),
			new Color(72, 84, 255),
			new Color(160, 80, 255),
			new Color(255, 224, 64),
			new Color(56, 68, 255),
			new Color(56, 68, 255),
			new Color(78, 35, 192),
			new Color(255, 86, 9),
			new Color(101, 200, 232),
			new Color(128, 132, 144),
			new Color(60, 192, 72),
	};

	int[][] pattern = new int[][]{
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			new int[]{7, 9, 7, 9, 7, 9, 7, 9, 7, 9, 7, 10, 10, 10, 10, 10, 10, 10, 10, 10},
			new int[]{9, 9, 9, 9, 9, 1, 1, 1, 1, 1, 7, 7, 7, 7, 7},
			new int[]{11, 11, 11, 11, 11, 7, 7, 7, 7, 7, 12, 12, 12, 12, 12},
			new int[]{7, 7, 7, 7, 7, 9, 9, 9, 9, 9, 13, 13, 13, 13, 13},
			new int[]{14, 14, 14, 14, 14, 9, 9, 9, 9, 9, 7, 7, 7, 7, 7},
			new int[]{9, 9, 9, 9, 9, 9, 9, 7, 7, 7, 7, 7, 7, 7},
			new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8},
			new int[]{15, 15, 15, 15, 15, 15, 15, 4, 4, 4, 4, 4, 4, 4},
			new int[]{9, 9, 9, 9, 9, 9, 9, 16, 16, 16, 16, 16, 16, 16},
			new int[]{7, 7, 7, 7, 7, 7, 7, 9, 9, 9, 9, 9, 9, 9},
			new int[]{9},
			new int[]{3, 3, 3, 3, 3, 0, 0, 0, 0, 0},
			new int[]{3, 3, 3, 3, 3, 3, 3, 18, 18, 18, 18, 18, 18, 20, 19, 20, 19, 20, 19, 20, 18, 18, 18, 18, 18, 18},
			new int[]{5, 5, 5, 5, 5, 5, 5, 9, 9, 9, 9, 9, 9, 9, 13, 13, 13, 13, 13, 13, 13},
			new int[]{16, 16, 16, 16, 16, 16, 16, 18, 18, 18, 18, 18, 18, 18, 7, 7, 7, 7, 7, 7, 7},
			new int[]{23, 23, 23, 23, 23, 23, 23, 23, 23, 18, 18, 18, 18, 18, 18, 18, 18, 18},
			new int[]{21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 22, 22, 22, 22, 22, 22, 22, 22, 22},
			new int[]{24},
			new int[]{25},
			new int[]{18, 18, 18, 18, 18, 18, 18, 25, 25, 25, 25, 25, 25, 25, 7, 7, 7, 7, 7, 7, 7}

	};

	public SlitherCanvas(Slither slither) {
		this.slither = slither;

//		loadImages();

		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addMouseWheelListener(this);
	}

	public void setupBuffers() {
		this.createBufferStrategy(2);
		bs = this.getBufferStrategy();
	}

//	public void loadImages() {
//		try {
//			backgroundImage = ImageIO.read(getClass().getResourceAsStream("/bg45.jpg"));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}


	public void render() {
		Graphics g = bs.getDrawGraphics();
		g.setColor(new Color(255, 0, 0));
		g.fillRect(0, 0, getWidth(), getHeight());
		try {
			synchronized (slither) {
				slither.gsc += (targScale - slither.gsc) / 5;
				slither.slitherClient.update();
				redraw(g);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		g.dispose();
		bs.show();
		Toolkit.getDefaultToolkit().sync();
		++frameCount;
	}

	public int getColorIndex(int skin) {
		if (skin >= pattern.length) {
			skin %= 9;
		}
		if (pattern[skin] == null)
			return skin;
		return pattern[skin][0];
	}

	public void redraw(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

		double scale = slither.gsc;

		Snake player = slither.player;
		double ox = 0;
		double oy = 0;
		if (player != null) {
			ox = player.x + player.fx - getWidth() / scale / 2;
			oy = player.y + player.fy - getHeight() / scale / 2;
		}

		for (Snake snake : slither.snakes.values()) {
			for (Snake.BodySection section : snake.snakeBody) {
				double px = (section.x + section.fx - ox) * scale;
				double py = (section.y + section.fy - oy) * scale;
				if (px >= 0 && px <= getWidth() && py >= 0 && py <= getHeight()) {
					snake.iiv = true;
				}
			}
		}


//			int bgWidth = (int)(backgroundImage.getWidth() * scale);
//			int bgHeight = (int)(backgroundImage.getHeight() * scale);
//			int clampedWidth = Math.min(bgWidth, getWidth());
//			int clampedHeight = Math.min(bgHeight, getHeight());
//			BufferedImage scaled = new BufferedImage(clampedWidth, clampedHeight, BufferedImage.TYPE_INT_ARGB);
//			Graphics2D bgGraphics = (Graphics2D) scaled.getGraphics();
//			bgGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
//			bgGraphics.drawImage(backgroundImage, 0, 0, bgWidth, bgHeight, null);
//
////			for(int i = 0; i <= getWidth() / bgWidth + 1; ++i) {
////				for(int j = 0; j <= getHeight() / bgHeight + 1; ++j) {
////					g.drawImage(scaled, i * bgWidth, j * bgHeight, null);
////				}
////			}
//
//			TexturePaint texturePaint = new TexturePaint(scaled, new Rectangle(0, 0, clampedWidth, clampedHeight));
//			g2d.setPaint(texturePaint);
//			g2d.fillRect(0, 0, getWidth(), getHeight());
//			g2d.setPaint(null);
//
		g2d.setColor(new Color(35, 35, 35));
		double radius = slither.gameRadius * 0.98;
		g2d.fill(new Ellipse2D.Double((slither.gameRadius - ox - radius) * scale, (slither.gameRadius - oy - radius) * scale, radius * scale * 2, radius * scale * 2));

		g2d.setColor(new Color(0, 255, 0, 16));
		for (Point sector : slither.sectors) {
			g2d.fill(new Rectangle2D.Double((sector.x * slither.sectorSize - ox + 4) * scale, (sector.y * slither.sectorSize - oy + 4) * scale, (slither.sectorSize - 8) * scale, (slither.sectorSize - 8) * scale));
		}
		for (Food food : slither.food.values()) {
			g2d.setColor(colors[food.color]);
			g2d.fill(new Ellipse2D.Double((food.x - ox - food.size) * scale, (food.y - oy - food.size) * scale, food.size * scale * 2, food.size * scale * 2));
		}
		for (Snake snake : slither.snakes.values()) {

			if (snake.iiv) {

				g2d.setColor(colors[getColorIndex(snake.skin)]);

				double snakeSize = 29 * snake.sc;

				Path2D.Double path = new Path2D.Double();

				double px = snake.x + snake.fx;
				double py = snake.y + snake.fy;
				double lpx;
				double lpy;

				double ax = 0;
				double ay = 0;
				double lax;
				double lay;

				boolean segments = false;

				path.moveTo((px - ox) * scale, (py - oy) * scale);

				double z = snake.cfl;


				for (int i = snake.snakeBody.size() - 1; i >= 0; --i) {
					Snake.BodySection loc = snake.snakeBody.get(i);

//						System.out.println(loc.x + ", " + loc.y);

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
							lpx += (lax - lpx) * f;
							lpy += (lay - lpy) * f;
							ax += (lax - ax) * f;
							ay += (lay - ay) * f;
						}

						if (segments) {
							--z;
						} else {
							z -= snake.chl + snake.fchl;
						}

						if (segments) {
							path.quadTo((lpx - ox) * scale, (lpy - oy) * scale, (ax - ox) * scale, (ay - oy) * scale);
						} else {
							path.lineTo((ax - ox) * scale, (ay - oy) * scale);
							segments = true;
						}
					}
				}

				g2d.setStroke(new BasicStroke((int) (snakeSize * scale), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				g2d.draw(path);

				Font prevFont = g2d.getFont();
				g2d.setFont(prevFont.deriveFont((float) (prevFont.getSize2D() * scale * 3)));
				FontMetrics fm = g2d.getFontMetrics();

				if (snake != slither.player) {
					String str = snake.name;
					int sw = fm.stringWidth(str);
					int sh = fm.getHeight();
					int x = (int) ((snake.x + snake.fx - ox) * scale) - sw / 2;
					int y = (int) ((snake.y + snake.fy - oy + 100) * scale) + sh / 2;
					g2d.setColor(Color.black);
					g2d.drawString(str, x + 1, y + 1);
					g2d.drawString(str, x + 1, y - 1);
					g2d.drawString(str, x - 1, y - 1);
					g2d.drawString(str, x - 1, y + 1);
					g2d.setColor(colors[getColorIndex(snake.skin)]);
					g2d.drawString(str, x, y);
				}

				g2d.setFont(prevFont);

			}
		}

		if (slither.leaderboard != null) {

			FontMetrics fm = g2d.getFontMetrics();

			int width = getWidth();
			g2d.setColor(new Color(32, 32, 32, 128));
			g2d.fillRect(width - 260, 4, 256, (fm.getHeight() + 4) * slither.leaderboard.size() + fm.getHeight() / 2 + 2);
			for (int i = 0; i < slither.leaderboard.size(); ++i) {
				PlayerScore score = slither.leaderboard.get(i);
				int size = (int) (15 * (slither.fpsls[score.length] + score.fam / slither.fmlts[score.length] - 1) - 5);

				Font prevFont = g2d.getFont();

				Color c = colors[getColorIndex(score.skin)];
				if (i + 1 == slither.leaderboardRank) {
					g2d.setColor(c.brighter());
					g2d.setFont(prevFont.deriveFont(Font.BOLD));
				} else {
					g2d.setColor(c.darker());
				}

				int y = i * (fm.getHeight() + 4) + 15 + fm.getHeight() / 2;

				String number = "#" + (i + 1);
				g2d.drawString(number, width - (230 + fm.stringWidth(number)), y);

				g2d.drawString(score.name, width - (214), y);

				String ssize = Integer.toString(size);
				g2d.drawString(ssize, width - (54 + fm.stringWidth(ssize)) / 2, y);

				g2d.setFont(prevFont);
			}
		}

		ArrayList<String> text = new ArrayList<String>();
		text.add("FPS: " + fps);
		text.add("Ping: " + slither.messageProcessor.ping);
		if (slither.player != null) {
			text.add("Length: " + Math.floor(150 * (slither.fpsls[slither.player.length] + slither.player.fam / slither.fmlts[slither.player.length] - 1) - 50) / 10.0);
			if (slither.leaderboard != null) {
				text.add("Rank: " + slither.playerRank + "/" + slither.numPlayers);
			}
		}
		text.add("Controller: " + slither.slitherClient.controller.getClass().getSimpleName());

		if (slither.slitherClient.controller != null) {
			slither.slitherClient.controller.render(g2d, ox, oy);
			slither.slitherClient.controller.drawText(text);
		}

		FontMetrics fm = g2d.getFontMetrics();

		g2d.setColor(Color.white);

		for (int i = 0; i < text.size(); ++i) {
			g2d.drawString(text.get(i), 10, 10 + fm.getHeight() * i + fm.getHeight() / 2);
		}

	}

	@Override
	public void mouseDragged(MouseEvent mouseEvent) {
		mouseAngle = Math.atan2(mouseEvent.getY() - getHeight() / 2, mouseEvent.getX() - getWidth() / 2);
	}

	@Override
	public void mouseMoved(MouseEvent mouseEvent) {
		mouseAngle = Math.atan2(mouseEvent.getY() - getHeight() / 2, mouseEvent.getX() - getWidth() / 2);
	}

	@Override
	public void mouseClicked(MouseEvent mouseEvent) {
	}

	@Override
	public void mousePressed(MouseEvent mouseEvent) {
		mouseDown = true;
	}

	@Override
	public void mouseReleased(MouseEvent mouseEvent) {
		mouseDown = false;
	}

	@Override
	public void mouseEntered(MouseEvent mouseEvent) {
	}

	@Override
	public void mouseExited(MouseEvent mouseEvent) {
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		targScale /= Math.pow(1.2, e.getWheelRotation());
		if (targScale < 0.01) {
			targScale = 0.01;
		}
		if (targScale > 10) {
			targScale = 10;
		}
	}
}
