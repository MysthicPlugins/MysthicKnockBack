package mk.kvlzx.items;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.config.MainConfig;
import mk.kvlzx.utils.MessageUtils;

public class CustomItem {
    
    public enum ItemType {
        KNOCKER,
        BOW,
        ARROW,
        PEARL,
        FEATHER,
        PLATE,
        BLOCKS;
    }

    public static ItemStack create(ItemType type) {
        MainConfig config = MysthicKnockBack.getInstance().getMainConfig();
        
        ItemStack item;
        ItemMeta meta;
        
        switch (type) {
            case KNOCKER:
                item = new ItemStack(Material.valueOf(config.getKnockerId()));
                meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(MessageUtils.getColor(config.getKnockerName()));
                    if (config.getKnockerLore() != null) {
                        meta.setLore(Arrays.asList(MessageUtils.getColor(config.getKnockerLore())));
                    }
                    if (config.getKnockerKnockback()) {
                        meta.addEnchant(Enchantment.KNOCKBACK, config.getKnockerKnockbackLevel(), true);
                    }
                    item.setItemMeta(meta);
                }
                break;
                
            case BOW:
                item = new ItemStack(Material.valueOf(config.getBowId()));
                meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(MessageUtils.getColor(config.getBowName()));
                    if (config.getBowLore() != null) {
                        meta.setLore(Arrays.asList(MessageUtils.getColor(config.getBowLore())));
                    }
                    if (config.getBowKnockback()) {
                        meta.addEnchant(Enchantment.ARROW_KNOCKBACK, config.getBowKnockbackLevel(), true);
                    }
                    item.setItemMeta(meta);
                }
                break;
                
            case ARROW:
                item = new ItemStack(Material.valueOf(config.getArrowId()));
                meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(MessageUtils.getColor(config.getArrowName()));
                    if (config.getArrowLore() != null) {
                        meta.setLore(Arrays.asList(MessageUtils.getColor(config.getArrowLore())));
                    }
                    if (config.getArrowKnockback()) {
                        meta.addEnchant(Enchantment.KNOCKBACK, config.getArrowKnockbackLevel(), true);
                    }
                    item.setItemMeta(meta);
                }
                break;
                
            case PEARL:
                item = new ItemStack(Material.valueOf(config.getPearlId()));
                meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(MessageUtils.getColor(config.getPearlName()));
                    if (config.getPearlLore() != null) {
                        meta.setLore(Arrays.asList(MessageUtils.getColor(config.getPearlLore())));
                    }
                    if (config.getPearlKnockback()) {
                        meta.addEnchant(Enchantment.KNOCKBACK, config.getPearlKnockbackLevel(), true);
                    }
                    item.setItemMeta(meta);
                }
                break;
                
            case FEATHER:
                item = new ItemStack(Material.valueOf(config.getFeatherId()));
                meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(MessageUtils.getColor(config.getFeatherName()));
                    if (config.getFeatherLore() != null) {
                        meta.setLore(Arrays.asList(MessageUtils.getColor(config.getFeatherLore())));
                    }
                    if (config.getFeatherKnockback()) {
                        meta.addEnchant(Enchantment.KNOCKBACK, config.getFeatherKnockbackLevel(), true);
                    }
                    item.setItemMeta(meta);
                }
                break;
                
            case PLATE:
                item = new ItemStack(Material.valueOf(config.getPlateId()));
                meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(MessageUtils.getColor(config.getPlateName()));
                    if (config.getPlateLore() != null) {
                        meta.setLore(Arrays.asList(MessageUtils.getColor(config.getPlateLore())));
                    }
                    if (config.getPlateKnockback()) {
                        meta.addEnchant(Enchantment.KNOCKBACK, config.getPlateKnockbackLevel(), true);
                    }
                    item.setItemMeta(meta);
                }
                break;
                
            case BLOCKS:
                item = new ItemStack(Material.valueOf(config.getBlocksId()));
                meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(MessageUtils.getColor(config.getBlocksName()));
                    if (config.getBlocksLore() != null) {
                        meta.setLore(Arrays.asList(MessageUtils.getColor(config.getBlocksLore())));
                    }
                    if (config.getBlocksKnockback()) {
                        meta.addEnchant(Enchantment.KNOCKBACK, config.getBlocksKnockbackLevel(), true);
                    }
                    item.setItemMeta(meta);
                }
                // Los bloques siempre vienen en cantidad de 64
                item.setAmount(64);
                break;
                
            default:
                throw new IllegalArgumentException("Unknown item type: " + type);
        }

        return item;
    }

    public static ItemStack createSkull(Player player, String displayName, String lore) {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        
        meta.setOwner(player.getName());
        meta.setDisplayName(MessageUtils.getColor(displayName));
        meta.setLore(Arrays.asList(MessageUtils.getColor(lore)));
        
        skull.setItemMeta(meta);
        return skull;
    }

    public static ItemStack createSkullFromUUID(UUID uuid, String displayName, String... lore) {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwner(Bukkit.getOfflinePlayer(uuid).getName());
        meta.setDisplayName(MessageUtils.getColor(displayName));
        meta.setLore(Arrays.asList(lore).stream()
                    .map(MessageUtils::getColor)
                    .collect(Collectors.toList()));
        skull.setItemMeta(meta);
        return skull;
    }

    public static ItemStack createEmptyTopSkull(int position, String name, String... lore) {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwner("MHF_Question");
        meta.setDisplayName(MessageUtils.getColor(name));
        meta.setLore(Arrays.asList(lore).stream()
                    .map(MessageUtils::getColor)
                    .collect(Collectors.toList()));
        skull.setItemMeta(meta);
        return skull;
    }
}
