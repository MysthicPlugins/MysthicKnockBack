package ik.kvlzx.items;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import ik.kvlzx.org.utils.MessageUtils;


public class Plate {
    private final ItemStack item;

    public Plate(String name, List<String> lore, Material material) {
        this.item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(MessageUtils.getColoredMessage("&6 Placa"));
            meta.setLore(Arrays.asList(MessageUtils.getColoredMessage("&8 ¿Listo para volar? Pisa y verás.")));
            item.setItemMeta(meta);
        }
    }

    public ItemStack getItem() {
        return item;
    }
}


