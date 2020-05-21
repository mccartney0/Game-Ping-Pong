package pong.entities;


import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Random;

import pong.main.Game;

public class Ball {

	public double x,y,dx,dy;
	public int w,h;
	public static double speed = 2.0;
	public static int pontoInimigo, pontoJogador;
	
	public Ball(int x, int y) {
		this.x =x;
		this.y =y;
		this.w = 4;
		this.h = 4;
		
		int angle = new Random().nextInt(120 - 45) + 45 + 5;
		dx = Math.cos(Math.toRadians(angle));
		dy = Math.sin(Math.toRadians(angle));
	}
	
	//lógica
	public void update() {
		
		if(x+(dx*speed) + w >= Game.W) {
			dx*=-1;
		}else if(x+(dx*speed) < 0 ) {
			dx*=-1;
		}
		
		if(y >= Game.H) {
			//Ponto inimigo
//			System.out.println("Ponto do inimigo ");
			pontoInimigo+=1;
			new Game();
			return;
		}else if(y <0) {
			//Ponto jogador
//			System.out.println("Ponto do Jogador ");
			pontoJogador+=1;
			new Game();
			return;
		}
		
		//Testar colisões
		Rectangle bounds = new Rectangle((int)(x+(dx*speed)),(int)(y+(dy*speed)),w,h);
		
		Rectangle boundsPlayer = new Rectangle(Game.player.x,Game.player.y,Game.player.w,Game.player.h);
		Rectangle boundsEnemy = new Rectangle((int)Game.enemy.x,(int)Game.enemy.y,Game.enemy.w,Game.enemy.h);
		
		//intersects = colidir
		if(bounds.intersects(boundsPlayer)) {
			int angle = new Random().nextInt(120 - 45) + 45;
			dx = Math.cos(Math.toRadians(angle));
			dy = Math.sin(Math.toRadians(angle));
			if(dy > 0)
				dy*=-1;
		}else if(bounds.intersects(boundsEnemy)) {
			int angle = new Random().nextInt(120 - 45) + 45 ;
			dx = Math.cos(Math.toRadians(angle));
			dy = Math.sin(Math.toRadians(angle));
			if(dy < 0)
				dy*=-1;
		}
		x+=dx*speed;
		y+=dy*speed;
		
		
	}
	
	//renderização
	public void render(Graphics g) {
		g.setColor(Color.white);
		g.fillRect((int)x,(int) y, w, h);
	}
}
