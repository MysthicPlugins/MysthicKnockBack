package kk.kvlzx.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import kk.kvlzx.utils.MessageUtils;

public class Arrow {

    private final ItemStack item;

    public Arrow(String name, Material material){
        this.item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if(meta !=null){
            meta.setDisplayName(MessageUtils.getColor(name));
            item.setItemMeta(meta);
        }

    }

    public ItemStack getItem(){
        return item;
    }
}
