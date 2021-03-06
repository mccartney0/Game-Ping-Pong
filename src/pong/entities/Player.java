package pong.entities;

import java.awt.Color;
import java.awt.Graphics;

import pong.main.Game;

public class Player {

	public boolean right, left;
	public int x, y,w,h;
	public int sizePlayer;
	
	public Player(int x, int y) {
		this.x = x;
		this.y = y;
		this.w = 40 + sizePlayer;
		this.h = 7;
	}

	public void update() {
		if(right) {
			x=x+2;
		}else if(left) {
			x=x-2;
		}
		
		if(x+w > Game.W) {
			x = Game.W - w;
		}else if(x<0) {
			x=0;
		}
		
		if(Ball.pontoJogador == 1) {
			sizePlayer += 5;
		}
	}

	public void render(Graphics g) {
		g.setColor(Color.blue);
		g.fillRect(x, y, w, h);
	}
}
