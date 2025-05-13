package ik.kvlzx.items;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import ik.kvlzx.org.utils.MessageUtils;


public class Feather {
    private final ItemStack item;

    public Feather(String name, List<String> lore, Material material) {
        this.item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(MessageUtils.getColoredMessage(name));
            meta.setLore(Arrays.asList(MessageUtils.getColoredMessage("&8 No es magia, es pura aerodinámica.")));
            item.setItemMeta(meta);
        }
    }

    public ItemStack getItem() {
        return item;
    }
}

