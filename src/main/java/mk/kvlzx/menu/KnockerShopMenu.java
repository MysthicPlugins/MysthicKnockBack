package mk.kvlzx.menu;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.cosmetics.KnockerShopItem;
import mk.kvlzx.stats.PlayerStats;
import mk.kvlzx.utils.MessageUtils;

public class KnockerShopMenu extends Menu {
    private final List<KnockerShopItem> shopItems;
    private static String currentCategory = "COMÚN";

    public KnockerShopMenu(MysthicKnockBack plugin) {
        super(plugin, "&8• &e&lTienda de Knockers &8•", 45);
        this.shopItems = initializeShopItems();
    }

    public static void setCurrentCategory(String category) {
        currentCategory = category;
    }

    private List<KnockerShopItem> initializeShopItems() {
        List<KnockerShopItem> items = new ArrayList<>();
        
        addCommonKnockers(items);
        addUncommonKnockers(items);
        addRareKnockers(items);
        addEpicKnockers(items);
        addLegendaryKnockers(items);
        
        return items;
    }

    private void addCommonKnockers(List<KnockerShopItem> items) {
        items.add(new KnockerShopItem(Material.STICK, "Palo Default", 0, "COMÚN", "&7", "&8El clásico palo de siempre"));
        items.add(new KnockerShopItem(Material.BONE, "Hueso Knockeador", 5000, "COMÚN", "&7", "&e¡Los perros lo adoran!"));
        items.add(new KnockerShopItem(Material.WOOD_SPADE, "Pala de Madera", 5000, "COMÚN", "&7", "&6¡Cavando tu victoria!"));
        items.add(new KnockerShopItem(Material.BLAZE_ROD, "Vara de Blaze", 5000, "COMÚN", "&7", "&cArdiente al tacto"));
        items.add(new KnockerShopItem(Material.CARROT_STICK, "Caña con Zanahoria", 5000, "COMÚN", "&7", "&6¡El favorito de los conejos!"));
    }

    private void addUncommonKnockers(List<KnockerShopItem> items) {
        items.add(new KnockerShopItem(Material.GOLD_HOE, "Azada Dorada", 15000, "POCO COMÚN", "&a", "&e¡Brilla como el sol!"));
        items.add(new KnockerShopItem(Material.IRON_SPADE, "Pala de Hierro", 15000, "POCO COMÚN", "&a", "&7Forjada en las montañas"));
        items.add(new KnockerShopItem(Material.STONE_HOE, "Azada de Piedra", 15000, "POCO COMÚN", "&a", "&8Tan dura como la roca"));
        items.add(new KnockerShopItem(Material.WOOD_HOE, "Azada de Madera", 15000, "POCO COMÚN", "&a", "&6Tallada a mano"));
        items.add(new KnockerShopItem(Material.WOOD_SWORD, "Espada de Madera", 15000, "POCO COMÚN", "&a", "&6¡Perfecta para entrenar!"));
    }

    private void addRareKnockers(List<KnockerShopItem> items) {
        items.add(new KnockerShopItem(Material.DIAMOND_HOE, "Azada de Diamante", 50000, "RARO", "&9", "&b¡La más brillante de todas!"));
        items.add(new KnockerShopItem(Material.GOLD_SWORD, "Espada Dorada", 50000, "RARO", "&9", "&e¡El arma de los reyes!"));
        items.add(new KnockerShopItem(Material.IRON_SWORD, "Espada de Hierro", 50000, "RARO", "&9", "&7Afilada como ninguna"));
        items.add(new KnockerShopItem(Material.DIAMOND_SPADE, "Pala de Diamante", 50000, "RARO", "&9", "&bCavando con estilo"));
        items.add(new KnockerShopItem(Material.GOLD_SPADE, "Pala Dorada", 50000, "RARO", "&9", "&e¡Reluciente como el oro!"));
    }

    private void addEpicKnockers(List<KnockerShopItem> items) {
        items.add(new KnockerShopItem(Material.DIAMOND_SWORD, "Espada de Diamante", 500000, "ÉPICO", "&5", "&b¡La más poderosa de todas!"));
        items.add(new KnockerShopItem(Material.IRON_AXE, "Hacha de Hierro", 500000, "ÉPICO", "&5", "&7¡Corta como mantequilla!"));
        items.add(new KnockerShopItem(Material.DIAMOND_AXE, "Hacha de Diamante", 500000, "ÉPICO", "&5", "&b¡La destructora definitiva!"));
        items.add(new KnockerShopItem(Material.GOLD_AXE, "Hacha Dorada", 500000, "ÉPICO", "&5", "&e¡Digna de un rey!"));
    }

    private void addLegendaryKnockers(List<KnockerShopItem> items) {
        items.add(new KnockerShopItem(Material.NETHER_STAR, "Estrella del Nether", 1000000, "LEGENDARIO", "&6", "&c&l¡El poder del Wither!"));
        items.add(new KnockerShopItem(Material.GHAST_TEAR, "Lágrima de Ghast", 1000000, "LEGENDARIO", "&6", "&f&l¡Lágrimas de poder!"));
        items.add(new KnockerShopItem(Material.PRISMARINE_SHARD, "Fragmento de Prismarina", 1000000, "LEGENDARIO", "&6", "&3&l¡La fuerza del océano!"));
        items.add(new KnockerShopItem(Material.EMERALD, "Esmeralda del Poder", 1000000, "LEGENDARIO", "&6", "&a&l¡La riqueza es poder!"));
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());

