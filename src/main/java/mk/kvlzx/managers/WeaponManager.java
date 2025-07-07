package mk.kvlzx.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.items.CustomItem.ItemType;
import mk.kvlzx.utils.config.CustomConfig;

public class WeaponManager {
    private final Map<UUID, ItemType> selectedWeapon = new HashMap<>();
    private final CustomConfig weaponConfig;
    private final MysthicKnockBack plugin;

    public WeaponManager(MysthicKnockBack plugin) {
        this.plugin = plugin;
        this.weaponConfig = new CustomConfig("weapons.yml", "data", plugin);
        this.weaponConfig.registerConfig();
        loadAllWeapons();
    }

    public ItemType getSelectedWeapon(UUID playerUUID) {
        // Si no está en memoria, intentar cargar desde archivo
        if (!selectedWeapon.containsKey(playerUUID)) {
            loadPlayerWeapon(playerUUID);
        }
        return selectedWeapon.getOrDefault(playerUUID, ItemType.BOW);
    }

    public void setSelectedWeapon(UUID playerUUID, ItemType weapon) {
        selectedWeapon.put(playerUUID, weapon);
        saveWeaponSelection(playerUUID, weapon);
    }

    public void toggleWeapon(UUID playerUUID) {
        ItemType currentWeapon = getSelectedWeapon(playerUUID);
        ItemType newWeapon = (currentWeapon == ItemType.BOW) ? ItemType.SLIME_BALL : ItemType.BOW;
        setSelectedWeapon(playerUUID, newWeapon);
    }

    /**
     * Guarda la selección de arma de un jugador en el archivo de configuración
     */
    private void saveWeaponSelection(UUID playerUUID, ItemType weapon) {
        weaponConfig.getConfig().set("weapons." + playerUUID.toString(), weapon.name());
        weaponConfig.saveConfig();
    }

    /**
     * Carga todas las selecciones de arma desde el archivo de configuración
     */
    private void loadAllWeapons() {
        ConfigurationSection section = weaponConfig.getConfig().getConfigurationSection("weapons");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    String weaponName = section.getString(key);
                    ItemType weapon = ItemType.valueOf(weaponName);
                    selectedWeapon.put(uuid, weapon);
                } catch (IllegalArgumentException e) {
                    // Log del error si el UUID o ItemType no es válido
                    plugin.getLogger().warning("Error loading weapon for UUID: " + key + " - " + e.getMessage());
                } catch (Exception e) {
                    plugin.getLogger().warning("Unexpected error loading weapon for UUID: " + key);
                }
            }
        }
    }

    /**
     * Carga la selección de arma de un jugador específico desde el archivo
     * Útil para cargar datos cuando un jugador se conecta por primera vez en una sesión
     */
    public void loadPlayerWeapon(UUID playerUUID) {
        String weaponName = weaponConfig.getConfig().getString("weapons." + playerUUID.toString());
        if (weaponName != null) {
            try {
                ItemType weapon = ItemType.valueOf(weaponName);
                selectedWeapon.put(playerUUID, weapon);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid weapon for player " + playerUUID + ": " + weaponName);
                // Usar arma por defecto si hay error
                selectedWeapon.put(playerUUID, ItemType.BOW);
            }
        }
    }

    /**
     * Remueve los datos de un jugador (útil para cuando un jugador se desconecta permanentemente)
     */
    public void removePlayerWeapon(UUID playerUUID) {
        selectedWeapon.remove(playerUUID);
        weaponConfig.getConfig().set("weapons." + playerUUID.toString(), null);
        weaponConfig.saveConfig();
    }

    /**
     * Limpia la caché de memoria de un jugador específico
     * Los datos persisten en el archivo
     */
    public void clearPlayerCache(UUID playerUUID) {
        selectedWeapon.remove(playerUUID);
    }

    /**
     * Obtiene todos los jugadores con armas guardadas
     */
    public Set<UUID> getAllPlayersWithWeapons() {
        return new HashSet<>(selectedWeapon.keySet());
    }

    /**
     * Guarda todas las selecciones de arma actuales en el archivo
     * Útil para guardar datos antes de apagar el servidor
     */
    public void saveAllWeapons() {
        for (Map.Entry<UUID, ItemType> entry : selectedWeapon.entrySet()) {
            weaponConfig.getConfig().set("weapons." + entry.getKey().toString(), entry.getValue().name());
        }
        weaponConfig.saveConfig();
    }

    /**
     * Recarga todos los datos desde el archivo
     */
    public void reloadData() {
        weaponConfig.reloadConfig();
        selectedWeapon.clear();
        loadAllWeapons();
    }
}
