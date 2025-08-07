package mk.kvlzx.menu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.arena.Arena;
import mk.kvlzx.utils.MessageUtils;

public class ArenaSelectMenu extends Menu {

    public ArenaSelectMenu(MysthicKnockBack plugin) {
        super(plugin, "&9&lSelect Arena to Vote", 54);
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        Collection<Arena> arenas = plugin.getArenaManager().getArenas();
        String currentArena = plugin.getArenaManager().getCurrentArena();
        
        // Verificar restricción de tiempo
        int arenaTimeLeft = plugin.getScoreboardManager().getArenaTimeLeft();
        int minTimeForVote = plugin.getArenaVoteManager().getMinArenaTimeForVote();
        boolean canVote = arenaTimeLeft > minTimeForVote;
        
        int slot = 10;
        for (Arena arena : arenas) {
            if (slot >= 44) break; // Limitar slots
            
            String arenaName = arena.getName();
            boolean isCurrent = arenaName.equals(currentArena);
            
            Material material;
            List<String> lore = new ArrayList<>();
            lore.add("");
            
            if (isCurrent) {
                material = Material.EMERALD_BLOCK;
                lore.add("&a✓ Current Arena");
                lore.add("&7You are already here!");
            } else if (!canVote) {
                material = Material.REDSTONE_BLOCK;
                int minutes = arenaTimeLeft / 60;
                int seconds = arenaTimeLeft % 60;
                String timeFormat = String.format("%02d:%02d", minutes, seconds);
                lore.add("&c✗ Cannot Vote");
                lore.add("&7Arena changes in &e" + timeFormat);
                lore.add("&7Wait until &e2:00+ &7remain");
            } else {
                material = Material.DIAMOND_BLOCK;
                lore.add("&7Click to start a vote");
                lore.add("&7to change to this arena!");
                lore.add("");
                lore.add("&e➤ Click to vote!");
            }
            lore.add("");
            
            ItemStack item = createItem(material, "&b&l" + arenaName, lore);
            inv.setItem(slot, item);
            
            // Calcular siguiente slot (patrón 3x7)
            slot++;
            if ((slot - 10) % 7 == 0) {
                slot += 2; // Saltar a siguiente fila
            }
        }
        
        // Info item
        List<String> infoLore = new ArrayList<>();
        infoLore.add("&7Select an arena to start a vote!");
        infoLore.add("");
        infoLore.add("&7Current: &b" + (currentArena != null ? currentArena : plugin.getTabConfig().getScoreNullArena()));
        infoLore.add("&7Total Arenas: &b" + arenas.size());
        
        if (!canVote) {
            int minutes = arenaTimeLeft / 60;
            int seconds = arenaTimeLeft % 60;
            String timeFormat = String.format("%02d:%02d", minutes, seconds);
            infoLore.add("");
            infoLore.add("&c⚠ Voting Disabled");
            infoLore.add("&7Arena changes in: &e" + timeFormat);
            infoLore.add("&7Need &e2:00+ &7to vote");
        } else {
            infoLore.add("");
            infoLore.add("&a✓ Voting Available");
        }
        
        ItemStack info = createItem(Material.PAPER, "&e&lArena Voting", infoLore);
        inv.setItem(4, info);
        
        // Botón de Back
        ItemStack backButton = createItem(Material.ARROW, "&c&lBack", 
            "&7Click to return to main menu"
        );
        inv.setItem(49, backButton);
        
        // Decoración
        ItemStack glass = createItem(Material.STAINED_GLASS_PANE, " ", (byte) 9);
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, glass);
            }
        }
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        if (!isValidClick(event)) {
            event.setCancelled(true);
            return;
        }
        
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        int slot = event.getSlot();
        
        if (item == null || !item.hasItemMeta()) return;
        
        // Manejar botón de back
        if (slot == 49) {
            plugin.getMenuManager().openMenu(player, "main");
            return;
        }
        
        String itemName = item.getItemMeta().getDisplayName();
        if (item.getType() == Material.STAINED_GLASS_PANE ||
            item.getType() == Material.PAPER) return;
        
        // Verificar restricción de tiempo antes de procesar
        int arenaTimeLeft = plugin.getScoreboardManager().getArenaTimeLeft();
        int minTimeForVote = plugin.getArenaVoteManager().getMinArenaTimeForVote();
        
        if (arenaTimeLeft <= minTimeForVote) {
            int minutes = arenaTimeLeft / 60;
            int seconds = arenaTimeLeft % 60;
            String timeFormat = String.format("%02d:%02d", minutes, seconds);
            
            player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + 
                "&cCannot vote! Arena changes in &e" + timeFormat + "&c. Wait until there's more than &e2:00 &cleft."));
            player.closeInventory();
            return;
        }
        
        // Extraer nombre de arena del item name
        String arenaName = MessageUtils.stripColor(itemName).trim();
        
        // Verificar que el jugador tiene permisos
        if (!player.hasPermission("mysthicknockback.vote.start")) {
            player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + 
                "&cYou don't have permission to start votes!"));
            player.closeInventory();
            return;
        }
        
        // Verificar que la arena existe y no es actual
        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena != null) {
            String currentArena = plugin.getArenaManager().getCurrentArena();
            if (arenaName.equals(currentArena)) {
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + 
                    "&cYou're already in that arena!"));
                player.closeInventory();
                return;
            }
            
            player.closeInventory();
            plugin.getArenaVoteManager().startVote(player, arenaName);
        } else {
            player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + 
                "&cArena not found!"));
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
    
    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtils.getColor(name));
        
        if (lore != null && !lore.isEmpty()) {
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