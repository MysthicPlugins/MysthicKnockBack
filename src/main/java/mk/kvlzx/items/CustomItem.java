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

import mk.kvlzx.utils.MessageUtils;

public class CustomItem {
    public enum ItemType {
        // Items de combate
        KNOCKER(Material.STICK, "&5Knocker", "&8 It's not the strength, it's the technique.", true),
        BOW(Material.BOW, "&4Mazakarko", "&8 One shot, one impact, one leap into the void.", false),
        ARROW(Material.ARROW, "&5Arrow", null, false),
        PEARL(Material.ENDER_PEARL, "&5Pearl", "&8 Each throw rewrites your destiny.", false),
        FEATHER(Material.FEATHER, "&eFeather", "&8 It's not magic, it's pure aerodynamics.", false),
        PLATE(Material.GOLD_PLATE, "&6Plate", "&8 Ready to fly? Step on it and see.", false),
        BLOCKS(Material.SANDSTONE, "&3Sandstone", "&8 A desert classic", false);

        private final Material material;
        private final String name;
        private final String lore;
        private final boolean knockback;

        ItemType(Material material, String name, String lore, boolean knockback) {
            this.material = material;
            this.name = name;
            this.lore = lore;
            this.knockback = knockback;
        }
    }

    public static ItemStack create(ItemType type) {
        ItemStack item = new ItemStack(type.material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(MessageUtils.getColor(type.name));
            
            if (type.lore != null) {
                meta.setLore(Arrays.asList(MessageUtils.getColor(type.lore)));
            }

            if (type.knockback) {
                meta.addEnchant(Enchantment.KNOCKBACK, 1, true);
            }
            
            if (type == ItemType.BOW) {
                meta.addEnchant(Enchantment.ARROW_KNOCKBACK, 2, true);
            }

            item.setItemMeta(meta);
        }

        if (type == ItemType.BLOCKS) {
            item.setAmount(64);
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
