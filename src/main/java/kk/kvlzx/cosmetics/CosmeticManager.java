package kk.kvlzx.cosmetics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import kk.kvlzx.KvKnockback;
import kk.kvlzx.hotbar.PlayerHotbar;
import kk.kvlzx.utils.config.CustomConfig;

public class CosmeticManager {
    private final KvKnockback plugin;
    private final CustomConfig cosmeticConfig;
    private final Map<UUID, Material> playerBlocks = new HashMap<>();
    private final Map<UUID, Set<Material>> playerOwnedBlocks = new HashMap<>();

    public CosmeticManager(KvKnockback plugin) {
        this.plugin = plugin;
        this.cosmeticConfig = new CustomConfig("cosmetics.yml", "data", plugin);
        this.cosmeticConfig.registerConfig();
        loadCosmetics();
    }

    public void setPlayerBlock(UUID uuid, Material blockType) {
        playerBlocks.put(uuid, blockType);
        savePlayerBlock(uuid);
        
        // Actualizar el layout del jugador
        Player player = plugin.getServer().getPlayer(uuid);
        if (player != null && player.isOnline()) {
            updatePlayerLayout(player);
        }
    }

    public Material getPlayerBlock(UUID uuid) {
        return playerBlocks.getOrDefault(uuid, Material.SANDSTONE);
    }

    private void updatePlayerLayout(Player player) {
        ItemStack[] layout = PlayerHotbar.getPlayerLayout(player.getUniqueId());
        
        // Buscar el slot de bloques comparando todos los bloques decorativos posibles
        int blockSlot = findBlockSlot(layout);
        if (blockSlot != -1) {
            Material newBlock = getPlayerBlock(player.getUniqueId());
            layout[blockSlot] = new ItemStack(newBlock, 64);
            PlayerHotbar.setPlayerLayout(player.getUniqueId(), layout);
        }
    }

    private int findBlockSlot(ItemStack[] layout) {
        Material[] decorativeBlocks = {
            Material.SANDSTONE,
            Material.GLASS,
            Material.STAINED_CLAY,
            Material.QUARTZ_BLOCK,
            Material.SMOOTH_BRICK,
            Material.WOOD,
            Material.WOOL,
            Material.SNOW_BLOCK,
            Material.GLOWSTONE,
            Material.NETHER_BRICK,
            Material.PRISMARINE,
            Material.SEA_LANTERN,
            Material.ENDER_STONE,
            Material.PACKED_ICE,
            Material.SANDSTONE
        };

        for (int i = 0; i < layout.length; i++) {
            if (layout[i] != null) {
                for (Material blockType : decorativeBlocks) {
                    if (layout[i].getType() == blockType) {
                        return i;
                    }
                }
            }
        }
        
        // Si no encontramos ningún bloque decorativo, usar el slot por defecto (1)
        return 1;
    }

    private void savePlayerBlock(UUID uuid) {
        Material block = playerBlocks.get(uuid);
        if (block != null) {
            cosmeticConfig.getConfig().set("blocks." + uuid.toString(), block.name());
            cosmeticConfig.saveConfig();
        }
    }

    public boolean hasPlayerBlock(UUID uuid, Material blockType) {
        if (blockType == Material.SANDSTONE) return true; // Bloque default siempre disponible
        Set<Material> ownedBlocks = playerOwnedBlocks.getOrDefault(uuid, new HashSet<>());
        return ownedBlocks.contains(blockType);
    }

    public void addPlayerBlock(UUID uuid, Material blockType) {
        playerOwnedBlocks.computeIfAbsent(uuid, k -> new HashSet<>()).add(blockType);
        savePlayerBlocks(uuid);
    }

    private void savePlayerBlocks(UUID uuid) {
        Set<Material> blocks = playerOwnedBlocks.get(uuid);
        if (blocks != null) {
            cosmeticConfig.getConfig().set("owned_blocks." + uuid.toString(), blocks.stream()
                .map(Material::name)
                .collect(Collectors.toList()));
            cosmeticConfig.saveConfig();
        }
    }

    public void saveAll() {
        for (Map.Entry<UUID, Material> entry : playerBlocks.entrySet()) {
            cosmeticConfig.getConfig().set("blocks." + entry.getKey().toString(), entry.getValue().name());
        }
        playerOwnedBlocks.forEach((uuid, blocks) -> {
            cosmeticConfig.getConfig().set("owned_blocks." + uuid.toString(), blocks.stream()
                .map(Material::name)
                .collect(Collectors.toList()));
        });
        cosmeticConfig.saveConfig();
    }

    private void loadCosmetics() {
        ConfigurationSection section = cosmeticConfig.getConfig().getConfigurationSection("blocks");
        if (section != null) {
            for (String uuid : section.getKeys(false)) {
                try {
                    UUID playerUUID = UUID.fromString(uuid);
                    Material block = Material.valueOf(section.getString(uuid));
                    playerBlocks.put(playerUUID, block);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Error loading cosmetic block for " + uuid);
                }
            }
        }
        ConfigurationSection ownedSection = cosmeticConfig.getConfig().getConfigurationSection("owned_blocks");
        if (ownedSection != null) {
            for (String uuid : ownedSection.getKeys(false)) {
                try {
                    UUID playerUUID = UUID.fromString(uuid);
                    List<String> blockNames = ownedSection.getStringList(uuid);
                    Set<Material> blocks = blockNames.stream()
                        .map(Material::valueOf)
                        .collect(Collectors.toSet());
                    playerOwnedBlocks.put(playerUUID, blocks);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Error loading owned blocks for " + uuid);
                }
            }
        }
    }
}
