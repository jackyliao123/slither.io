package tk.jackyliao123.slither;

import tk.jackyliao123.slither.controller.HumanController;
import tk.jackyliao123.slither.controller.MoveToTargetController;
import tk.jackyliao123.slither.controller.SpinController;

import java.awt.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Slither {

	public String serverHost;
	public int serverPort;
	public SlitherMessageProcessor messageProcessor;
	public SlitherClient slitherClient;

	public int gameRadius;

	public HashMap<Integer, Snake> snakes = new HashMap<Integer, Snake>();
	public HashMap<Point, Food> food = new HashMap<Point, Food>();
	public HashSet<Point> sectors = new HashSet<Point>();

	public ArrayList<PlayerScore> leaderboard;

	public int playerRank = -1;
	public int numPlayers = -1;
	public int leaderboardRank = -1;

	public Snake player;

	public boolean playing;

	public int mscps;
	public double[] fmlts;
	public double[] fpsls;

	public int hfc = 92;
	public int afc = 26;
	public int rfc = 43;
	public int lfc = 128;

	public double[] hfas = new double[hfc];
	public double[] afas = new double[afc];
	public double[] rfas = new double[rfc];
	public double[] lfas = new double[lfc];

	public double cst;

	public int sectorSize;

	public double nsp1 = 4.25;
	public double nsp2 = 0.5;
	public double nsp3 = 12;

	public double mamu = 0.22;
//	public double mamu2 = 0.028;

	public double anguc;
	public double angnuc;

	public double wangnuc;

	public double spangdv = 4.8;

	public double lag_mult = 1;

	public double nsep = 4.5;

	public double gsc = 0.9;

	public long etm;

	public boolean boost;
	public double angle;

	public boolean wfpr;

	public boolean lagging;

	public long lastPing;
	public long lastInput;

	public Slither(String serverHost, int serverPort, String username, int skin) {
		this.serverHost = serverHost;
		this.serverPort = serverPort;
		generateConstants();
		messageProcessor = new SlitherMessageProcessor(this, serverHost, serverPort, username, skin);
		slitherClient = new SlitherClient(this);
	}

	public void generateConstants() {
		for (int i = 0; i < hfc; ++i) {
			hfas[i] = 0.5 * (1.0 - Math.cos(Math.PI * (hfc - 1.0 - i) / (hfc - 1.0)));
		}
		for (int i = 0; i < afc; ++i) {
			afas[i] = 0.5 * (1.0 - Math.cos(Math.PI * (afc - 1.0 - i) / (afc - 1.0)));
		}
		for (int i = 0; i < rfc; ++i) {
			rfas[i] = 0.5 * (1.0 - Math.cos(Math.PI * (rfc - 1.0 - i) / (rfc - 1.0)));
		}
		for (int i = 0; i < lfc; ++i) {
			lfas[i] = 0.5 * (1.0 - Math.cos(Math.PI * (lfc - 1.0 - i) / (lfc - 1.0)));
		}
	}

	public void setMscps(int mscps) {
		if (this.mscps != mscps) {
			this.mscps = mscps;
			fmlts = new double[mscps + 2049];
			fpsls = new double[mscps + 2049];
			for (int b = 0; b <= mscps; b++) {
				if (b >= mscps)
					fmlts[b] = fmlts[b - 1];
				else
					fmlts[b] = Math.pow(1.0 - b * 1.0 / mscps, 2.25);
				if (0 == b)
					fpsls[b] = 0;
				else
					fpsls[b] = fpsls[b - 1] + 1.0 / fmlts[b - 1];
			}
			for (int b = 0; b < 2048; b++) {
				fmlts[b + mscps] = fmlts[mscps];
				fpsls[b + mscps] = fpsls[mscps];
			}
		}
	}

	public static void main(String[] args) throws Exception {
		System.setProperty("sun.java2d.opengl", "true");
		System.setProperty("sun.java2d.d3d", "false");
		System.setProperty("sun.java2d.noddraw", "true");
		Slither slither = new Slither("45.58.115.162", 444, "testBot", 15);
		SlitherUI ui = new SlitherUI(slither);
		slither.slitherClient.controller = new HumanController(slither, ui);
		ui.start();

	}

}
