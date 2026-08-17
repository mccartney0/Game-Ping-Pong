package pong.main;

public class Inventory {

    private final Stats stats;

    public Inventory(Stats stats) {
        this.stats = stats;
    }

    public boolean isUnlocked(CosmeticItem item) {
        return item != null && stats != null && stats.xp >= item.getUnlockXp();
    }

    public CosmeticItem equipped(CosmeticItem.Slot slot) {
        if (stats == null) {
            return CosmeticCatalog.forSlot(slot)[0];
        }
        String id = stats.getEquippedItemId(slot);
        CosmeticItem item = CosmeticCatalog.byId(id);
        if (item.getSlot() != slot || !isUnlocked(item)) {
            return CosmeticCatalog.forSlot(slot)[0];
        }
        return item;
    }

    public void cycle(CosmeticItem.Slot slot, int direction) {
        CosmeticItem[] items = CosmeticCatalog.forSlot(slot);
        if (items.length == 0 || stats == null) {
            return;
        }
        CosmeticItem current = equipped(slot);
        int currentIndex = 0;
        for (int i = 0; i < items.length; i++) {
            if (items[i].getId().equals(current.getId())) {
                currentIndex = i;
                break;
            }
        }
        for (int step = 1; step <= items.length; step++) {
            int index = (currentIndex + direction * step % items.length + items.length) % items.length;
            if (isUnlocked(items[index])) {
                stats.setEquippedItemId(slot, items[index].getId());
                stats.save();
                return;
            }
        }
    }

    public String getStatus(CosmeticItem item) {
        if (isUnlocked(item)) {
            return "DESBLOQUEADO";
        }
        return "XP " + item.getUnlockXp();
    }
}
