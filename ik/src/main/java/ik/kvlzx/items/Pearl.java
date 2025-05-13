package ik.kvlzx.items;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import ik.kvlzx.org.utils.MessageUtils;



public class Pearl {
    
    private final ItemStack item;

    public Pearl(String name, List<String> lore, Material material) {
        this.item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        if(meta !=null){

            meta.setDisplayName(MessageUtils.getColoredMessage(name));
            meta.setLore(Arrays.asList(MessageUtils.getColoredMessage("&8 Cada lanzamiento reescribe tu destino.")));
            item.setItemMeta(meta);
            
        }

    }

    public ItemStack getItem(){
        return item;
    }

}
