package mk.kvlzx.cosmetics;

import java.util.ArrayList;
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
import mk.kvlzx.config.KnockersShopConfig;
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
    private final Map<UUID, String> playerDeathMessages = new HashMap<>();
    private final Map<UUID, Set<String>> playerOwnedMessages = new HashMap<>();
    private final Map<UUID, String> playerKillMessages = new HashMap<>();
    private final Map<UUID, Set<String>> playerOwnedKillMessages = new HashMap<>();
    private final Map<UUID, String> playerArrowEffects = new HashMap<>();
    private final Map<UUID, Set<String>> playerOwnedArrowEffects = new HashMap<>();
    private final Map<UUID, String> playerDeathSounds = new HashMap<>();
    private final Map<UUID, Set<String>> playerOwnedDeathSounds = new HashMap<>();
    private final Map<UUID, String> playerKillSounds = new HashMap<>();
    private final Map<UUID, Set<String>> playerOwnedKillSounds = new HashMap<>();
    private final Map<UUID, String> playerBackgroundMusic = new HashMap<>();
    private final Map<UUID, Set<String>> playerOwnedMusic = new HashMap<>();
    private final Map<UUID, String> playerJoinMessages = new HashMap<>();
    private final Map<UUID, Set<String>> playerOwnedJoinMessages = new HashMap<>();

    public CosmeticManager(MysthicKnockBack plugin) {
        this.plugin = plugin;
        this.cosmeticConfig = new CustomConfig("cosmetics.yml", "data", plugin);
        this.cosmeticConfig.registerConfig();
        loadCosmetics();
    }

    public void setPlayerBlock(UUID uuid, Material blockType) {
        playerBlocks.put(uuid, blockType);
        savePlayerBlock(uuid);
        
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

        int blockSlot = findItemSlot(layout, "BLOCK");
        if (blockSlot != -1) {
            Material newBlock = getPlayerBlock(player.getUniqueId());
            ItemStack updated = BlockUtils.createBlockItem(newBlock, player.getUniqueId());
            layout[blockSlot] = updated;
        }

        int knockerSlot = findItemSlot(layout, "KNOCKER");
        if (knockerSlot != -1) {
            Material newKnocker = getPlayerKnocker(player.getUniqueId());
            ItemStack knockerItem = plugin.getKnockersShopConfig().createKnockerItem(newKnocker);
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
        if (blockType == Material.SANDSTONE) return true;
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
        return playerKnockers.getOrDefault(uuid, Material.STICK);
    }

    private void savePlayerKnocker(UUID uuid) {
        Material knocker = playerKnockers.get(uuid);
        if (knocker != null) {
            cosmeticConfig.getConfig().set("knockers." + uuid.toString(), knocker.name());
            cosmeticConfig.saveConfig();
        }
    }

    public boolean hasPlayerKnocker(UUID uuid, Material knockerType) {
        KnockersShopConfig.KnockerItem defaultKnocker = plugin.getKnockersShopConfig().getKnockerItems().values().stream().filter(KnockersShopConfig.KnockerItem::isDefault).findFirst().orElse(null);
        if (defaultKnocker != null && knockerType == defaultKnocker.getMaterial()) {
            return true;
        }
        Set<Material> ownedKnockers = playerOwnedKnockers.getOrDefault(uuid, new HashSet<>());
        return ownedKnockers.contains(knockerType);
    }

    public void addPlayerKnocker(UUID uuid, Material knockerType) {
        playerOwnedKnockers.computeIfAbsent(uuid, k -> new HashSet<>()).add(knockerType);
        savePlayerOwnedKnockers(uuid);
    }

    private void savePlayerOwnedKnockers(UUID uuid) {
        Set<Material> knockers = playerOwnedKnockers.get(uuid);
        if (knockers != null) {
            cosmeticConfig.getConfig().set("owned_knockers." + uuid.toString(), knockers.stream()
                .map(Material::name)
                .collect(Collectors.toList()));
            cosmeticConfig.saveConfig();
        }
    }

    public String getPlayerDeathMessage(UUID uuid) {
        return playerDeathMessages.getOrDefault(uuid, "default");
    }

    public void setPlayerDeathMessage(UUID uuid, String messageName) {
        playerDeathMessages.put(uuid, messageName);
        savePlayerDeathMessage(uuid);
    }

    public boolean hasPlayerDeathMessage(UUID uuid, String messageName) {
        if (messageName.equals("default")) return true;
        Set<String> ownedMessages = playerOwnedMessages.getOrDefault(uuid, new HashSet<>());
        return ownedMessages.contains(messageName);
    }

    public void addPlayerDeathMessage(UUID uuid, String messageName) {
        playerOwnedMessages.computeIfAbsent(uuid, k -> new HashSet<>()).add(messageName);
        savePlayerDeathMessages(uuid);
    }

    private void savePlayerDeathMessage(UUID uuid) {
        String message = playerDeathMessages.get(uuid);
        if (message != null) {
            cosmeticConfig.getConfig().set("death_messages." + uuid.toString(), message);
            cosmeticConfig.saveConfig();
        }
    }

    private void savePlayerDeathMessages(UUID uuid) {
        Set<String> messages = playerOwnedMessages.get(uuid);
        if (messages != null) {
            cosmeticConfig.getConfig().set("owned_messages." + uuid.toString(), 
                new ArrayList<>(messages));
            cosmeticConfig.saveConfig();
        }
    }

    public String getPlayerKillMessage(UUID uuid) {
        return playerKillMessages.getOrDefault(uuid, "default");
    }

    public void setPlayerKillMessage(UUID uuid, String messageName) {
        playerKillMessages.put(uuid, messageName);
        savePlayerKillMessage(uuid);
    }

    public boolean hasPlayerKillMessage(UUID uuid, String messageName) {
        if (messageName.equals("default")) return true;
        Set<String> owned = playerOwnedKillMessages.getOrDefault(uuid, new HashSet<>());
        return owned.contains(messageName);
    }

    public void addPlayerKillMessage(UUID uuid, String messageName) {
        playerOwnedKillMessages.computeIfAbsent(uuid, k -> new HashSet<>()).add(messageName);
        savePlayerKillMessages(uuid);
    }

    private void savePlayerKillMessage(UUID uuid) {
        String message = playerKillMessages.get(uuid);
        if (message != null) {
            cosmeticConfig.getConfig().set("kill_messages." + uuid.toString(), message);
            cosmeticConfig.saveConfig();
        }
    }

    private void savePlayerKillMessages(UUID uuid) {
        Set<String> messages = playerOwnedKillMessages.get(uuid);
        if (messages != null) {
            cosmeticConfig.getConfig().set("owned_kill_messages." + uuid.toString(), 
                new ArrayList<>(messages));
            cosmeticConfig.saveConfig();
        }
    }

    public String getPlayerArrowEffect(UUID uuid) {
        return playerArrowEffects.getOrDefault(uuid, "none");
    }

    public void setPlayerArrowEffect(UUID uuid, String effectName) {
        playerArrowEffects.put(uuid, effectName);
        savePlayerArrowEffect(uuid);
    }

    public boolean hasPlayerArrowEffect(UUID uuid, String effectName) {
        if (effectName.equals("none")) return true;
        Set<String> owned = playerOwnedArrowEffects.getOrDefault(uuid, new HashSet<>());
        return owned.contains(effectName);
    }

    public void addPlayerArrowEffect(UUID uuid, String effectName) {
        playerOwnedArrowEffects.computeIfAbsent(uuid, k -> new HashSet<>()).add(effectName);
        savePlayerArrowEffects(uuid);
    }

    private void savePlayerArrowEffect(UUID uuid) {
        String effect = playerArrowEffects.get(uuid);
        if (effect != null) {
            cosmeticConfig.getConfig().set("arrow_effects." + uuid.toString(), effect);
            cosmeticConfig.saveConfig();
        }
    }

    private void savePlayerArrowEffects(UUID uuid) {
        Set<String> effects = playerOwnedArrowEffects.get(uuid);
        if (effects != null) {
            cosmeticConfig.getConfig().set("owned_arrow_effects." + uuid.toString(), 
                new ArrayList<>(effects));
            cosmeticConfig.saveConfig();
        }
    }

    public String getPlayerDeathSound(UUID uuid) {
        return playerDeathSounds.getOrDefault(uuid, "none");
    }

    public void setPlayerDeathSound(UUID uuid, String soundName) {
        playerDeathSounds.put(uuid, soundName);
        savePlayerDeathSound(uuid);
    }

    public boolean hasPlayerDeathSound(UUID uuid, String soundName) {
        if (soundName.equals("none")) return true;
        Set<String> owned = playerOwnedDeathSounds.getOrDefault(uuid, new HashSet<>());
        return owned.contains(soundName);
    }

    public void addPlayerDeathSound(UUID uuid, String soundName) {
        playerOwnedDeathSounds.computeIfAbsent(uuid, k -> new HashSet<>()).add(soundName);
        savePlayerDeathSounds(uuid);
    }

    private void savePlayerDeathSound(UUID uuid) {
        String sound = playerDeathSounds.get(uuid);
        if (sound != null) {
            cosmeticConfig.getConfig().set("death_sounds." + uuid.toString(), sound);
            cosmeticConfig.saveConfig();
        }
    }

    private void savePlayerDeathSounds(UUID uuid) {
        Set<String> sounds = playerOwnedDeathSounds.get(uuid);
        if (sounds != null) {
            cosmeticConfig.getConfig().set("owned_death_sounds." + uuid.toString(), 
                new ArrayList<>(sounds));
            cosmeticConfig.saveConfig();
        }
    }

    public String getPlayerKillSound(UUID uuid) {
        return playerKillSounds.getOrDefault(uuid, "none");
    }

    public void setPlayerKillSound(UUID uuid, String soundName) {
        playerKillSounds.put(uuid, soundName);
        savePlayerKillSound(uuid);
    }

    public boolean hasPlayerKillSound(UUID uuid, String soundName) {
        if (soundName.equals("none")) return true;
        Set<String> owned = playerOwnedKillSounds.getOrDefault(uuid, new HashSet<>());
        return owned.contains(soundName);
    }

    public void addPlayerKillSound(UUID uuid, String soundName) {
        playerOwnedKillSounds.computeIfAbsent(uuid, k -> new HashSet<>()).add(soundName);
        savePlayerKillSounds(uuid);
    }

    private void savePlayerKillSound(UUID uuid) {
        String sound = playerKillSounds.get(uuid);
        if (sound != null) {
            cosmeticConfig.getConfig().set("kill_sounds." + uuid.toString(), sound);
            cosmeticConfig.saveConfig();
        }
    }

    private void savePlayerKillSounds(UUID uuid) {
        Set<String> sounds = playerOwnedKillSounds.get(uuid);
        if (sounds != null) {
            cosmeticConfig.getConfig().set("owned_kill_sounds." + uuid.toString(), 
                new ArrayList<>(sounds));
            cosmeticConfig.saveConfig();
        }
    }

    public String getPlayerBackgroundMusic(UUID uuid) {
        return playerBackgroundMusic.getOrDefault(uuid, "none");
    }

    public void setPlayerBackgroundMusic(UUID uuid, String musicName) {
        playerBackgroundMusic.put(uuid, musicName);
        savePlayerBackgroundMusic(uuid);
    }

    public boolean hasPlayerBackgroundMusic(UUID uuid, String musicName) {
        if (musicName.equals("none")) return true;
        Set<String> owned = playerOwnedMusic.getOrDefault(uuid, new HashSet<>());
        return owned.contains(musicName);
    }

    public void addPlayerBackgroundMusic(UUID uuid, String musicName) {
        playerOwnedMusic.computeIfAbsent(uuid, k -> new HashSet<>()).add(musicName);
        savePlayerOwnedMusic(uuid);
    }

    private void savePlayerBackgroundMusic(UUID uuid) {
        String music = playerBackgroundMusic.get(uuid);
        if (music != null) {
            cosmeticConfig.getConfig().set("background_music." + uuid.toString(), music);
            cosmeticConfig.saveConfig();
        }
    }

    private void savePlayerOwnedMusic(UUID uuid) {
        Set<String> music = playerOwnedMusic.get(uuid);
        if (music != null) {
            cosmeticConfig.getConfig().set("owned_background_music." + uuid.toString(), 
                new ArrayList<>(music));
            cosmeticConfig.saveConfig();
        }
    }

    public String getPlayerJoinMessage(UUID uuid) {
        return playerJoinMessages.getOrDefault(uuid, "default");
    }

    public void setPlayerJoinMessage(UUID uuid, String messageName) {
        playerJoinMessages.put(uuid, messageName);
        savePlayerJoinMessage(uuid);
    }

    public boolean hasPlayerJoinMessage(UUID uuid, String messageName) {
        if (messageName.equals("default")) return true;
        Set<String> owned = playerOwnedJoinMessages.getOrDefault(uuid, new HashSet<>());
        return owned.contains(messageName);
    }

    public void addPlayerJoinMessage(UUID uuid, String messageName) {
        playerOwnedJoinMessages.computeIfAbsent(uuid, k -> new HashSet<>()).add(messageName);
        savePlayerJoinMessages(uuid);
    }

    private void savePlayerJoinMessage(UUID uuid) {
        String message = playerJoinMessages.get(uuid);
        if (message != null) {
            cosmeticConfig.getConfig().set("join_messages." + uuid.toString(), message);
            cosmeticConfig.saveConfig();
        }
    }

    private void savePlayerJoinMessages(UUID uuid) {
        Set<String> messages = playerOwnedJoinMessages.get(uuid);
        if (messages != null) {
            cosmeticConfig.getConfig().set("owned_join_messages." + uuid.toString(), new ArrayList<>(messages));
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
        for (Map.Entry<UUID, Material> entry : playerKnockers.entrySet()) {
            cosmeticConfig.getConfig().set("knockers." + entry.getKey().toString(), entry.getValue().name());
        }
        playerOwnedKnockers.forEach((uuid, knockers) -> {
            cosmeticConfig.getConfig().set("owned_knockers." + uuid.toString(), knockers.stream()
                .map(Material::name)
                .collect(Collectors.toList()));
        });
        for (Map.Entry<UUID, String> entry : playerDeathMessages.entrySet()) {
            cosmeticConfig.getConfig().set("death_messages." + entry.getKey().toString(), 
                entry.getValue());
        }
        playerOwnedMessages.forEach((uuid, messages) -> {
            cosmeticConfig.getConfig().set("owned_messages." + uuid.toString(), 
                new ArrayList<>(messages));
        });
        for (Map.Entry<UUID, String> entry : playerKillMessages.entrySet()) {
            cosmeticConfig.getConfig().set("kill_messages." + entry.getKey().toString(), 
                entry.getValue());
        }
        playerOwnedKillMessages.forEach((uuid, messages) -> {
            cosmeticConfig.getConfig().set("owned_kill_messages." + uuid.toString(), 
                new ArrayList<>(messages));
        });
        for (Map.Entry<UUID, String> entry : playerArrowEffects.entrySet()) {
            cosmeticConfig.getConfig().set("arrow_effects." + entry.getKey().toString(), 
                entry.getValue());
        }
        playerOwnedArrowEffects.forEach((uuid, effects) -> {
            cosmeticConfig.getConfig().set("owned_arrow_effects." + uuid.toString(), 
                new ArrayList<>(effects));
        });
        for (Map.Entry<UUID, String> entry : playerDeathSounds.entrySet()) {
            cosmeticConfig.getConfig().set("death_sounds." + entry.getKey().toString(), 
                entry.getValue());
        }
        playerOwnedDeathSounds.forEach((uuid, sounds) -> {
            cosmeticConfig.getConfig().set("owned_death_sounds." + uuid.toString(), 
                new ArrayList<>(sounds));
        });
        for (Map.Entry<UUID, String> entry : playerKillSounds.entrySet()) {
            cosmeticConfig.getConfig().set("kill_sounds." + entry.getKey().toString(), 
                entry.getValue());
        }
        playerOwnedKillSounds.forEach((uuid, sounds) -> {
            cosmeticConfig.getConfig().set("owned_kill_sounds." + uuid.toString(), 
                new ArrayList<>(sounds));
        });
        for (Map.Entry<UUID, String> entry : playerBackgroundMusic.entrySet()) {
            cosmeticConfig.getConfig().set("background_music." + entry.getKey().toString(), 
                entry.getValue());
        }
        playerOwnedMusic.forEach((uuid, music) -> {
            cosmeticConfig.getConfig().set("owned_background_music." + uuid.toString(), 
                new ArrayList<>(music));
        });
        for (Map.Entry<UUID, String> entry : playerJoinMessages.entrySet()) {
            cosmeticConfig.getConfig().set("join_messages." + entry.getKey().toString(), entry.getValue());
        }
        playerOwnedJoinMessages.forEach((uuid, messages) -> {
            cosmeticConfig.getConfig().set("owned_join_messages." + uuid.toString(), new ArrayList<>(messages));
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
                    Material knocker = Material.valueOf(knockerSection.getString(uuid));
                    playerKnockers.put(playerUUID, knocker);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Error loading cosmetic knocker for " + uuid);
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
                    plugin.getLogger().warning("Error loading owned knockers for " + uuid);
                }
            }
        }
        ConfigurationSection messageSection = cosmeticConfig.getConfig().getConfigurationSection("death_messages");
        if (messageSection != null) {
            for (String uuid : messageSection.getKeys(false)) {
                try {
                    UUID playerUUID = UUID.fromString(uuid);
                    String message = messageSection.getString(uuid);
                    playerDeathMessages.put(playerUUID, message);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Error loading death message for " + uuid);
                }
            }
        }

        ConfigurationSection ownedMessagesSection = cosmeticConfig.getConfig().getConfigurationSection("owned_messages");
        if (ownedMessagesSection != null) {
            for (String uuid : ownedMessagesSection.getKeys(false)) {
                try {
                    UUID playerUUID = UUID.fromString(uuid);
                    List<String> messageNames = ownedMessagesSection.getStringList(uuid);
                    playerOwnedMessages.put(playerUUID, new HashSet<>(messageNames));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Error loading owned messages for " + uuid);
                }
            }
        }
        ConfigurationSection killMessageSection = cosmeticConfig.getConfig().getConfigurationSection("kill_messages");
        if (killMessageSection != null) {
            for (String uuid : killMessageSection.getKeys(false)) {
                try {
                    UUID playerUUID = UUID.fromString(uuid);
                    String message = killMessageSection.getString(uuid);
                    playerKillMessages.put(playerUUID, message);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Error loading kill message for " + uuid);
                }
            }
        }

        ConfigurationSection ownedKillMessagesSection = cosmeticConfig.getConfig().getConfigurationSection("owned_kill_messages");
        if (ownedKillMessagesSection != null) {
            for (String uuid : ownedKillMessagesSection.getKeys(false)) {
                try {
                    UUID playerUUID = UUID.fromString(uuid);
                    List<String> messageNames = ownedKillMessagesSection.getStringList(uuid);
                    playerOwnedKillMessages.put(playerUUID, new HashSet<>(messageNames));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Error loading owned kill messages for " + uuid);
                }
            }
        }

        ConfigurationSection arrowEffectSection = cosmeticConfig.getConfig().getConfigurationSection("arrow_effects");
        if (arrowEffectSection != null) {
            for (String uuid : arrowEffectSection.getKeys(false)) {
                try {
                    UUID playerUUID = UUID.fromString(uuid);
                    String effect = arrowEffectSection.getString(uuid);
                    playerArrowEffects.put(playerUUID, effect);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Error loading arrow effect for " + uuid);
                }
            }
        }
        
        ConfigurationSection ownedArrowEffectsSection = cosmeticConfig.getConfig().getConfigurationSection("owned_arrow_effects");
        if (ownedArrowEffectsSection != null) {
            for (String uuid : ownedArrowEffectsSection.getKeys(false)) {
                try {
                    UUID playerUUID = UUID.fromString(uuid);
                    List<String> effectNames = ownedArrowEffectsSection.getStringList(uuid);
                    playerOwnedArrowEffects.put(playerUUID, new HashSet<>(effectNames));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Error loading owned arrow effects for " + uuid);
                }
            }
        }

        ConfigurationSection deathSoundSection = cosmeticConfig.getConfig().getConfigurationSection("death_sounds");
        if (deathSoundSection != null) {
            for (String uuid : deathSoundSection.getKeys(false)) {
                try {
                    UUID playerUUID = UUID.fromString(uuid);
                    String sound = deathSoundSection.getString(uuid);
                    playerDeathSounds.put(playerUUID, sound);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Error loading death sound for " + uuid);
                }
            }
        }
        
        ConfigurationSection ownedDeathSoundsSection = cosmeticConfig.getConfig().getConfigurationSection("owned_death_sounds");
        if (ownedDeathSoundsSection != null) {
            for (String uuid : ownedDeathSoundsSection.getKeys(false)) {
                try {
                    UUID playerUUID = UUID.fromString(uuid);
                    List<String> soundNames = ownedDeathSoundsSection.getStringList(uuid);
                    playerOwnedDeathSounds.put(playerUUID, new HashSet<>(soundNames));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Error loading owned death sounds for " + uuid);
                }
            }
        }

        ConfigurationSection killSoundSection = cosmeticConfig.getConfig().getConfigurationSection("kill_sounds");
        if (killSoundSection != null) {
            for (String uuid : killSoundSection.getKeys(false)) {
                try {
                    UUID playerUUID = UUID.fromString(uuid);
                    String sound = killSoundSection.getString(uuid);
                    playerKillSounds.put(playerUUID, sound);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Error loading kill sound for " + uuid);
                }
            }
        }
        
        ConfigurationSection ownedKillSoundsSection = cosmeticConfig.getConfig().getConfigurationSection("owned_kill_sounds");
        if (ownedKillSoundsSection != null) {
            for (String uuid : ownedKillSoundsSection.getKeys(false)) {
                try {
                    UUID playerUUID = UUID.fromString(uuid);
                    List<String> soundNames = ownedKillSoundsSection.getStringList(uuid);
                    playerOwnedKillSounds.put(playerUUID, new HashSet<>(soundNames));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Error loading owned kill sounds for " + uuid);
                }
            }
        }

        ConfigurationSection backgroundMusicSection = cosmeticConfig.getConfig().getConfigurationSection("background_music");
        if (backgroundMusicSection != null) {
            for (String uuid : backgroundMusicSection.getKeys(false)) {
                try {
                    UUID playerUUID = UUID.fromString(uuid);
                    String music = backgroundMusicSection.getString(uuid);
                    playerBackgroundMusic.put(playerUUID, music);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Error loading background music for " + uuid);
                }
            }
        }
        
        ConfigurationSection ownedMusicSection = cosmeticConfig.getConfig().getConfigurationSection("owned_background_music");
        if (ownedMusicSection != null) {
            for (String uuid : ownedMusicSection.getKeys(false)) {
                try {
                    UUID playerUUID = UUID.fromString(uuid);
                    List<String> musicNames = ownedMusicSection.getStringList(uuid);
                    playerOwnedMusic.put(playerUUID, new HashSet<>(musicNames));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Error loading owned background music for " + uuid);
                }
            }
        }

        ConfigurationSection joinMessageSection = cosmeticConfig.getConfig().getConfigurationSection("join_messages");
        if (joinMessageSection != null) {
            for (String uuid : joinMessageSection.getKeys(false)) {
                try {
                    UUID playerUUID = UUID.fromString(uuid);
                    String message = joinMessageSection.getString(uuid);
                    playerJoinMessages.put(playerUUID, message);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Error loading join message for " + uuid);
                }
            }
        }
        ConfigurationSection ownedJoinMessagesSection = cosmeticConfig.getConfig().getConfigurationSection("owned_join_messages");
        if (ownedJoinMessagesSection != null) {
            for (String uuid : ownedJoinMessagesSection.getKeys(false)) {
                try {
                    UUID playerUUID = UUID.fromString(uuid);
                    List<String> messageNames = ownedJoinMessagesSection.getStringList(uuid);
                    playerOwnedJoinMessages.put(playerUUID, new HashSet<>(messageNames));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Error loading owned join messages for " + uuid);
                }
            }
        }
    }
}