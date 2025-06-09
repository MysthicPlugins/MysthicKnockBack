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
import mk.kvlzx.cosmetics.BackgroundMusicItem;
import mk.kvlzx.stats.PlayerStats;
import mk.kvlzx.utils.MessageUtils;

public class MusicShopMenu extends Menu {
    private final List<BackgroundMusicItem> shopItems;
    private static String currentCategory = "COMÚN";

    public MusicShopMenu(MysthicKnockBack plugin) {
        super(plugin, "&8• &d&lTienda de Música &8•", 45);
        this.shopItems = initializeShopItems();
    }

    public static void setCurrentCategory(String category) {
        currentCategory = category;
    }

    private List<BackgroundMusicItem> initializeShopItems() {
        List<BackgroundMusicItem> items = new ArrayList<>();

        // Música Común (15000 coins)
        items.add(new BackgroundMusicItem(
            "Far", 15000, "COMÚN", "&7",
            "&7¡Una melodía distante!", 
            "records.far", 1.0f, 1.0f));

        items.add(new BackgroundMusicItem(
            "Mall", 15000, "COMÚN", "&7",
            "&7¡Sonidos del centro comercial!", 
            "records.mall", 1.0f, 1.0f));

        items.add(new BackgroundMusicItem(
            "Strad", 15000, "COMÚN", "&7",
            "&7¡Una melodía clásica!", 
            "records.strad", 1.0f, 1.0f));

        // Música Épica (35000 coins)
        items.add(new BackgroundMusicItem(
            "Cat", 35000, "ÉPICO", "&5",
            "&5¡El disco del gato!", 
            "records.cat", 1.0f, 1.0f));

        items.add(new BackgroundMusicItem(
            "Chirp", 35000, "ÉPICO", "&5",
            "&5¡Melodías del pájaro!", 
            "records.chirp", 1.0f, 1.0f));

        // Música Legendaria (75000 coins)
        items.add(new BackgroundMusicItem(
            "Mellohi", 75000, "LEGENDARIO", "&6",
            "&6¡La melodía mística!", 
            "records.mellohi", 1.0f, 1.0f));

        items.add(new BackgroundMusicItem(
            "Stal", 75000, "LEGENDARIO", "&6",
            "&6¡La música del acero!", 
            "records.stal", 1.0f, 1.0f));

        return items;
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());

        // Balance actual
        inv.setItem(4, createItem(Material.EMERALD, "&a&lTu Balance",
            "&7Balance actual: &e" + stats.getKGCoins() + " KGCoins",
            "",
            "&7Categoría actual: " + currentCategory));

