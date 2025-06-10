package mk.kvlzx.menu;

import java.util.ArrayList;
import java.util.List;

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
import mk.kvlzx.items.CustomItem;
import mk.kvlzx.reports.ReportReason;
import mk.kvlzx.utils.MessageUtils;

public class ReportReasonMenu extends Menu {

    public ReportReasonMenu(MysthicKnockBack plugin) {
        super(plugin, "&8• &c&lSeleccionar Razón &8•", 45);
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        String targetName = plugin.getReportManager().getReportTarget(player.getUniqueId());

        // Cabeza del jugador a reportar
        ItemStack targetHead = CustomItem.createSkullFromUUID(
            plugin.getServer().getPlayer(targetName).getUniqueId(),
            "&c&lReportando a: &f" + targetName,
            "&8▪ &7Selecciona una razón para el reporte",
            "",
            "&8➥ &7Elige cuidadosamente"
        );
        inv.setItem(4, targetHead);

        // Razones de reporte en forma de U
        int[] reasonSlots = {19, 20, 21, 22, 23, 24, 25, 34};
        int index = 0;
        for (ReportReason reason : ReportReason.values()) {
            if (index >= reasonSlots.length) break;
            
            List<String> lore = new ArrayList<>();
            lore.add("&8▪ &7Click para seleccionar este motivo");
            lore.add("");
            lore.add("&8➥ &7" + reason.getDescription());
            
            inv.setItem(reasonSlots[index], createItem(
                reason.getIcon(),
                reason.getDisplayName(),
                lore.toArray(new String[0])
            ));
            index++;
        }

        // Banners rojos en las esquinas
        ItemStack redCornerBanner = createBanner(Material.BANNER, (byte) 1, "&c", "CROSS"); // Banner rojo con cruz
        inv.setItem(0, redCornerBanner);   // Esquina superior izquierda
        inv.setItem(8, redCornerBanner);   // Esquina superior derecha
        inv.setItem(36, redCornerBanner);  // Esquina inferior izquierda
        inv.setItem(44, redCornerBanner);  // Esquina inferior derecha

        // Separadores decorativos (línea superior, estilo ajedrez)
        ItemStack redPane = createItem(Material.STAINED_GLASS_PANE, " ", (byte) 14); // Panel rojo
        ItemStack darkGrayPane = createItem(Material.STAINED_GLASS_PANE, " ", (byte) 7); // Panel gris oscuro (sustituto de negro)
        for (int i : new int[]{1, 2, 3, 5, 6, 7}) {
            inv.setItem(i, i % 2 == 0 ? redPane : darkGrayPane); // Alternar rojo y gris
        }

        // Botón para volver
        inv.setItem(40, createItem(Material.ARROW, "&c← Cancelar", 
            "&7Click para volver a la lista de jugadores"));

        // Bordes con patrón de ajedrez
        for (int i = 9; i <= 35; i += 9) {
            inv.setItem(i, i % 18 == 0 ? redPane : darkGrayPane); // Borde izquierdo
            inv.setItem(i + 8, i % 18 == 0 ? redPane : darkGrayPane); // Borde derecho
        }
        for (int i = 9; i <= 17; i++) {
            if (i != 13) { // Evitar sobrescribir la cabeza
                inv.setItem(i, i % 2 == 0 ? redPane : darkGrayPane); // Borde superior
            }
        }
        for (int i = 27; i <= 35; i++) {
            inv.setItem(i, i % 2 == 0 ? redPane : darkGrayPane); // Borde inferior
        }

        // Rellenar espacios vacíos con rosas
        ItemStack rose = createItem(Material.RED_ROSE, "&7", (byte) 0); // Rosa roja
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, rose); // Rellenar slots vacíos
            }
        }
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.STAINED_GLASS_PANE || 
            clicked.getType() == Material.BANNER || clicked.getType() == Material.RED_ROSE) return;

        String targetName = plugin.getReportManager().getReportTarget(player.getUniqueId());
        if (targetName == null) {
            player.closeInventory();
            player.sendMessage(MessageUtils.getColor("&cError: No se encontró el jugador a reportar"));
            return;
        }

        if (event.getSlot() == 40) {
            plugin.getMenuManager().openMenu(player, "player_list");
            return;
        }

        // Buscar la razón que coincida con el ítem clickeado
        for (ReportReason reason : ReportReason.values()) {
            if (clicked.getType() == reason.getIcon()) {
                plugin.getReportManager().submitReport(player, targetName, reason);
                player.closeInventory();
                return;
            }
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
        
        // Añadir patrón al banner
        if (pattern != null) {
            PatternType patternType;
            switch (pattern.toUpperCase()) {
                case "CROSS":
                    patternType = PatternType.CROSS;
                    break;
                case "BORDER":
                    patternType = PatternType.BORDER;
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
            meta.addPattern(new Pattern(DyeColor.values()[color], patternType));
        }
        
        banner.setItemMeta(meta);
        return banner;
    }
}
