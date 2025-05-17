package kk.kvlzx.items;

import java.util.Arrays;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import kk.kvlzx.utils.MessageUtils;

public class CustomItem {
    public enum ItemType {
        // Items de combate
        KNOCKER(Material.STICK, "&5Knocker", "&8 No es la fuerza, es la técnica.", true),
        BOW(Material.BOW, "&4Mazakarko", "&8 Un disparo, un impacto, un salto al vacío.", false),
        ARROW(Material.ARROW, "&5Flecha", null, false),
        PEARL(Material.ENDER_PEARL, "&5Perla", "&8 Cada lanzamiento reescribe tu destino.", false),
        FEATHER(Material.FEATHER, "&ePluma", "&8 No es magia, es pura aerodinámica.", false),
        PLATE(Material.GOLD_PLATE, "&6Placa", "&8 ¿Listo para volar? Pisa y verás.", false),
        BLOCKS(Material.SANDSTONE, "&3Arenisca", "&8 Un clásico del desierto", false),

        // Items del spawn
        TOP_KILLS(Material.DIAMOND_SWORD, "&b⚔ Top Kills", "&7Click para ver el top de kills", false),
        TOP_KDR(Material.GOLDEN_APPLE, "&6☠ Top KDR", "&7Click para ver el top de KDR", false),
        TOP_STREAKS(Material.BLAZE_POWDER, "&c⚡ Top Rachas", "&7Click para ver el top de rachas", false),
        TOP_ELO(Material.NETHER_STAR, "&e✦ Top ELO", "&7Click para ver el top de ELO", false),
        PLAYER_STATS(Material.SKULL_ITEM, "&a✪ Tus Estadísticas", "&7Click para ver tus stats", false);

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
                meta.addEnchant(Enchantment.KNOCKBACK, 2, true);
            }

            item.setItemMeta(meta);
        }

        if (type == ItemType.BLOCKS) {
            item.setAmount(64);
        }

        return item;
    }

    public static ItemStack createSkull(Player player) {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        
        meta.setOwner(player.getName());
        meta.setDisplayName(MessageUtils.getColor("&a✪ Tus Estadísticas"));
        meta.setLore(Arrays.asList(MessageUtils.getColor("&7Click para ver tus stats")));
        
        skull.setItemMeta(meta);
        return skull;
    }
}
