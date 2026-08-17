package pong.main;

import java.awt.Rectangle;

public class ArenaElement {

    public ArenaElementType type;
    public int x;
    public int y;
    public int width;
    public int height;

    public ArenaElement(ArenaElementType type, int x, int y, int width, int height) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Rectangle bounds() {
        return new Rectangle(x, y, width, height);
    }
}
