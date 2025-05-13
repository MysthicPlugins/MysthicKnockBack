package ik.kvlzx.items;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import ik.kvlzx.org.utils.MessageUtils;

public class Bow {

    private final ItemStack item;

    public Bow(String name, List<String> lore, Material material){
        this.item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if(meta !=null){
            meta.setDisplayName(MessageUtils.getColoredMessage(name));
            meta.addEnchant(Enchantment.ARROW_KNOCKBACK, 2, true);
            meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
            meta.setLore(Arrays.asList(MessageUtils.getColoredMessage("&8 Un disparo, un impacto, un salto al vacío.")));
            item.setItemMeta(meta);
        }

    }

    public ItemStack getItem(){
        return item;
    }

}
