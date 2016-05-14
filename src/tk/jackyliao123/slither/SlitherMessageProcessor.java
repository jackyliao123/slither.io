package tk.jackyliao123.slither;

import tk.jackyliao123.websocket.WebSocketClient;
import tk.jackyliao123.websocket.WebSocketClientProcessor;
import tk.jackyliao123.websocket.WebSocketListener;

import java.awt.*;
import java.awt.geom.Point2D;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SlitherMessageProcessor extends WebSocketListener {

	public Slither slither;

	public WebSocketClientProcessor processor;

	public String username;
	public int skin;

	private ByteBuffer buffer;

	public long lastTime;
	public long currentTime;

	public boolean lastBoost;
	public byte lastSentAngle = -1;

	public boolean connected;

	public long lastPing;
	public long ping;

	public SlitherMessageProcessor(Slither slither, String serverHost, int serverPort, String username, int skin) {
		this.slither = slither;
		this.username = username;
		this.skin = skin;
		try {


			HashMap<String, String> headers = new HashMap<String, String>();

			headers.put("Origin", "http://slither.io");
			headers.put("Sec-WebSocket-Extensions", "permessage-deflate; client_max_window_bits");
			headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.75 Safari/537.36");

			Socket socket = new Socket(serverHost, serverPort);
			WebSocketClient client = new WebSocketClient(socket.getInputStream(), socket.getOutputStream(), serverHost + ":" + serverPort, "/slither", headers);
			processor = new WebSocketClientProcessor(client, this, true);
			processor.start();

			System.out.println("Connected");
			sendInit();
			connected = true;

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void sendInit() {
		byte[] user = username.getBytes(Charset.forName("UTF-8"));
		byte[] toSend = new byte[user.length + 3];
		toSend[0] = 0x73;
		toSend[1] = 0x07;
		toSend[2] = (byte) skin;
		System.arraycopy(user, 0, toSend, 3, user.length);
		processor.sendBinary(toSend);
	}

	@Override
	public void onClose() {
		slither.playing = false;
		connected = false;
	}

	public int int8() {
		return buffer.get() & 0xFF;
	}

	public int int16() {
		return (buffer.get() & 0xFF) << 8 |
				(buffer.get() & 0xFF);
	}

	public int int24() {
		return (buffer.get() & 0xFF) << 16 |
				(buffer.get() & 0xFF) << 8 |
				(buffer.get() & 0xFF);
	}

	public byte[] getBytes(int length) {
		byte[] b = new byte[length];
		buffer.get(b);
		return b;
	}

	@Override
	public void onBinary(byte[] buf) {
		try {
			synchronized (slither) {
				buffer = ByteBuffer.wrap(buf);
				lastTime = currentTime;
				currentTime = System.currentTimeMillis();
				int b = int16();
				int e = (int) (currentTime - lastTime);
				if (lastTime == 0) {
					e = 0;
				}
				slither.etm += e - b;

				int type = buffer.get() & 0xFF;
				int payloadLength = buffer.limit() - 3;

				switch (type) {
					case 'a':
						initialSetup(type, int24(), int16(), int16(), int16(), int8() / 10.0, int16() / 100.0, int16() / 100.0, int16() / 100.0, int16() / 1.0E3, int16() / 1.0E3, int16() / 1.0E3, int8());
						break;
					case 'e':
					case 'E':
					case '3':
					case '4':
					case '5': {
						int id = int16();
						int z = -1;
						double E = -1;
						double u = -1;
						double A = -1;
						if (payloadLength == 5) {
							z = 'e' == type ? 1 : 2;
							E = 2 * int8() * Math.PI / 256.0;
							u = 2 * int8() * Math.PI / 256.0;
							A = int8() / 18.0;
						} else if (payloadLength == 4) {
							if (type == 'e') {
								E = 2 * int8() * Math.PI / 256.0;
								A = int8() / 18.0;
							} else if (type == 'E') {
								z = 1;
								u = 2 * int8() * Math.PI / 256.0;
								A = int8() / 18.0;
							} else if (type == '4') {
								z = 2;
								u = 2 * int8() * Math.PI / 256.0;
								A = int8() / 18.0;
							} else if (type == '3') {
								z = 1;
								E = 2 * int8() * Math.PI / 256.0;
								u = 2 * int8() * Math.PI / 256.0;
							} else {
								z = 2;
								E = 2 * int8() * Math.PI / 256.0;
								u = 2 * int8() * Math.PI / 256.0;
							}
						} else if (payloadLength == 3) {
							if (type == 'e') {
								E = 2 * int8() * Math.PI / 256.0;
							} else if (type == 'E') {
								z = 1;
								u = 2 * int8() * Math.PI / 256.0;
							} else if (type == '4') {
								z = 2;
								u = 2 * int8() * Math.PI / 256.0;
							} else if (type == '3') {
								A = int8() / 18.0;
							}
						}
						updateSnakeDirection(type, id, E, u, A, z);
						break;
					}
					case 'g':
					case 'n':
					case 'G':
					case 'N': {
						if (slither.playing) {
							boolean I = type == 'n' || type == 'N';
							int id = int16();
							int x;
							int y;
							double fam = -1;
							boolean relative;

							if (type == 'g' || type == 'n') {
								x = int16();
								y = int16();
								relative = false;
							} else {
								x = int8() - 128;
								y = int8() - 128;
								relative = true;
							}
							if (I) {
								fam = int24() / 16777215.0;
							}
							updateSnakePosition(type, id, x, y, relative, fam, I);
						}
						break;
					}
					case 's':
						if (slither.playing) {
							int id = int16();
							if (payloadLength > 6) {
								double snakeStop = 2.0 * int24() * Math.PI / 16777215.0;
								int8();
								double ang = 2.0 * int24() * Math.PI / 16777215.0;
								double speed = int16() / 1.0e3;
								double I = int24() / 16777215.0;
								int skin = int8();
								double x = int24() / 5.0;
								double y = int24() / 5.0;
								int nameLength = int8();
								String name = new String(getBytes(nameLength), "UTF-8");
								boolean tail = false;

								double sx = 0;
								double sy = 0;

								ArrayList<Point2D.Double> points = new ArrayList<Point2D.Double>();

								while (buffer.hasRemaining()) {
									if (tail) {
										sx += (int8() - 127) / 2.0;
										sy += (int8() - 127) / 2.0;
									} else {
										sx = int24() / 5.0;
										sy = int24() / 5.0;
										tail = true;
									}
									points.add(new Point2D.Double(sx, sy));
								}
								newSnake(type, id, snakeStop, ang, speed, I, skin, x, y, name, points);
							} else {
								deleteSnake(type, id);
							}
						}
						break;
					case 'F':
						while (buffer.hasRemaining()) {
							addFood(type, int8(), int16(), int16(), int8() / 5.0, false);
						}
						break;
					case 'c':
						eatFood(type, int16(), int16(), buffer.hasRemaining() ? int16() : -1);
						break;
					case 'w':
						updateSector(type, true, int8(), int8());
						break;
					case 'W':
						updateSector(type, false, int8(), int8());
						break;
					case 'b':
					case 'f': {
						addFood(type, int8(), int16(), int16(), int8() / 5.0, type == 'b');
						break;
					}
					case 'r': {
						int id = int16();
						double fam = -1;
						if (payloadLength >= 4) {
							fam = int24() / 16777215.0;
						}
						shortenSnake(type, id, fam);
						break;
					}
					case 'h':
						updateFam(type, int16(), int24() / 16777215.0);
						break;
					case 'l': {
						slither.leaderboardRank = int8();
						slither.playerRank = int16();
						slither.numPlayers = int16();
						ArrayList<PlayerScore> leaderboard = new ArrayList<PlayerScore>();
						while (buffer.hasRemaining()) {
							leaderboard.add(new PlayerScore(int16(), int24() / 16777215.0, int8(), new String(getBytes(int8()))));
						}
						slither.leaderboard = leaderboard;
						break;
					}
					case 'v':

						break;
					case 'j':
					case 'y':
						//TODO PREYS
						break;
					case 'p':
						pong(type);
						break;
					default:
						System.out.println("Unknown packet (" + (char) type + ") received");

				}
			}
		} catch (Exception e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}
	}

	public void initialSetup(int packetType, int radius, int mscps, int sectorSize, int sectorCount, double spangdv, double nsp1, double nsp2, double nsp3, double mamu, double manu2, double cst, int version) {
		slither.gameRadius = radius;
		System.out.println("Initial setup");
		System.out.println("gameRadius = " + radius);
		System.out.println("mscps = " + mscps);
		System.out.println("sectorSize = " + sectorSize);
		System.out.println("sectorCount = " + sectorCount);
		System.out.println("spangdv = " + spangdv);
		System.out.println("nsp1 = " + nsp1);
		System.out.println("nsp2 = " + nsp2);
		System.out.println("nsp3 = " + nsp3);
		System.out.println("mamu = " + mamu);
		System.out.println("mamu2 = " + manu2);
		System.out.println("cst = " + cst);
		System.out.println("version = " + version);
		if (version != 8) {
			try {
				processor.close();
			} catch (Exception e) {
			}
			throw new RuntimeException("Unsupported server version: " + version);
		}
		slither.setMscps(mscps);
		slither.playing = true;
		slither.spangdv = spangdv;
		slither.sectorSize = sectorSize;
		slither.nsp1 = nsp1;
		slither.nsp2 = nsp2;
		slither.nsp3 = nsp3;
		slither.mamu = mamu;
		slither.cst = cst;
	}

	public void pong(int packetType /* p */) {
		slither.wfpr = false;
		ping = System.currentTimeMillis() - lastPing;
		if (slither.lagging) {
			slither.etm *= slither.lag_mult;
			slither.lagging = false;
		}
	}

	public void updateSnakeDirection(int packetType /* e E 3 4 5 */, int snakeId, double angle, double x, double A, int h) {
		Snake snake = slither.snakes.get(snakeId);
		if (snake != null) {

			if (h != -1) {
				snake.dir = h;
			}

			++slither.anguc;

			if (angle != -1) {
				if (snake.ang == angle)
					++slither.angnuc;
				double c = (angle - snake.ang) % (Math.PI * 2.0);
				if (c < 0)
					c += Math.PI * 2.0;
				if (c > Math.PI)
					c -= Math.PI * 2.0;
				int w = snake.fapos;

				for (int i = 0; i < slither.afc; ++i) {
					snake.fas[w] -= c * slither.afas[i];
					++w;
					if (w >= slither.afc) {
						w = 0;
					}
				}
				snake.fatg = slither.afc;
				snake.ang = angle;
			}
			if (x != -1) {
				if (snake.wang == x)
					++slither.wangnuc;
				snake.wang = x;
				if (snake != slither.player)
					snake.eang = x;
			}
			if (A != -1) {
				snake.sp = A;
				snake.spang = snake.sp / slither.spangdv;
				if (snake.spang > 1)
					snake.spang = 1;
			}
//			if(snake == slither.player) {
//				System.out.println(slither.player.wang + ", " + slither.player.ang + ", " + slither.player.dir + ", " + angle + ", " + x);
//
//			}
		}
	}

	public void shortenSnake(int packetType /* r */, int snakeId, double fam) {
		Snake snake = slither.snakes.get(snakeId);
		if (snake != null) {
			if (fam != -1) {
				snake.fam = fam;
			}
			for (Snake.BodySection section : snake.snakeBody) {
				if (!section.dying) {
					section.dying = true;
					snake.length--;
					snake.sc = Math.min(6.0, 1.0 + (snake.length - 2) / 106.0);
					snake.scang = 0.13 + 0.87 * Math.pow((7.0 - snake.sc) / 6.0, 2.0);
					snake.ssp = slither.nsp1 + slither.nsp2 * snake.sc;
					snake.fsp = snake.ssp + 0.1;
					snake.wsep = 6.0 * snake.sc;
					double c = slither.nsep / slither.gsc;
					if (snake.wsep < c)
						snake.wsep = c;
					break;
				}
			}
			snake.snl();
		}
	}

	public void addFood(int packetType /* b f */, int color, int x, int y, double size, boolean b) {
		slither.food.put(new Point(x, y), new Food(x, y, color, size));
	}

	public void updateSector(int packetType /* w W */, boolean destroy, int x, int y) {
		if (destroy) {
			slither.food.remove(new Point(x, y));
			Iterator<Map.Entry<Point, Food>> iter = slither.food.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<Point, Food> food = iter.next();
				if (food.getKey().x / slither.sectorSize == x && food.getKey().y / slither.sectorSize == y) {
					iter.remove();
				}
			}
			slither.sectors.remove(new Point(x, y));
		} else {
			slither.sectors.add(new Point(x, y));
		}
	}

	public void updateSnakePosition(int packetType /* g n G N */, int snakeId, int x, int y, boolean relative, double fam, boolean increase) {
		Snake s = slither.snakes.get(snakeId);

		if (s != null) {

			Snake.BodySection lastSection = s.snakeBody.get(s.snakeBody.size() - 1);
			double nx;
			double ny;
			if (relative) {
				nx = lastSection.x + x;
				ny = lastSection.y + y;
			} else {
				nx = x;
				ny = y;
			}

			if (increase) {
				s.length++;
			} else {
				for (Snake.BodySection section : s.snakeBody) {
					section.dying = true;
					break;
				}
			}

			if (fam != -1) {
				s.fam = fam;
			}

			Snake.BodySection section = new Snake.BodySection(nx, ny);

			section.ebx = s.x - lastSection.x;
			section.eby = s.y - lastSection.y;

			if (s.iiv) {
				double xx = s.x + s.fx - section.x;
				double yy = s.y + s.fy - section.y;

				section.fx += xx;
				section.fy += yy;
				section.exs.add(xx);
				section.eys.add(yy);
				section.efs.add(0);
				section.ems.add(1);
				++section.eiu;

			}

			int xl = s.snakeBody.size() - 3;
			if (xl >= 1) {
				Snake.BodySection end = s.snakeBody.get(xl);
				int n = 0;
				double h = 0;
				for (int i = xl - 1; i >= 0; --i) {
					Snake.BodySection current = s.snakeBody.get(i);
					++n;
					double xx = current.x;
					double yy = current.y;
					if (n <= 4) {
						h = slither.cst * n / 4.0;
					}
					current.x += (end.x - current.x) * h;
					current.y += (end.y - current.y) * h;
					if (s.iiv) {
						xx -= current.x;
						yy -= current.y;
						current.fx += xx;
						current.fy += yy;
						current.exs.add(xx);
						current.eys.add(yy);
						current.efs.add(0);
						current.ems.add(2);
						++current.eiu;
					}
					end = current;
				}
			}

			s.sc = Math.min(6.0, 1.0 + (s.length - 2.0) / 106.0);
			s.scang = 0.13 + 0.87 * Math.pow((7.0 - s.sc) / 6.0, 2.0);
			s.ssp = slither.nsp1 + slither.nsp2 * s.sc;
			s.fsp = s.ssp + 0.1;
			s.wsep = 6.0 * s.sc;
			double c = slither.nsep / slither.gsc;
			if (s.wsep < c)
				s.wsep = c;
			s.sep = s.wsep;

			if (increase) {
				s.snl();
			}

			s.lnp = section;

			// f == snake && (ovxx = snake.xx + snake.fx, ovyy = snake.yy + snake.fy);

			double z = slither.etm / 8.0 * s.sp / 4.0;

			z *= slither.lag_mult;

			double t = s.chl - 1;
			s.chl = z / s.msl;

			double xx = s.x;
			double yy = s.y;//TODO ERROR WHAT IS THIS

			s.x = nx + Math.cos(s.ang) * z;
			s.y = ny + Math.sin(s.ang) * z;

			double xxx = s.x - xx;
			double yyy = s.y - yy;
			double e = s.chl - t;
			int w = s.fpos;

			for (int i = 0; i < slither.rfc; ++i) {
				s.fxs[w] -= xxx * slither.rfas[i];
				s.fys[w] -= yyy * slither.rfas[i];
				s.fchls[w] -= e * slither.rfas[i];
				++w;
				if (w >= slither.rfc) {
					w = 0;
				}
				s.fx = s.fxs[s.fpos];
				s.fy = s.fys[s.fpos];
				s.fchl = s.fchls[s.fpos];
				s.ftg = slither.rfc;
				//if(D) {
				s.ehl = 0;
				//}
				//TODO VIEW STUFF
			}

			s.snakeBody.add(section);
		}
	}

	public void newSnake(int packetType, int snakeId, double snakeStop, double ang, double speed, double I, int skin, double x, double y, String name, ArrayList<Point2D.Double> points) {
//		System.out.println(snakeId + ": " + name + " " + I / 16777215.0);
		Snake snake = new Snake(slither, snakeId, ang);
		snake.x = x;
		snake.y = y;
		snake.name = name;
		snake.skin = skin;
		snake.ehang = snake.wehang = snakeStop;

		double lx = 0;
		double ly = 0;
		boolean first = true;

		ArrayList<Snake.BodySection> bodySections = new ArrayList<Snake.BodySection>();
		for (Point2D.Double p : points) {
			if (first) {
				lx = p.x;
				ly = p.y;
				first = false;
			}
			Snake.BodySection section = new Snake.BodySection(p.x, p.y);
			section.ebx = p.x - lx;
			section.eby = p.y - ly;
			bodySections.add(section);
			lx = p.x;
			ly = p.y;
		}
		snake.snakeBody = bodySections;
		snake.length = bodySections.size();

		snake.tl = snake.length + snake.fam;
		snake.cfl = snake.tl;

		snake.eang = snake.wang = ang;
		snake.sp = speed;
		snake.spang = snake.sp / slither.spangdv;
		if (snake.spang > 1)
			snake.spang = 1;
		snake.fam = I;
		snake.sc = Math.min(6.0, 1.0 + (snake.length - 2) / 106.0);
		snake.scang = 0.13 + 0.87 * Math.pow((7.0 - snake.sc) / 6.0, 2);
		snake.ssp = slither.nsp1 + slither.nsp2 * snake.sc;
		snake.fsp = snake.ssp + 0.1;
		snake.wsep = 6 * snake.sc;
		double c = slither.nsep / slither.gsc;
		if (snake.wsep < c)
			snake.wsep = c;
		snake.sep = snake.wsep;

		snake.snl();
		slither.snakes.put(snakeId, snake);
		if (slither.player == null) {
			slither.player = snake;
		}

	}

	public void updateFam(int packetType, int snakeId, double fam) {
		Snake snake = slither.snakes.get(snakeId);
		if (snake != null) {
			snake.fam = fam;
			snake.snl();
		}
	}

	public void deleteSnake(int packetType, int snakeId) {
		slither.snakes.remove(snakeId);
	}

	public void eatFood(int packetType, int x, int y, int eaterId) {
		slither.food.remove(new Point(x, y));
	}

	public void sendInput() {
		byte angle = (byte) ((((slither.angle / (2.0 * Math.PI)) % 1 + 1) % 1) * 250);
		if (angle != lastSentAngle) {
			processor.sendBinary(new byte[]{angle});
			lastSentAngle = angle;
		}
		if (lastBoost != slither.boost) {
			processor.sendBinary(new byte[]{(byte) (slither.boost ? 253 : 254)});
			lastBoost = slither.boost;
		}
	}

	public void ping() {
		lastPing = System.currentTimeMillis();
		processor.sendBinary(new byte[]{(byte) 251});
	}

}
