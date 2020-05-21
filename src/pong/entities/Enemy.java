package pong.entities;

import java.awt.Color;
import java.awt.Graphics;

import pong.main.Game;

public class Enemy {

	public boolean right, left;
	public double x, y;
	public int w, h;
	public static double difficulty = 0.02;

	public Enemy(int x, int y) {
		this.x = x;
		this.y = y;
		this.w = 40;
		this.h = 7;
	}

	// lógica
	public void update() {
		x += (Game.ball.x - x - 6) * difficulty; // Dificuldade inimigo

		if (right) {
			x = x + 2;
		} else if (left) {
			x = x - 2;
		}

		if (x + w > Game.W) {
			x = Game.W - w;
		} else if (x < 0) {
			x = 0;
		}

		if (Ball.pontoInimigo >= 55) {
			Game.gameState = "GAMEOVER";
			Ball.pontoInimigo = 0;
			Ball.pontoJogador = 0;
		}

		if (Ball.pontoJogador >= 1) {
			Game.gameState = "WIN";
			Ball.pontoInimigo = 0;
			Ball.pontoJogador = 0;
			Game.nextNivel = true;
			Game.nivel++;
		}

		// fazer outros niveis com outras dificuldades
	}

	// renderização
	public void render(Graphics g) {
		g.setColor(Color.red);
		g.fillRect((int) x, (int) y, w, h);
	}
}
