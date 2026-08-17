package pong.main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

public class ArenaBlueprint {

    private final List<ArenaElement> elements = new ArrayList<ArenaElement>();
    private String name = "Minha Arena";

    public ArenaBlueprint() {
        addElement(ArenaElementType.BLOCK, 60, 58, 40, 4);
        addElement(ArenaElementType.TURBO, 24, 50, 18, 14);
        addElement(ArenaElementType.PORTAL, 118, 50, 12, 14);
    }

    public ArenaBlueprint(String name) {
        this.name = name == null || name.trim().isEmpty() ? "Minha Arena" : name.trim();
    }

    public List<ArenaElement> getElements() {
        return Collections.unmodifiableList(elements);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
    }

    public void addElement(ArenaElementType type, int x, int y, int width, int height) {
        if (type == null || elements.size() >= 12) {
            return;
        }
        int safeWidth = Math.max(4, Math.min(54, width));
        int safeHeight = Math.max(3, Math.min(22, height));
        int safeX = Math.max(4, Math.min(Game.W - safeWidth - 4, x));
        int safeY = Math.max(32, Math.min(Game.H - safeHeight - 18, y));
        elements.add(new ArenaElement(type, safeX, safeY, safeWidth, safeHeight));
    }

    public void removeClosest(int x, int y) {
        ArenaElement closest = null;
        double distance = Double.MAX_VALUE;
        for (ArenaElement element : elements) {
            double dx = element.x + element.width / 2.0 - x;
            double dy = element.y + element.height / 2.0 - y;
            double current = dx * dx + dy * dy;
            if (current < distance) {
                distance = current;
                closest = element;
            }
        }
        if (closest != null && distance < 22 * 22) {
            elements.remove(closest);
        }
    }

    public void clear() {
        elements.clear();
    }

    public boolean isValid() {
        if (elements.isEmpty()) {
            return false;
        }
        for (ArenaElement element : elements) {
            Rectangle bounds = element.bounds();
            if (bounds.x < 4 || bounds.y < 30 || bounds.x + bounds.width > Game.W - 4
                    || bounds.y + bounds.height > Game.H - 16) {
                return false;
            }
        }
        return true;
    }

    public String toShareCode() {
        StringBuilder builder = new StringBuilder();
        builder.append(name.replace("|", " ")).append('|');
        for (ArenaElement element : elements) {
            builder.append(element.type.name()).append(',').append(element.x).append(',').append(element.y).append(',')
                    .append(element.width).append(',').append(element.height).append(';');
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(builder.toString().getBytes(StandardCharsets.UTF_8));
    }

    public static ArenaBlueprint fromShareCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(code.trim()), StandardCharsets.UTF_8);
            String[] sections = decoded.split("\\|", 2);
            ArenaBlueprint blueprint = new ArenaBlueprint(sections[0]);
            if (sections.length == 2) {
                String[] rawElements = sections[1].split(";");
                for (String raw : rawElements) {
                    String[] values = raw.split(",");
                    if (values.length != 5) {
                        continue;
                    }
                    blueprint.addElement(ArenaElementType.valueOf(values[0]), Integer.parseInt(values[1]),
                            Integer.parseInt(values[2]), Integer.parseInt(values[3]), Integer.parseInt(values[4]));
                }
            }
            return blueprint.isValid() ? blueprint : null;
        } catch (Exception exception) {
            return null;
        }
    }

    public void renderElements(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        for (ArenaElement element : elements) {
            Color color = colorFor(element.type);
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 70));
            g2.fillRoundRect(element.x - 2, element.y - 2, element.width + 4, element.height + 4, 6, 6);
            g2.setColor(color);
            g2.fillRoundRect(element.x, element.y, element.width, element.height, 4, 4);
            g2.setColor(Color.white);
            g2.drawRoundRect(element.x, element.y, element.width - 1, element.height - 1, 4, 4);
        }
    }

    public void render(Graphics g, int cursorX, int cursorY, ArenaElementType selectedType) {
        Graphics2D g2 = (Graphics2D) g;
        renderElements(g2);
        int previewWidth = selectedType == ArenaElementType.BLOCK ? 26 : 14;
        int previewHeight = selectedType == ArenaElementType.BLOCK ? 4 : 12;
        Color cursorColor = colorFor(selectedType);
        g2.setColor(new Color(cursorColor.getRed(), cursorColor.getGreen(), cursorColor.getBlue(), 90));
        g2.fillRoundRect(cursorX - previewWidth / 2, cursorY - previewHeight / 2, previewWidth, previewHeight, 4, 4);
        g2.setColor(Color.white);
        g2.drawRect(cursorX - 2, cursorY - 2, 4, 4);
    }

    private Color colorFor(ArenaElementType type) {
        switch (type) {
        case BLOCK:
            return new Color(255, 90, 200);
        case TURBO:
            return new Color(255, 210, 70);
        case SLOW:
            return new Color(100, 180, 255);
        case PORTAL:
            return new Color(180, 110, 255);
        case GRAVITY:
            return new Color(100, 255, 180);
        default:
            return Color.white;
        }
    }
}
