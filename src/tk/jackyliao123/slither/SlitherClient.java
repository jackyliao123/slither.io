package tk.jackyliao123.slither;

public class SlitherClient {

	public Slither slither;
	public Controller controller;
	public long lastUpdated = System.currentTimeMillis();

	public double fr = 0;
	public double lfr;
	public double fr2 = 0;
	public double lfr2;
	public double vfr;
	public int vfrb;
	public int vfrb2;

	public SlitherClient(Slither slither) {
		this.slither = slither;
	}

	public void update() {
		if (slither.player != null) {
			slither.player.eang = slither.angle;
		}

		long b = System.currentTimeMillis();

		if (slither.messageProcessor.connected) {
			try {
				if (!slither.wfpr && b - slither.lastPing > 250) {
					slither.messageProcessor.ping();
					slither.wfpr = true;
					slither.lastPing = b;
				}

				if (b - slither.lastInput > 100) { //TODO original 150
					slither.messageProcessor.sendInput();
					slither.lastInput = b;
				}
			} catch (Exception e) {
				slither.messageProcessor.connected = false;
			}
		}

		vfr = (b - lastUpdated) / 8.0;
		if (b - slither.lastPing > 420) {
			slither.lagging = true;
		}
		if (slither.lagging) {
			slither.lag_mult *= 0.85;
			if (slither.lag_mult < 0.01)
				slither.lag_mult = 0.01;
		} else if (slither.lag_mult < 1) {
			slither.lag_mult += 0.05;
			if (slither.lag_mult > 1)
				slither.lag_mult = 1;
		}
		if (vfr > 120)
			vfr = 120;
		lastUpdated = b;

		vfr *= slither.lag_mult;
		slither.etm *= slither.lag_mult;

		lfr = fr;
		fr += vfr;

		vfrb = (int) Math.round(Math.floor(fr) - Math.floor(lfr));

		lfr2 = fr2;
		fr2 += 2 * vfr;
		vfrb2 = (int) Math.round(Math.floor(fr2) - Math.floor(lfr2));

		slither.etm *= Math.pow(0.993, vfrb);

		for (Snake snake : slither.snakes.values()) {

			double f = slither.mamu * vfr * snake.scang * snake.spang;
			double c = snake.sp * vfr / 4.0;
			if (c > snake.msl)
				c = snake.msl;

			if (!snake.dead) {
				if (snake.tsp != snake.sp) {
					if (snake.tsp < snake.sp) {
						snake.tsp += 0.3 * vfr;
						if (snake.tsp > snake.sp)
							snake.tsp = snake.sp;
					} else {
						snake.tsp -= 0.3 * vfr;
						if (snake.tsp < snake.sp)
							snake.tsp = snake.sp;
					}
				}
				if (snake.tsp > snake.fsp) {
					snake.sfr += (snake.tsp - snake.fsp) * vfr * 0.021;
				}
				if (snake.fltg > 0) {
					double h = vfrb;
					if (h > snake.fltg)
						h = snake.fltg;
					snake.fltg -= h;
					for (int i = 0; i < h; ++i) {
						snake.fl = snake.fls[snake.flpos];
						snake.fls[snake.flpos] = 0;
						snake.flpos++;
						if (snake.flpos >= slither.lfc) {
							snake.flpos = 0;
						}
					}
				} else {
					if (snake.fltg == 0) {
						snake.fltg = -1;
						snake.fl = 0;
					}
				}
				snake.cfl = snake.tl + snake.fl;
			}

			// TODO REPETITIVE CODE

			if (snake.dir == 1) {
				snake.ang -= f;
				if (snake.ang < 0 || snake.ang >= Math.PI * 2)
					snake.ang %= Math.PI * 2;
				if (snake.ang < 0)
					snake.ang += Math.PI * 2;
				double h = (snake.wang - snake.ang) % (Math.PI * 2);
				if (h < 0)
					h += Math.PI * 2;
				if (h > Math.PI)
					h -= Math.PI * 2;
				if (h > 0) {
					snake.ang = snake.wang;
					snake.dir = 0;
				}
			} else if (snake.dir == 2) {
				snake.ang += f;
				if (snake.ang < 0 || snake.ang >= Math.PI * 2)
					snake.ang %= Math.PI * 2;
				if (snake.ang < 0)
					snake.ang += Math.PI * 2;
				double h = (snake.wang - snake.ang) % (Math.PI * 2);
				if (h < 0)
					h += Math.PI * 2;
				if (h > Math.PI)
					h -= Math.PI * 2;
				if (h < 0) {
					snake.ang = snake.wang;
					snake.dir = 0;
				}
			} else
				snake.ang = snake.wang;

			if (snake.ehl != 1) {
				snake.ehl += 0.03 * vfr;
				if (snake.ehl >= 1)
					snake.ehl = 1;
			}

			Snake.BodySection part = snake.snakeBody.get(snake.snakeBody.size() - 1);
			snake.wehang = Math.atan2(snake.y + snake.fy - part.y - part.fy + part.eby * (1.0 - snake.ehl), snake.x + snake.fx - part.x - part.fx + part.ebx * (1.0 - snake.ehl));

			if (!snake.dead && snake.ehang != snake.wehang) {
				double h = (snake.wehang - snake.ehang) % (Math.PI * 2);
				if (h < 0)
					h += Math.PI * 2;
				if (h > Math.PI)
					h -= Math.PI * 2;
				if (h < 0)
					snake.edir = 1;
				else if (h > 0)
					snake.edir = 2;
			}
			//TODO REPETITIVE CODE
			if (snake.edir == 1) {
				snake.ehang -= 0.1 * vfr;
				if (snake.ehang < 0 || snake.ehang >= Math.PI * 2)
					snake.ehang %= Math.PI * 2;
				if (snake.ehang < 0)
					snake.ehang += Math.PI * 2;
				double h = (snake.wehang - snake.ehang) % (Math.PI * 2);
				if (h < 0)
					h += Math.PI * 2;
				if (h > Math.PI)
					h -= Math.PI * 2;
				if (h > 0) {
					snake.ehang = snake.wehang;
					snake.edir = 0;
				}
			} else if (snake.edir == 2) {
				snake.ehang += 0.1 * vfr;
				if (snake.ehang < 0 || snake.ehang >= Math.PI * 2)
					snake.ehang %= Math.PI * 2;
				if (snake.ehang < 0)
					snake.ehang += Math.PI * 2;
				double h = (snake.wehang - snake.ehang) % (Math.PI * 2);
				if (h < 0)
					h += Math.PI * 2;
				if (h > Math.PI)
					h -= Math.PI * 2;
				if (h < 0) {
					snake.ehang = snake.wehang;
					snake.edir = 0;
				}
			}

			if (!snake.dead) {
				snake.x += Math.cos(snake.ang) * c;
				snake.y += Math.sin(snake.ang) * c;
				snake.chl += c / snake.msl;
			}

			if (vfrb > 0) {

				for (int i = snake.snakeBody.size() - 1; i >= 0; --i) {
					Snake.BodySection bs = snake.snakeBody.get(i);
					if (bs.dyingTimeout > 1) {
						snake.snakeBody.remove(i);
					} else {
						if (bs.dying) {
							bs.dyingTimeout += .0015 * vfrb;
						}
					}
				}

				for (int i = snake.snakeBody.size() - 1; i >= 0; --i) {
					Snake.BodySection bs = snake.snakeBody.get(i);
					if (bs.eiu > 0) {
						double fx = 0;
						double fy = 0;
						int cm1 = bs.eiu - 1;
						for (int j = cm1; j >= 0; --j) {
							bs.efs.set(j, bs.ems.get(j) == 2 ? bs.efs.get(j) + vfrb2 : bs.efs.get(j) + vfrb);
							int h = bs.efs.get(j);
							if (h >= slither.hfc) {
								if (j != cm1) {
									bs.exs.set(j, bs.exs.get(cm1));
									bs.eys.set(j, bs.eys.get(cm1));
									bs.efs.set(j, bs.efs.get(cm1));
									bs.ems.set(j, bs.ems.get(cm1));
								}
								--bs.eiu;
								bs.exs.remove(bs.eiu);
								bs.eys.remove(bs.eiu);
								bs.efs.remove(bs.eiu);
								bs.ems.remove(bs.eiu);
								--cm1;
							} else {
								fx += bs.exs.get(j) * slither.hfas[h];
								fy += bs.eys.get(j) * slither.hfas[h];
							}
						}
						bs.fx = fx;
						bs.fy = fy;
					}
				}
			}

			double x = Math.cos(snake.eang) * snake.pma;
			double y = Math.sin(snake.eang) * snake.pma;
			if (snake.rex < x) {
				snake.rex += vfr / 6.0;
				if (snake.rex >= x)
					snake.rex = x;
			}
			if (snake.rey < y) {
				snake.rey += vfr / 6.0;
				if (snake.rey >= y)
					snake.rey = y;
			}
			if (snake.rex > x) {
				snake.rex -= vfr / 6.0;
				if (snake.rex <= x)
					snake.rex = x;
			}
			if (snake.rey > y) {
				snake.rey -= vfr / 6.0;
				if (snake.rey <= y)
					snake.rey = y;
			}

			if (vfrb > 0) {
				if (snake.ftg > 0) {
					int h = vfrb;
					if (h > snake.ftg)
						h = snake.ftg;
					snake.ftg -= h;
					for (int i = 0; i < h; ++i) {
						snake.fx = snake.fxs[snake.fpos];
						snake.fy = snake.fys[snake.fpos];
						snake.fchl = snake.fchls[snake.fpos];
						snake.fxs[snake.fpos] = 0;
						snake.fys[snake.fpos] = 0;
						snake.fchls[snake.fpos] = 0;
						++snake.fpos;
						if (snake.fpos >= slither.rfc) {
							snake.fpos = 0;
						}
					}
				} else if (snake.ftg == 0) {
					snake.ftg = -1;
					snake.fx = 0;
					snake.fy = 0;
					snake.fchl = 0;
				}
				if (snake.fatg > 0) {
					int h = vfrb;
					if (h > snake.fatg)
						h = snake.fatg;
					snake.fatg -= h;
					for (int i = 0; i < h; ++i) {
						snake.fa = snake.fas[snake.fapos];
						snake.fas[snake.fapos] = 0;
						++snake.fapos;
						if (snake.fapos >= slither.afc)
							snake.fapos = 0;
					}
				} else if (snake.fatg == 0) {
					snake.fatg = -1;
					snake.fa = 0;
				}
			}

		}

		if (controller != null) {
			controller.update();
		}

	}
}
