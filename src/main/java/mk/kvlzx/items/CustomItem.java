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
        KNOCKER(Material.STICK, "&5Knocker", "&8 No es la fuerza, es la técnica.", true),
        BOW(Material.BOW, "&4Mazakarko", "&8 Un disparo, un impacto, un salto al vacío.", false),
        ARROW(Material.ARROW, "&5Flecha", null, false),
        PEARL(Material.ENDER_PEARL, "&5Perla", "&8 Cada lanzamiento reescribe tu destino.", false),
        FEATHER(Material.FEATHER, "&ePluma", "&8 No es magia, es pura aerodinámica.", false),
        PLATE(Material.GOLD_PLATE, "&6Placa", "&8 ¿Listo para volar? Pisa y verás.", false),
        BLOCKS(Material.SANDSTONE, "&3Arenisca", "&8 Un clásico del desierto", false);

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
