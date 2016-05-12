package tk.jackyliao123.slither;

import tk.jackyliao123.slither.controller.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class SlitherUI {

	public Slither slither;
	public SlitherCanvas slitherCanvas;
	public UpdateThread timerThread;

	public SlitherUI(Slither slither) {
		this.slither = slither;
		timerThread = new UpdateThread(this, 60);
		JFrame frame = new JFrame("slither.io");
		slitherCanvas = new SlitherCanvas(slither);
		slitherCanvas.setPreferredSize(new Dimension(800, 600));
		frame.add(slitherCanvas);
		slitherCanvas.addKeyListener(new SwitchListener());
		slitherCanvas.setFocusable(true);
		slitherCanvas.requestFocus();
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		slitherCanvas.setupBuffers();
		new FPSThread(this).start();
	}

	public class SwitchListener extends KeyAdapter {
		public void keyPressed(KeyEvent e) {
			switch(e.getKeyCode()) {
				case KeyEvent.VK_S:
					slither.slitherClient.controller = new SpinController(slither);
					break;
				case KeyEvent.VK_H:
					slither.slitherClient.controller = new HumanController(slither, SlitherUI.this);
					break;
				case KeyEvent.VK_M:
					slither.slitherClient.controller = new MoveToTargetController(slither);
					break;
				case KeyEvent.VK_P:
					PathFindingController contr = new PathFindingController(slither);
					slither.slitherClient.controller = contr;
					contr.subController = new HumanController(slither, SlitherUI.this);
					break;
				case KeyEvent.VK_C:
					slither.slitherClient.controller = new CollectFoodController(slither);
					break;
				case KeyEvent.VK_B: {
					new Thread (){
						public void run() {
							final Slither s = new Slither(slither.serverURI, "testBotHelper", 14);
							s.slitherClient.controller = new PathFindingController(s, slither.player);
							new Thread() {
								public void run() {
									while (true) {
										try {
											s.slitherClient.update();
											Thread.sleep(50);
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								}
							}.start();
							System.out.println("Bot started");
						}
					}.start();
					break;
				}
				default:
					slither.slitherClient.controller.keyPress(e);
			}
		}
	}

	public void start() {
		timerThread.start();
	}

	public void update() {
		slitherCanvas.render();
	}

	public static class FPSThread extends Thread {
		public SlitherUI slitherUI;
		public FPSThread(SlitherUI slitherUI) {
			this.setName("FPS counter");
			this.slitherUI = slitherUI;
		}
		public void run() {
			while(true) {
				try {
					Thread.sleep(1000);
				} catch(InterruptedException e){}
				slitherUI.slitherCanvas.fps = slitherUI.slitherCanvas.frameCount;
				slitherUI.slitherCanvas.frameCount = 0;
			}
		}
	}

	public static class UpdateThread extends Thread {
		public SlitherUI slitherUI;

		public long targetFPS;

		public UpdateThread(SlitherUI slitherUI, int targetFPS) {
			this.setName("Client Update Thread");
			this.slitherUI = slitherUI;
			this.targetFPS = targetFPS;
		}

		public void run() {
			while(true) {
				long startTime = System.currentTimeMillis();
				slitherUI.update();
				long endTime = System.currentTimeMillis();
				double delta = endTime - startTime;
				double delay = 1000.0 / targetFPS;
				try {
					Thread.sleep(Math.round(Math.max(0, delay - delta)));
				}
				catch(InterruptedException e){}
			}
		}
	}
}
