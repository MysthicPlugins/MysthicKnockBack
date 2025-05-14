package kk.kvlzx.items;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import kk.kvlzx.utils.MessageUtils;

public class Plate {
    private final ItemStack item;

    public Plate(String name, List<String> lore, Material material) {
        this.item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(MessageUtils.getColor("&6 Placa"));
            meta.setLore(Arrays.asList(MessageUtils.getColor("&8 ¿Listo para volar? Pisa y verás.")));
            item.setItemMeta(meta);
        }
    }

    public ItemStack getItem() {
        return item;
    }
}


