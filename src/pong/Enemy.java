package pong;

import java.awt.Color;
import java.awt.Graphics;

public class Enemy {

	public double x,y;
	public int w,h;
	
	public Enemy(int x, int y) {
		this.x =x;
		this.y =y;
		this.w = 40;
		this.h = 7;
	}
	
	//lógica
	public void update() {
		x+= (Game.ball.x -x - 6) * 0.1;  // Dificuldade inimigo
	}
	
	//renderização
	public void render(Graphics g) {
		g.setColor(Color.red);
		g.fillRect((int)x,(int) y, w, h);
	}
}
