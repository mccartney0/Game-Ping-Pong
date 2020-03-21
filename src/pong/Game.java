package pong;

import java.awt.Canvas;
import java.awt.Dimension;

import javax.swing.JFrame;

public class Game extends Canvas implements Runnable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static int W = 240;
	public static int H = 120;
	public static int SCALE = 3;
	
	public Game() {
		this.setPreferredSize(new Dimension(W*SCALE,H*SCALE));
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
		
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
