package pong.main;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

import pong.entities.Ball;
import pong.entities.Enemy;
import pong.entities.Player;
import pong.ui.Menu;
import pong.ui.UI;


public class Game extends Canvas implements Runnable, KeyListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static int W = 160;
	public static int H = 120;
	public static int SCALE = 3;

	// Renderizando, parando de piscar a tela java 8 -
	public BufferedImage layer = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);

	public static String gameState = "NORMAL";
	@SuppressWarnings("unused")
	private boolean isRunning = true;
	@SuppressWarnings("unused")
	private Thread thread;

	private boolean restartGame = false;
	private int framesGameOver = 0;
	private boolean showMessageGameOver = true;
	
	public static int nivel = 1;
	public static boolean nextNivel;
	public static Player player;
	public static Enemy enemy;
	public static Ball ball;
	public static Menu menu;
	public static UI ui;

	public Game() {
		this.setPreferredSize(new Dimension(W * SCALE, H * SCALE));
		this.addKeyListener(this);
		player = new Player(60, H - 8);
		enemy = new Enemy(60, 1);
		ball = new Ball(100, H / 10 - 5);
		ui = new UI(100, 1);
	}

//	public synchronized void stop() {
//		isRunning = false;
//		try {
//			thread.join();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//	}

	public static void main(String[] args) {
		Game game = new Game();
		JFrame frame = new JFrame("Ping Pong");
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(game);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		new Thread(game).start();

	}

	public void update() {
		if (gameState == "NORMAL") {
			this.restartGame = false; // Prevenção
			player.update();
			enemy.update();
			ball.update();
		}

		if (restartGame) {
			this.restartGame = false;
			Game.gameState = "NORMAL";
		}

		if (gameState == "GAMEOVER") {
			// Forma de Fazer animação - Game over
			this.framesGameOver++;
			if (this.framesGameOver == 30) {
				this.framesGameOver = 0;
				if (this.showMessageGameOver)
					this.showMessageGameOver = false;
				else
					this.showMessageGameOver = true;
			}

		}
		
		if(nextNivel) {
			if(1 < Game.nivel) {
				nextNivel = false;
				Ball.speed += 0.5;
				Enemy.difficulty += 0.05;
			}
		}
		
		
	}//FIM UPDATE

	public void render() {
		BufferStrategy bs = this.getBufferStrategy();
		if (bs == null) {
			this.createBufferStrategy(3);
			return;

		}
		Graphics g = layer.getGraphics();
		g.setColor(Color.black);
		g.fillRect(0, 0, W, H);

		player.render(g);
		enemy.render(g);
		ball.render(g);
		g.dispose();

		g = bs.getDrawGraphics();
		g.drawImage(layer, 0, 0, W * SCALE, H * SCALE, null);
		ui.render(g);
//		g.drawImage(layer, 100, 0, W * SCALE -100, H * SCALE, null);
		
		if (gameState == "GAMEOVER") {
			Graphics2D g2 = (Graphics2D) g;
			g2.setColor(new Color(0, 0, 0, 230));
			g2.fillRect(0, 0, W * SCALE, H * SCALE);
			g.setFont(new Font("arial", Font.BOLD, 36));
			g.setColor(Color.white);
			g.drawString("Game Over", 150, 150);
			g.setFont(new Font("arial", Font.BOLD, 25));

			if (showMessageGameOver) {
				g.drawString(">Pressione Enter para reiniciar<", 50, 234);
			}
		}
		
		if (gameState == "WIN") {
			Graphics2D g2 = (Graphics2D) g;
			g2.setColor(new Color(0, 0, 0, 230));
			g2.fillRect(0, 0, W * SCALE, H * SCALE);
			g.setFont(new Font("arial", Font.BOLD, 36));
			g.setColor(Color.white);
			g.drawString("You WIN!", 160, 130);
			g.setFont(new Font("arial", Font.BOLD, 25));
			g.drawString("Para avançar de nível",100, 204);
			g.drawString(">Pressione Enter<", 125, 234);
		}
		bs.show();
	}

	@Override // Game looping
	public void run() {

		while (true) {
			requestFocus(); // Para focar automaticamente na janela e os comandos funcionarem, sem precisar
							// clicar, basta inserir na primeira linha do Game Looping:
			update();
			render();
			try {
				Thread.sleep(1000 / 60);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}
//	public void run() {
//
//		long lastTime = System.nanoTime();
//		double amountOfUpdates = 60.0;
//		double ns = 1000000000 / amountOfUpdates;
//		double delta = 0;
//		@SuppressWarnings("unused")
//		int frames = 0;
//		double timer = System.currentTimeMillis();
//		requestFocus();
//		while (isRunning) {
//			long now = System.nanoTime();
//			delta += (now - lastTime) / ns;
//			lastTime = now;
//
//			if (delta >= 1) {
//				update();
//				render();
//				frames++;
//				delta--;
//			}
//
//			if (System.currentTimeMillis() - timer >= 1000) {
////				System.out.println("FPS: " + frames);
//				frames = 0;
//				timer += 1000;
//			}
//
//		}
//
//		stop();
//	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			player.right = true;
		} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			player.left = true;
		}

		if (e.getKeyCode() == KeyEvent.VK_A) {
			player.left = true;
		} else if (e.getKeyCode() == KeyEvent.VK_D) {
			player.right = true;
		}

		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			this.restartGame = true;
		}

	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			player.right = false;
		} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			player.left = false;
		}

		if (e.getKeyCode() == KeyEvent.VK_A) {
			player.left = false;
		} else if (e.getKeyCode() == KeyEvent.VK_D) {
			player.right = false;
		}
	}

}
