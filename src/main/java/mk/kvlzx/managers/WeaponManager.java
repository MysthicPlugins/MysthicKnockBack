package mk.kvlzx.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import mk.kvlzx.items.CustomItem.ItemType;

public class WeaponManager {
    private final Map<UUID, ItemType> selectedWeapon = new HashMap<>();

    public ItemType getSelectedWeapon(UUID playerUUID) {
        return selectedWeapon.getOrDefault(playerUUID, ItemType.BOW);
    }

    public void setSelectedWeapon(UUID playerUUID, ItemType weapon) {
        selectedWeapon.put(playerUUID, weapon);
    }

    public void toggleWeapon(UUID playerUUID) {
        if (getSelectedWeapon(playerUUID) == ItemType.BOW) {
            setSelectedWeapon(playerUUID, ItemType.SLIME_BALL);
        } else {
            setSelectedWeapon(playerUUID, ItemType.BOW);
        }
    }
}
