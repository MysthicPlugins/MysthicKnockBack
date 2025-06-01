package mk.kvlzx.cosmetics;

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
import org.bukkit.enchantments.Enchantment;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.hotbar.PlayerHotbar;
import mk.kvlzx.utils.BlockUtils;
import mk.kvlzx.utils.config.CustomConfig;

public class CosmeticManager {
    private final MysthicKnockBack plugin;
    private final CustomConfig cosmeticConfig;
    private final Map<UUID, Material> playerBlocks = new HashMap<>();
    private final Map<UUID, Set<Material>> playerOwnedBlocks = new HashMap<>();
    private final Map<UUID, Material> playerKnockers = new HashMap<>();
    private final Map<UUID, Set<Material>> playerOwnedKnockers = new HashMap<>();

    public CosmeticManager(MysthicKnockBack plugin) {
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

        // Actualizar bloque
        int blockSlot = findItemSlot(layout, "BLOCK");
        if (blockSlot != -1) {
            Material newBlock = getPlayerBlock(player.getUniqueId());
            BlockShopItem shopItem = BlockShopItem.getByMaterial(newBlock);

            ItemStack updated;
            if (shopItem != null) {
                updated = shopItem.createItemStack();
                updated.setAmount(64);
            } else {
                updated = new ItemStack(newBlock, 64);
            }

            layout[blockSlot] = updated;
        }

        // Actualizar knocker
        int knockerSlot = findItemSlot(layout, "KNOCKER");
        if (knockerSlot != -1) {
            Material newKnocker = getPlayerKnocker(player.getUniqueId());
            KnockerShopItem knocker = KnockerShopItem.getByMaterial(newKnocker);

            ItemStack knockerItem;
            if (knocker != null) {
                knockerItem = knocker.createItemStack();
            } else {
                knockerItem = new ItemStack(newKnocker);
            }

            layout[knockerSlot] = knockerItem;
        }

        PlayerHotbar.setPlayerLayout(player.getUniqueId(), layout);
    }

    private int findItemSlot(ItemStack[] layout, String type) {
        for (int i = 0; i < layout.length; i++) {
            if (layout[i] != null) {
                if (type.equals("BLOCK") && BlockUtils.isDecorativeBlock(layout[i].getType())) {
                    return i;
                } else if (type.equals("KNOCKER") && layout[i].getEnchantmentLevel(Enchantment.KNOCKBACK) > 0) {
                    return i;
                }
            }
        }
        return type.equals("BLOCK") ? 1 : 0; // Slots por defecto
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

    public void setPlayerKnocker(UUID uuid, Material knockerType) {
        playerKnockers.put(uuid, knockerType);
        savePlayerKnocker(uuid);
        
        Player player = plugin.getServer().getPlayer(uuid);
        if (player != null && player.isOnline()) {
            updatePlayerLayout(player);
        }
    }

    public Material getPlayerKnocker(UUID uuid) {
        return playerKnockers.getOrDefault(uuid, Material.STICK); // Stick como default
    }

    private void savePlayerKnocker(UUID uuid) {
        Set<Material> ownedKnockers = playerOwnedKnockers.get(uuid);
        if (ownedKnockers != null) {
            cosmeticConfig.getConfig().set("owned_knockers." + uuid.toString(), ownedKnockers.stream()
                .map(Material::name)
                .collect(Collectors.toList()));
            cosmeticConfig.saveConfig();
        }
    }

    public boolean hasPlayerKnocker(UUID uuid, Material knockerType) {
        if (knockerType == Material.STICK) return true; // Knocker default siempre disponible
        Set<Material> ownedKnockers = playerOwnedKnockers.getOrDefault(uuid, new HashSet<>());
        return ownedKnockers.contains(knockerType);
    }

    public void addPlayerKnocker(UUID uuid, Material knockerType) {
        playerOwnedKnockers.computeIfAbsent(uuid, k -> new HashSet<>()).add(knockerType);
        savePlayerKnocker(uuid);
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
        for (Map.Entry<UUID, Material> entry : playerKnockers.entrySet()) {
            cosmeticConfig.getConfig().set("knockers." + entry.getKey().toString(), entry.getValue().name());
        }
        playerOwnedKnockers.forEach((uuid, blocks) -> {
            cosmeticConfig.getConfig().set("owned_knockers." + uuid.toString(), blocks.stream()
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
        ConfigurationSection knockerSection = cosmeticConfig.getConfig().getConfigurationSection("knockers");
        if (knockerSection != null) {
            for (String uuid : knockerSection.getKeys(false)) {
                try {
                    UUID playerUUID = UUID.fromString(uuid);
                    Material block = Material.valueOf(knockerSection.getString(uuid));
                    playerKnockers.put(playerUUID, block);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Error loading cosmetic block for " + uuid);
                }
            }
        }
        ConfigurationSection ownedKnockersSection = cosmeticConfig.getConfig().getConfigurationSection("owned_knockers");
        if (ownedKnockersSection != null) {
            for (String uuid : ownedKnockersSection.getKeys(false)) {
                try {
                    UUID playerUUID = UUID.fromString(uuid);
                    List<String> knockerNames = ownedKnockersSection.getStringList(uuid);
                    Set<Material> knockers = knockerNames.stream()
                        .map(Material::valueOf)
                        .collect(Collectors.toSet());
                    playerOwnedKnockers.put(playerUUID, knockers);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Error loading owned blocks for " + uuid);
                }
            }
        }
    }
}
