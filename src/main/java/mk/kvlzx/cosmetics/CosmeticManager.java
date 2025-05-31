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
    private final Map<UUID, KnockerShopItem> playerKnockers = new HashMap<>();
    private final Map<UUID, Set<KnockerShopItem>> playerOwnedKnockers = new HashMap<>();

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
            KnockerShopItem knocker = getPlayerKnocker(player.getUniqueId());
            if (knocker != null) {
                layout[knockerSlot] = knocker.createItemStack();
            }
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

    public void setPlayerKnocker(UUID uuid, KnockerShopItem knocker) {
        playerKnockers.put(uuid, knocker);
        savePlayerKnocker(uuid);
        
        Player player = plugin.getServer().getPlayer(uuid);
        if (player != null && player.isOnline()) {
            updatePlayerLayout(player);
        }
    }

    public KnockerShopItem getPlayerKnocker(UUID uuid) {
        return playerKnockers.getOrDefault(uuid, 
            KnockerShopItem.getAllKnockers().stream()
                .filter(k -> k.getMaterial() == Material.STICK)
                .findFirst()
                .orElse(null));
    }

    private void savePlayerKnocker(UUID uuid) {
        KnockerShopItem knocker = playerKnockers.get(uuid);
        if (knocker != null) {
            String path = "knockers." + uuid.toString();
            cosmeticConfig.getConfig().set(path + ".material", knocker.getMaterial().name());
            cosmeticConfig.getConfig().set(path + ".data", knocker.getData());
            cosmeticConfig.saveConfig();
        }
    }

    public boolean hasPlayerKnocker(UUID uuid, KnockerShopItem knocker) {
        if (knocker.getMaterial() == Material.STICK) return true; // Knocker default siempre disponible
        Set<KnockerShopItem> ownedKnockers = playerOwnedKnockers.getOrDefault(uuid, new HashSet<>());
        return ownedKnockers.contains(knocker);
    }

    public void addPlayerKnocker(UUID uuid, KnockerShopItem knocker) {
        playerOwnedKnockers.computeIfAbsent(uuid, k -> new HashSet<>()).add(knocker);
        savePlayerKnockers(uuid);
    }

    private void savePlayerKnockers(UUID uuid) {
        Set<KnockerShopItem> knockers = playerOwnedKnockers.get(uuid);
        if (knockers != null) {
            List<String> knockerData = knockers.stream()
                .map(knocker -> knocker.getMaterial().name() + ":" + knocker.getData())
                .collect(Collectors.toList());
            cosmeticConfig.getConfig().set("owned_knockers." + uuid.toString(), knockerData);
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
        playerKnockers.forEach((uuid, knocker) -> {
            String path = "knockers." + uuid.toString();
            cosmeticConfig.getConfig().set(path + ".material", knocker.getMaterial().name());
            cosmeticConfig.getConfig().set(path + ".data", knocker.getData());
        });
        playerOwnedKnockers.forEach((uuid, knockers) -> {
            List<String> knockerData = knockers.stream()
                .map(knocker -> knocker.getMaterial().name() + ":" + knocker.getData())
                .collect(Collectors.toList());
            cosmeticConfig.getConfig().set("owned_knockers." + uuid.toString(), knockerData);
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
                    String materialName = knockerSection.getString(uuid + ".material");
                    byte data = (byte) knockerSection.getInt(uuid + ".data");
                    Material material = Material.valueOf(materialName);
                    KnockerShopItem knocker = KnockerShopItem.getByMaterial(material, data);
                    if (knocker != null) {
                        playerKnockers.put(playerUUID, knocker);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Error loading knocker for " + uuid);
                }
            }
        }
        ConfigurationSection ownedKnockersSection = cosmeticConfig.getConfig().getConfigurationSection("owned_knockers");
        if (ownedKnockersSection != null) {
            for (String uuid : ownedKnockersSection.getKeys(false)) {
                try {
                    UUID playerUUID = UUID.fromString(uuid);
                    List<String> knockerData = ownedKnockersSection.getStringList(uuid);
                    Set<KnockerShopItem> knockers = knockerData.stream()
                        .map(data -> {
                            String[] parts = data.split(":");
                            Material material = Material.valueOf(parts[0]);
                            byte dataByte = Byte.parseByte(parts[1]);
                            return KnockerShopItem.getByMaterial(material, dataByte);
                        })
                        .filter(knocker -> knocker != null)
                        .collect(Collectors.toSet());
                    playerOwnedKnockers.put(playerUUID, knockers);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Error loading owned knockers for " + uuid);
                }
            }
        }
    }
}
