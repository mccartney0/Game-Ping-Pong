package pong.main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class ArenaEditor {

    private final ArenaBlueprint blueprint;
    private int cursorX = Game.W / 2;
    private int cursorY = Game.H / 2;
    private int selectedType;

    public ArenaEditor() {
        blueprint = new ArenaBlueprint("Arena Mutante");
        blueprint.clear();
    }

    public ArenaBlueprint getBlueprint() {
        return blueprint;
    }

    public void load(ArenaBlueprint source) {
        blueprint.clear();
        if (source == null) {
            return;
        }
        blueprint.setName(source.getName());
        for (ArenaElement element : source.getElements()) {
            blueprint.addElement(element.type, element.x, element.y, element.width, element.height);
        }
    }

    public ArenaElementType getSelectedType() {
        return ArenaElementType.values()[selectedType];
    }

    public void moveCursor(int dx, int dy) {
        cursorX = Math.max(8, Math.min(Game.W - 8, cursorX + dx));
        cursorY = Math.max(34, Math.min(Game.H - 22, cursorY + dy));
    }

    public void cycleType(int direction) {
        int length = ArenaElementType.values().length;
        selectedType = (selectedType + direction + length) % length;
    }

    public void place() {
        ArenaElementType type = getSelectedType();
        int width = type == ArenaElementType.BLOCK ? 30 : 14;
        int height = type == ArenaElementType.BLOCK ? 4 : 12;
        blueprint.addElement(type, cursorX - width / 2, cursorY - height / 2, width, height);
    }

    public void remove() {
        blueprint.removeClosest(cursorX, cursorY);
    }

    public void clear() {
        blueprint.clear();
    }

    public boolean isValid() {
        return blueprint.isValid();
    }

    public String getShareCode() {
        return blueprint.toShareCode();
    }

    public void render(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(new Color(8, 22, 35));
        g2.fillRect(0, 0, Game.W, Game.H);
        g2.setColor(new Color(50, 120, 160, 80));
        for (int x = 8; x < Game.W; x += 12) {
            g2.drawLine(x, 30, x, Game.H - 16);
        }
        for (int y = 34; y < Game.H - 16; y += 12) {
            g2.drawLine(4, y, Game.W - 4, y);
        }
        g2.setColor(new Color(140, 230, 255));
        g2.drawRect(4, 30, Game.W - 9, Game.H - 47);
        blueprint.render(g2, cursorX, cursorY, getSelectedType());
        g2.setColor(Color.white);
        g2.setFont(new Font("Dialog", Font.BOLD, 7));
        g2.drawString("EDITOR ARENA MUTANTE", 8, 10);
        g2.setFont(new Font("Dialog", Font.PLAIN, 6));
        g2.drawString("ELEMENTO: " + getSelectedType().getLabel(), 8, 20);
        g2.drawString("ELEMENTOS " + blueprint.getElements().size() + "/12  " + (isValid() ? "VALIDA" : "COLOQUE UM ELEMENTO"), 78, 20);
        g2.setColor(new Color(190, 220, 235));
    }
}
