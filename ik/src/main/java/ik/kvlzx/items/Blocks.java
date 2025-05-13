package ik.kvlzx.items;

import java.util.Arrays;
import java.util.List;


import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import ik.kvlzx.org.utils.MessageUtils;
import org.bukkit.Material;



public class Blocks {

    private final ItemStack item;

    public Blocks(String name, List<String> lore, Material material) {
        this.item = new ItemStack(material, 64);
        ItemMeta meta = item.getItemMeta();
        if(meta !=null){

            meta.setDisplayName(MessageUtils.getColoredMessage(name));
            meta.setLore(Arrays.asList(MessageUtils.getColoredMessage("&8 Un clásico del desierto")));
            item.setItemMeta(meta);
            
        }

    }

    public ItemStack getItem(){
        return item;
    }

}