        // Balance actual
        inv.setItem(4, createItem(Material.EMERALD, "&a&lTu Balance",
            "&7Balance actual: &e" + stats.getKGCoins() + " KGCoins",
            "",
            "&7Categoría actual: " + currentCategory));

        // Mostrar knockers
        int slot = 10;
        for (KnockerShopItem item : shopItems) {
            if (item.getRarity().equals(currentCategory)) {
                if (slot > 34) break;
                setupKnockerButton(inv, slot, item, player, plugin.getCosmeticManager().getPlayerKnocker(player.getUniqueId()));
                slot++;
                if ((slot + 1) % 9 == 0) slot += 2;
            }
        }

        // Botón para volver
        inv.setItem(40, createItem(Material.ARROW, "&c← Volver", 
            "&7Click para volver a las categorías"));

        // Relleno
        fillEmptySlots(inv, createItem(Material.STAINED_GLASS_PANE, " ", (byte) 15));
    }

    private void setupKnockerButton(Inventory inv, int slot, KnockerShopItem item, Player player, Material currentKnocker) {
        boolean hasKnocker = item.getMaterial() == Material.STICK || plugin.getCosmeticManager().hasPlayerBlock(player.getUniqueId(), item.getMaterial());
        boolean isSelected = currentKnocker == item.getMaterial();
        
        List<String> lore = new ArrayList<>();
        
        lore.add(item.getRarityColor() + "✦ Rareza: " + item.getRarity());
        lore.add("");
        lore.add(MessageUtils.getColor(item.getDescription()));
        lore.add("");
        
        if (item.getMaterial() == Material.STICK && item.getData() == 0) {
            lore.add("&aKnocker por defecto");
            lore.add("&8➥ Siempre disponible");
            if (isSelected) {
                lore.add("");
                lore.add("&aSeleccionado actualmente");
            } else {
                lore.add("");
                lore.add("&eClick para seleccionar");
            }
        } else if (hasKnocker) {
            if (isSelected) {
                lore.add("&aSeleccionado actualmente");
                lore.add("&8➥ Usando este knocker");
            } else {
                lore.add("&eClick para seleccionar");
                lore.add("&8➥ Ya posees este knocker");
            }
        } else {
            lore.add("&7Click para comprar");
            lore.add("");
            lore.add("&8➥ Precio: &e" + item.getPrice() + " KGCoins");
        }

        String displayName = (isSelected ? "&b" : item.getRarityColor()) + item.getName();
        ItemStack buttonItem = createItem(item.getMaterial(), displayName, item.getData(), lore.toArray(new String[0]));
        
        if (isSelected) {
            buttonItem.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
            ItemMeta meta = buttonItem.getItemMeta();
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            buttonItem.setItemMeta(meta);
        }

        inv.setItem(slot, buttonItem);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (event.getSlot() == 40) {
            plugin.getMenuManager().openMenu(player, "knocker_categories");
            return;
        }

        if (clicked == null || clicked.getType() == Material.STAINED_GLASS_PANE || 
            clicked.getType() == Material.EMERALD) return;

        KnockerShopItem shopItem = findShopItem(clicked);
        if (shopItem == null) return;

        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());

        // Si ya tiene el knocker o es el palo por defecto
        if (plugin.getCosmeticManager().hasPlayerKnocker(player.getUniqueId(), shopItem.getMaterial())) {
            plugin.getCosmeticManager().setPlayerKnocker(player.getUniqueId(), shopItem.getMaterial());
            player.sendMessage(MessageUtils.getColor("&aHas seleccionado el knocker de " + shopItem.getName()));
            player.closeInventory();
        } else {
            if (stats.getKGCoins() >= shopItem.getPrice()) {
                stats.removeKGCoins(shopItem.getPrice());
                plugin.getCosmeticManager().addPlayerKnocker(player.getUniqueId(), shopItem.getMaterial());
                plugin.getCosmeticManager().setPlayerKnocker(player.getUniqueId(), shopItem.getMaterial());
                player.sendMessage(MessageUtils.getColor("&a¡Has comprado y seleccionado el knocker de " + 
                    shopItem.getName() + " &apor &e" + shopItem.getPrice() + " KGCoins&a!"));
                player.closeInventory();
            } else {
                player.sendMessage(MessageUtils.getColor("&cNo tienes suficientes KGCoins para comprar este knocker."));
            }
        }
    }

    private KnockerShopItem findShopItem(ItemStack clicked) {
        return shopItems.stream()
            .filter(item -> item.getMaterial() == clicked.getType() && item.getData() == clicked.getDurability())
            .findFirst()
            .orElse(null);
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        return createItem(material, name, (byte) 0, lore);
    }

    private ItemStack createItem(Material material, String name, byte data, String... lore) {
        ItemStack item = new ItemStack(material, 1, data);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtils.getColor(name));
        
        if (lore.length > 0) {
            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                coloredLore.add(MessageUtils.getColor(line));
            }
            meta.setLore(coloredLore);
        }
        
        item.setItemMeta(meta);
        return item;
    }
}
