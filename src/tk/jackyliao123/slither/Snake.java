package tk.jackyliao123.slither;

import java.util.ArrayList;

public class Snake {

	public Slither slither;

	public int id;
	public double x;
	public double y;

	public double fx;
	public double fy;

	public String name;

	public int skin;
	public int length;

	public double sc = 1;
	public double fam;

	public double ang;
	public double wang;
	public double eang;
	public int dir;

	public double sp = 2;
	public double spang;

	public int fapos;
	public double[] fas;
	public int fatg;

	public double scang = 1;
	public double ssp;
	public double fsp;
	public double wsep;

	public BodySection lnp;

	public double chl;
	public double msl = 42; //TODO MAYBE INT?
	public double ehl = 1;

	public int fpos;
	public double[] fxs;
	public double[] fys;
	public double[] fchls;

	public int flpos;
	public double[] fls;

	public double fchl;
	public int ftg;

	public boolean iiv; // Is in view

	public double tl;

	public boolean dead;
	public double tsp;
	public double sfr;
	public double fltg;
	public double fl;
	public double cfl;
	public double ehang;
	public double wehang;
	public int edir;

	public double sep;
	public double pma = 2.3; //TODO SKIN

	public double rex;
	public double rey;

	public double fa;

	public ArrayList<BodySection> snakeBody = new ArrayList<BodySection>();

	public Object customData;

	public Snake(Slither slither, int id, double angle) {
		this.slither = slither;
		this.id = id;
		fas = new double[slither.afc];
		fxs = new double[slither.rfc];
		fys = new double[slither.rfc];
		fchls = new double[slither.rfc];
		fls = new double[slither.lfc];

		ang = eang = wang = angle;

		ssp = slither.nsp1 + slither.nsp2 * sc;
		fsp = ssp + 0.1;
	}

	public void snl() {
		double f = tl;
		tl = length + fam;
		f = tl - f;
		int b = flpos;
		for (int h = 0; h < slither.lfc; ++h) {
			fls[b] -= f * slither.lfas[h];
			++b;
			if (b >= slither.lfc)
				b = 0;
		}
		fl = fls[flpos];
		fltg = slither.lfc;

	}

	public static class BodySection {
		public double x;
		public double y;

		public double fx;
		public double fy;

		public boolean dying;
		public double dyingTimeout;

		public ArrayList<Double> exs = new ArrayList<Double>();
		public ArrayList<Double> eys = new ArrayList<Double>();
		public ArrayList<Integer> efs = new ArrayList<Integer>();
		public ArrayList<Integer> ems = new ArrayList<Integer>();

		public int eiu;
		public double ebx;
		public double eby;

		public BodySection(double x, double y) {
			this.x = x;
			this.y = y;
		}
	}
}
