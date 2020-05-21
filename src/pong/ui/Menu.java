package pong.ui;

import pong.main.Game;

public class Menu {

	public String[] options = { "novo jogo", "sair" };
	public int currentOption = 0;
	public int maxOption = options.length - 1;

	public boolean up, down, enter;

	public static boolean pause = false;
	
	public void update() {
		if (up) {
			up = false;
			currentOption--;
			if (currentOption < 0)
				currentOption = maxOption;
		}
		if (down) {
			down = false;
			currentOption++;
			if (currentOption > maxOption)
				currentOption = 0;
		}
		if (enter) {
			enter = false;
			if (options[currentOption] == "novo jogo" || options[currentOption] == "continuar") {
				Game.gameState = "NORMAL";
				pause = false;
			}
		}
	}
}
