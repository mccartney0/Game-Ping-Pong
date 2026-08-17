package pong.main;

import java.awt.Color;

public final class CosmeticCatalog {

    private static final CosmeticItem[] ITEMS = {
            new CosmeticItem("paddle-neon", "Neon Cyan", CosmeticItem.Slot.PADDLE, 0,
                    new Color(70, 220, 255), new Color(35, 120, 220), "Visual inicial do piloto"),
            new CosmeticItem("paddle-solar", "Solar Gold", CosmeticItem.Slot.PADDLE, 100,
                    new Color(255, 220, 90), new Color(230, 120, 30), "Desbloqueada no rank 2"),
            new CosmeticItem("paddle-void", "Void Violet", CosmeticItem.Slot.PADDLE, 250,
                    new Color(210, 120, 255), new Color(90, 35, 180), "Desbloqueada no rank 3"),
            new CosmeticItem("ball-core", "Core White", CosmeticItem.Slot.BALL, 0,
                    new Color(255, 255, 255), new Color(100, 220, 255), "Bola padrao"),
            new CosmeticItem("ball-plasma", "Plasma", CosmeticItem.Slot.BALL, 150,
                    new Color(255, 110, 220), new Color(120, 60, 255), "Bola de energia"),
            new CosmeticItem("ball-ember", "Ember", CosmeticItem.Slot.BALL, 350,
                    new Color(255, 190, 70), new Color(255, 70, 50), "Bola incandescente"),
            new CosmeticItem("arena-grid", "Grid Neon", CosmeticItem.Slot.ARENA, 0,
                    new Color(65, 140, 190), new Color(7, 13, 35), "Arena inicial"),
            new CosmeticItem("arena-sunset", "Sunset Circuit", CosmeticItem.Slot.ARENA, 200,
                    new Color(255, 95, 150), new Color(45, 10, 50), "Arena liberada no rank 3"),
            new CosmeticItem("title-rookie", "PILOTO", CosmeticItem.Slot.TITLE, 0,
                    Color.white, Color.white, "Titulo inicial"),
            new CosmeticItem("title-mutant", "ARQUITETO MUTANTE", CosmeticItem.Slot.TITLE, 300,
                    new Color(120, 255, 180), Color.white, "Titulo de criador de arenas")
    };

    private CosmeticCatalog() {
    }

    public static CosmeticItem[] all() {
        return ITEMS.clone();
    }

    public static CosmeticItem byId(String id) {
        if (id != null) {
            for (CosmeticItem item : ITEMS) {
                if (item.getId().equals(id)) {
                    return item;
                }
            }
        }
        return ITEMS[0];
    }

    public static CosmeticItem[] forSlot(CosmeticItem.Slot slot) {
        int count = 0;
        for (CosmeticItem item : ITEMS) {
            if (item.getSlot() == slot) {
                count++;
            }
        }
        CosmeticItem[] result = new CosmeticItem[count];
        int index = 0;
        for (CosmeticItem item : ITEMS) {
            if (item.getSlot() == slot) {
                result[index++] = item;
            }
        }
        return result;
    }
}
