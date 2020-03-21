package pong;

import java.awt.Color;
import java.awt.Graphics;

public class Player {

	public boolean right, left;
	public int x, y,w,h;

	public Player(int x, int y) {
		this.x = x;
		this.y = y;
		this.w = 40;
		this.h = 10;
	}

	public void update() {
		if(right) {
			x++;
		}else if(left) {
			x--;
		}
		
		if(x+w > Game.W) {
			x = Game.W - w;
		}else if(x<0) {
			x=0;
		}
	}

	public void render(Graphics g) {
		g.setColor(Color.blue);
		g.fillRect(x, y, w, h);
	}
}
