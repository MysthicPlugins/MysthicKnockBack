package kk.kvlzx.items;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import kk.kvlzx.utils.MessageUtils;



public class Blocks {

    private final ItemStack item;

    public Blocks(String name, List<String> lore, Material material) {
        this.item = new ItemStack(material, 64);
        ItemMeta meta = item.getItemMeta();
        if(meta !=null){
            meta.setDisplayName(MessageUtils.getColor(name));
            meta.setLore(Arrays.asList(MessageUtils.getColor("&8 Un clásico del desierto")));
            item.setItemMeta(meta);
        }

    }

    public ItemStack getItem(){
        return item;
    }

}