        // Mostrar música
        int slot = 10;
        for (BackgroundMusicItem item : shopItems) {
            if (item.getRarity().equals(currentCategory)) {
                if (slot > 34) break;
                setupMusicButton(inv, slot, item, player);
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

    private void setupMusicButton(Inventory inv, int slot, BackgroundMusicItem item, Player player) {
        boolean hasMusic = plugin.getCosmeticManager().hasPlayerBackgroundMusic(player.getUniqueId(), item.getName());
        boolean isSelected = plugin.getCosmeticManager().getPlayerBackgroundMusic(player.getUniqueId())
                            .equals(item.getName());
        
        List<String> lore = new ArrayList<>();
        lore.add(item.getRarityColor() + "✦ Rareza: " + item.getRarity());
        lore.add("");
        lore.add(MessageUtils.getColor(item.getDescription()));
        lore.add("");
        lore.add("&eClick derecho para escuchar una muestra");
        lore.add("");
        
        if (hasMusic) {
            if (isSelected) {
                lore.add("&aSeleccionado actualmente");
                lore.add("&eClick para deseleccionar");
            } else {
                lore.add("&eClick para seleccionar");
            }
        } else {
            lore.add("&7Click para comprar");
            lore.add("");
            lore.add("&8➥ Precio: &e" + item.getPrice() + " KGCoins");
        }

        Material material = isSelected ? Material.JUKEBOX : Material.RECORD_12;
        ItemStack button = createItem(material, 
            (isSelected ? "&b" : item.getRarityColor()) + item.getName(), 
            lore.toArray(new String[0]));

        if (isSelected) {
            ItemMeta meta = button.getItemMeta();
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            button.setItemMeta(meta);
        }

        inv.setItem(slot, button);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (event.getSlot() == 40) {
            // Detener cualquier música de muestra
            stopPreviewMusic(player);
            plugin.getMenuManager().openMenu(player, "music_categories");
            return;
        }

        if (clicked == null || clicked.getType() == Material.STAINED_GLASS_PANE || 
            clicked.getType() == Material.EMERALD) return;

        String itemName = MessageUtils.stripColor(clicked.getItemMeta().getDisplayName());
        BackgroundMusicItem musicItem = findMusicItem(itemName);
        if (musicItem == null) return;

        // Si es click derecho, reproducir muestra
        if (event.isRightClick()) {
            playPreviewMusic(player, musicItem);
            return;
        }

        // Si es click izquierdo, manejar compra/selección
        handleMusicSelection(player, musicItem);
    }

    private void playPreviewMusic(Player player, BackgroundMusicItem musicItem) {
        // Detener cualquier música previa
        stopPreviewMusic(player);

        // Usar el MusicManager para reproducir la música
        plugin.getMusicManager().playPreviewMusic(player, musicItem.getSound());
    }

    private void stopPreviewMusic(Player player) {
        plugin.getMusicManager().stopMusicForPlayer(player);
    }

    private void handleMusicSelection(Player player, BackgroundMusicItem musicItem) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
        String currentMusic = plugin.getCosmeticManager().getPlayerBackgroundMusic(player.getUniqueId());

        if (plugin.getCosmeticManager().hasPlayerBackgroundMusic(player.getUniqueId(), musicItem.getName())) {
            if (currentMusic.equals(musicItem.getName())) {
                plugin.getCosmeticManager().setPlayerBackgroundMusic(player.getUniqueId(), "none");
                player.sendMessage(MessageUtils.getColor("&aHas deseleccionado la música."));
                stopBackgroundMusic(player);
            } else {
                plugin.getCosmeticManager().setPlayerBackgroundMusic(player.getUniqueId(), musicItem.getName());
                player.sendMessage(MessageUtils.getColor("&aHas seleccionado la música: " + musicItem.getName()));
                startBackgroundMusic(player, musicItem);
            }
            player.closeInventory();
        } else {
            if (stats.getKGCoins() >= musicItem.getPrice()) {
                stats.removeKGCoins(musicItem.getPrice());
                plugin.getCosmeticManager().addPlayerBackgroundMusic(player.getUniqueId(), musicItem.getName());
                plugin.getCosmeticManager().setPlayerBackgroundMusic(player.getUniqueId(), musicItem.getName());
                player.sendMessage(MessageUtils.getColor("&a¡Has comprado y seleccionado la música " + 
                    musicItem.getName() + " &apor &e" + musicItem.getPrice() + " KGCoins&a!"));
                startBackgroundMusic(player, musicItem);
                player.closeInventory();
            } else {
                player.sendMessage(MessageUtils.getColor("&cNo tienes suficientes KGCoins para comprar esta música."));
            }
        }
    }

    private BackgroundMusicItem findMusicItem(String name) {
        return shopItems.stream()
            .filter(item -> MessageUtils.stripColor(item.getName()).equals(name))
            .findFirst()
            .orElse(null);
    }

    private void startBackgroundMusic(Player player, BackgroundMusicItem musicItem) {
        plugin.getMusicManager().startMusicForPlayer(player, musicItem.getSound());
    }

    private void stopBackgroundMusic(Player player) {
        plugin.getMusicManager().stopMusicForPlayer(player);
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
