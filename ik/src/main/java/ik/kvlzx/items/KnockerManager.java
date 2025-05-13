package ik.kvlzx.items;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import ik.kvlzx.org.utils.MessageUtils;

public class KnockerManager {
    private static final Map<String, Knockers> knockersMap = new HashMap<>();

    static{
        knockersMap.put("STICK", new Knockers("&5 Knocker", 
                 Arrays.asList(MessageUtils.getColoredMessage("&8 No es la fuerza, es la técnica.")), Material.STICK));

        knockersMap.put("ROSE", new Knockers("&5 Knocker", 
                 Arrays.asList(MessageUtils.getColoredMessage("&8 Incluso la más hermosa flor puede causar estragos.")), Material.RED_ROSE));
 
        knockersMap.put("BONE", new Knockers("&5 Knocker",
                 Arrays.asList(MessageUtils.getColoredMessage("&8 Solo los más fuertes se aferran a la historia.")), Material.BONE));

        knockersMap.put("BLAZE_ROD", new Knockers("&5 Knocker",
                 Arrays.asList(MessageUtils.getColoredMessage("&8 Solo aquellos con voluntad ardiente la dominan.")), Material.BLAZE_ROD));

        knockersMap.put("LEVER", new Knockers("&5 Knocker",
                 Arrays.asList(MessageUtils.getColoredMessage("&8 Más que un mecanismo, una extensión de tu voluntad.")), Material.LEVER));

        knockersMap.put("TORCH", new Knockers("&5 Knocker",
                 Arrays.asList(MessageUtils.getColoredMessage("&8 Arde con intensidad… y lanza con brutalidad.")), Material.TORCH));

        knockersMap.put("SHEARS", new Knockers("&5 Knocker",
                 Arrays.asList(MessageUtils.getColoredMessage("&8 Corta la distancia con un solo golpe.")), Material.SHEARS));

        knockersMap.put("COOKIE", new Knockers("&5 Knocker",
                 Arrays.asList(MessageUtils.getColoredMessage("&8 Golpeado por la receta secreta.")), Material.COOKIE));

        knockersMap.put("POTATO", new Knockers("&5 Knocker",
                 Arrays.asList(MessageUtils.getColoredMessage("&8 Cosechada con amor, usada con odio.")), Material.POTATO));

        knockersMap.put("DIAMOND", new Knockers("&5 Knocker",
                 Arrays.asList(MessageUtils.getColoredMessage("&8 Forjado en la presión, diseñado para la dominación.")), Material.DIAMOND));

        knockersMap.put("SLIME_BALL", new Knockers("&5 Knocker",
                 Arrays.asList(MessageUtils.getColoredMessage("&8 Un impacto pegajoso con consecuencias voladoras.")), Material.SLIME_BALL));

        knockersMap.put("RED_MUSHROOM", new Knockers("&5 Knocker",
                 Arrays.asList(MessageUtils.getColoredMessage("&8 No es magia, es Knockback puro.")), Material.RED_MUSHROOM));

        knockersMap.put("BROWN_MUSHROOM", new Knockers("&5 Knocker",
                 Arrays.asList(MessageUtils.getColoredMessage("&8 De la sombra, nace la fuerza.")), Material.BROWN_MUSHROOM));

        knockersMap.put("NETHER_STAR", new Knockers("&5 Knocker",
                 Arrays.asList(MessageUtils.getColoredMessage("&8 Luz celestial, impacto infernal.")), Material.NETHER_STAR));

    }

    public static ItemStack getKnocker(String name) {
        return knockersMap.getOrDefault(name, knockersMap.get("STICK")).getItem();
    }


}
