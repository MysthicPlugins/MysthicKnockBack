package mk.kvlzx.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.DyeColor;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.stats.PlayerStats;
import mk.kvlzx.utils.MessageUtils;
import mk.kvlzx.items.CustomItem;

public class TopTimeMenu extends Menu {

    public TopTimeMenu(MysthicKnockBack plugin) {
        super(plugin, "&8• &7&lTop Tiempo &8•", 45);
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        // Banners para el borde exterior
        ItemStack blueBanner = createBanner(Material.BANNER, (byte) 4, " ", "CROSS"); // Banner azul con cruz
        ItemStack cyanBanner = createBanner(Material.BANNER, (byte) 9, " ", "STRIPE_CENTER"); // Banner cian con raya central
        ItemStack goldBanner = createBanner(Material.BANNER, (byte) 11, "&6", "FLOWER"); // Banner dorado con flor para esquinas

        // Esquinas con banners dorados
        inv.setItem(0, goldBanner);   // Superior izquierda
        inv.setItem(8, goldBanner);   // Superior derecha
        inv.setItem(36, goldBanner);  // Inferior izquierda
        inv.setItem(44, goldBanner);  // Inferior derecha

        // Borde exterior (azul y cian alternados, excluyendo esquinas)
        for (int i = 1; i < 8; i++) {
            inv.setItem(i, i % 2 == 0 ? blueBanner : cyanBanner); // Fila superior
            inv.setItem(36 + i, i % 2 == 0 ? blueBanner : cyanBanner); // Fila inferior
        }
        for (int i = 9; i <= 36; i += 9) {
            inv.setItem(i, i % 18 == 0 ? blueBanner : cyanBanner); // Columna izquierda
            inv.setItem(i + 8, i % 18 == 0 ? blueBanner : cyanBanner); // Columna derecha
        }

        // Borde interior con lana
        ItemStack blueWool = createItem(Material.WOOL, " ", (byte) 11); // Lana azul
        ItemStack whiteWool = createItem(Material.WOOL, " ", (byte) 0); // Lana blanca
        for (int i = 1; i < 8; i++) {
            inv.setItem(9 + i, i % 2 == 0 ? blueWool : whiteWool); // Fila superior interior
            inv.setItem(27 + i, i % 2 == 0 ? blueWool : whiteWool); // Fila inferior interior
        }
        for (int i = 9; i < 36; i += 9) {
            inv.setItem(i + 1, i % 18 == 0 ? blueWool : whiteWool); // Columna izquierda interior
            inv.setItem(i + 7, i % 18 == 0 ? blueWool : whiteWool); // Columna derecha interior
        }

        // Colocar las cabezas de los jugadores
        List<UUID> topPlayers = new ArrayList<>(PlayerStats.getAllStats());
        topPlayers.sort((uuid1, uuid2) -> {
            PlayerStats stats1 = PlayerStats.getStats(uuid1);
            PlayerStats stats2 = PlayerStats.getStats(uuid2);
            return Long.compare(stats2.getPlayTime(), stats1.getPlayTime());
        });

        int[] slots = {11, 12, 13, 14, 15, 20, 21, 22, 23, 24};
        for (int i = 0; i < 10; i++) {
            ItemStack skull;
            if (i < topPlayers.size()) {
                UUID uuid = topPlayers.get(i);
                PlayerStats stats = PlayerStats.getStats(uuid);
                String playerName = Bukkit.getOfflinePlayer(uuid).getName();
                
                List<String> lore = new ArrayList<>();
                lore.add("&7Posición: &f#" + (i + 1));
                lore.add("&7Tiempo: &b" + stats.getFormattedPlayTime());
                
                skull = CustomItem.createSkullFromUUID(uuid, 
                    "&b" + playerName,
                    lore.toArray(new String[0]));
            } else {
                skull = CustomItem.createEmptyTopSkull(i + 1, "&7Sin datos", 
                    "&7Posición: &f#" + (i + 1),
                    "&7Tiempo: &e0h 00m");
            }
            
            inv.setItem(slots[i], skull);
        }

        // Botón para volver
        ItemStack backButton = createItem(Material.ARROW, "&c← Volver", 
            "&7Click para volver al menú principal");
        inv.setItem(40, backButton);

        // Relleno con relojes
        ItemStack clock = createItem(Material.WATCH, " ", "&7"); // Reloj para el tema de tiempo
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, clock); // Rellenar slots vacíos
            }
        }
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.BANNER || 
            clicked.getType() == Material.WOOL || clicked.getType() == Material.WATCH) {
            return;
        }

        if (event.getSlot() == 40) { // Botón de volver
            Player player = (Player) event.getWhoClicked();
            plugin.getMenuManager().openMenu(player, "main");
        }
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

    private ItemStack createBanner(Material material, byte color, String name, String pattern) {
        ItemStack banner = new ItemStack(material, 1, color);
        BannerMeta meta = (BannerMeta) banner.getItemMeta();
        meta.setDisplayName(MessageUtils.getColor(name));
        
        if (pattern != null) {
            PatternType patternType;
            switch (pattern.toUpperCase()) {
                case "CROSS":
                    patternType = PatternType.CROSS;
                    break;
                case "STRIPE_CENTER":
                    patternType = PatternType.STRIPE_CENTER;
                    break;
                case "FLOWER":
                    patternType = PatternType.FLOWER;
                    break;
                default:
                    patternType = PatternType.BASE;
            }
            meta.addPattern(new Pattern(DyeColor.getByDyeData(color), patternType));
        }
        
        banner.setItemMeta(meta);
        return banner;
    }
}