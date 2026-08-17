package pong.main;

import java.awt.Color;

public class CosmeticItem {

    public enum Slot {
        PADDLE,
        BALL,
        ARENA,
        TITLE
    }

    private final String id;
    private final String name;
    private final Slot slot;
    private final int unlockXp;
    private final Color primary;
    private final Color secondary;
    private final String description;

    public CosmeticItem(String id, String name, Slot slot, int unlockXp, Color primary, Color secondary,
            String description) {
        this.id = id;
        this.name = name;
        this.slot = slot;
        this.unlockXp = unlockXp;
        this.primary = primary;
        this.secondary = secondary;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Slot getSlot() {
        return slot;
    }

    public int getUnlockXp() {
        return unlockXp;
    }

    public Color getPrimary() {
        return primary;
    }

    public Color getSecondary() {
        return secondary;
    }

    public String getDescription() {
        return description;
    }
}
