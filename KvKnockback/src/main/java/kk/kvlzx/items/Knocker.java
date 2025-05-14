package kk.kvlzx.items;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import kk.kvlzx.utils.MessageUtils;

public class Knocker {

    private final ItemStack item;

    public Knocker(String name, List<String> lore, Material material){
        this.item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if(meta !=null){
            meta.setDisplayName(MessageUtils.getColor(name));
            meta.addEnchant(Enchantment.KNOCKBACK, 2, true);
            meta.setLore(Arrays.asList(MessageUtils.getColor("&8 No es la fuerza, es la técnica.")));
            item.setItemMeta(meta);
        }
    }

    public ItemStack getItem(){
        return item;
    }

}
