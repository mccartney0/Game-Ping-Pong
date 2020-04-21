package pong;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

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
	
	public static Player player;
	public static Enemy enemy;
	public static Ball ball;
	public static Menu menu;

	public Game() {
		this.setPreferredSize(new Dimension(W * SCALE, H * SCALE));
		this.addKeyListener(this);
		player = new Player(100, H-8);
		enemy = new Enemy(100,1);
		ball = new Ball(100,H/2-1);
	}

	public static void main(String[] args) {
		Game game = new Game();
		JFrame frame = new JFrame("Pong");
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
		player.update();
		enemy.update();
		ball.update();
		}
	}

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
		
		g = bs.getDrawGraphics();
		g.drawImage(layer, 0, 0, W * SCALE, H * SCALE, null);

		bs.show();
	}

	@Override //Game looping
	public void run() {

		while (true) {
			requestFocus(); // Para focar automaticamente na janela e os comandos funcionarem, sem precisar clicar, basta inserir na primeira linha do Game Looping:
			update();
			render();
			try {
				Thread.sleep(1000 / 60);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

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
		
		if(e.getKeyCode() == KeyEvent.VK_A) {
			player.left = true;
		} else if (e.getKeyCode() == KeyEvent.VK_D) {
			player.right = true;
		}

	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			player.right = false;
		} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			player.left = false;
		}
		
		if(e.getKeyCode() == KeyEvent.VK_A) {
			player.left = false;
		} else if (e.getKeyCode() == KeyEvent.VK_D) {
			player.right = false;
		}
	}

}
