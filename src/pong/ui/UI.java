package pong.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import pong.entities.Ball;
import pong.main.Game;


public class UI {
	
	public int x, y,w,h;
	
	public UI(int x, int y) {
		this.x = x;
		this.y = y;
		this.w = 1;
		this.h = 7;
	}
	
	//renderização
		public void render(Graphics g) {
			g.setFont(new Font("arial", Font.BOLD, 20));
			g.setColor(Color.white);
			g.drawString("Nível:", 5, 50);
			g.drawString(Game.nivel+"", 65, 50);
			g.drawString(Ball.pontoJogador+"", 40, 240);
			g.drawString("Pontos", 13, 120);
			g.drawString("Máquina", 6, 150);
			g.drawString(Ball.pontoInimigo+"", 40, 180);
			g.drawString("Jogador", 6, 210);
			g.drawString(Ball.pontoJogador+"", 40, 240);
		}
}
