package mk.kvlzx.menu;

import java.util.Collection;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.arena.Arena;
import mk.kvlzx.config.ArenaSelectMenuConfig;
import mk.kvlzx.config.ArenaSelectMenuConfig.ArenaItemType;
import mk.kvlzx.utils.MessageUtils;

public class ArenaSelectMenu extends Menu {
    private final ArenaSelectMenuConfig menuConfig;

    public ArenaSelectMenu(MysthicKnockBack plugin) {
        super(plugin, plugin.getArenaSelectMenuConfig().getMenuTitle(), plugin.getArenaSelectMenuConfig().getMenuSize());
        this.menuConfig = plugin.getArenaSelectMenuConfig();
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        Collection<Arena> arenas = plugin.getArenaManager().getArenas();
        String currentArena = plugin.getArenaManager().getCurrentArena();
        
        // Verificar restricción de tiempo
        int arenaTimeLeft = plugin.getScoreboardManager().getArenaTimeLeft();
        int minTimeForVote = plugin.getArenaVoteManager().getMinArenaTimeForVote();
        boolean canVote = arenaTimeLeft > minTimeForVote;
        
        String timeLeft = formatTime(arenaTimeLeft);
        
        int slot = menuConfig.getStartSlot();
        for (Arena arena : arenas) {
            if (slot >= menuConfig.getMaxSlot()) break; // Limitar slots
            
            String arenaName = arena.getName();
            boolean isCurrent = arenaName.equals(currentArena);
            
            ArenaItemType itemType;
            if (isCurrent) {
                itemType = ArenaItemType.CURRENT;
            } else if (!canVote) {
                itemType = ArenaItemType.BLOCKED;
            } else {
                itemType = ArenaItemType.AVAILABLE;
            }
            
            ItemStack arenaItem = menuConfig.createArenaItem(arenaName, itemType, timeLeft);
            inv.setItem(slot, arenaItem);
            
            // Calcular siguiente slot usando la configuración
            slot = menuConfig.calculateNextSlot(slot);
        }
        
        // Info item
        ItemStack infoItem = menuConfig.createInfoItem(
            currentArena != null ? currentArena : plugin.getTabConfig().getScoreNullArena(),
            arenas.size(),
            timeLeft
        );
        inv.setItem(menuConfig.getInfoItemSlot(), infoItem);
        
        // Botón de Back
        ItemStack backButton = menuConfig.createBackButton();
        inv.setItem(menuConfig.getBackButtonSlot(), backButton);
        
        // Decoración
        ItemStack decoration = menuConfig.createDecorationItem();
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, decoration);
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
        if (slot == menuConfig.getBackButtonSlot()) {
            plugin.getMenuManager().openMenu(player, "main");
            return;
        }
        
        // Ignorar clicks en items especiales y decoración
        if (slot == menuConfig.getInfoItemSlot() ||
            item.getType() == Material.valueOf(menuConfig.getDecorationId()) ||
            item.getType() == Material.valueOf(menuConfig.getInfoItemId())) {
            return;
        }
        
        // Verificar restricción de tiempo antes de procesar
        int arenaTimeLeft = plugin.getScoreboardManager().getArenaTimeLeft();
        int minTimeForVote = plugin.getArenaVoteManager().getMinArenaTimeForVote();
        
        if (arenaTimeLeft <= minTimeForVote) {
            String timeFormat = formatTime(arenaTimeLeft);
            
            player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + 
                menuConfig.getCannotVoteTimeMessage().replace("%time_left%", timeFormat)));
            player.closeInventory();
            return;
        }
        
        // Extraer nombre de arena del item name
        String itemName = item.getItemMeta().getDisplayName();
        String arenaName = MessageUtils.stripColor(itemName).trim();
        
        // Verificar que el jugador tiene permisos
        if (!player.hasPermission("mysthicknockback.vote.start")) {
            player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + 
                menuConfig.getNoPermissionMessage()));
            player.closeInventory();
            return;
        }
        
        // Verificar que la arena existe y no es actual
        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena != null) {
            String currentArena = plugin.getArenaManager().getCurrentArena();
            if (arenaName.equals(currentArena)) {
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + 
                    menuConfig.getAlreadyInArenaMessage()));
                player.closeInventory();
                return;
            }
            
            // Solo proceder si el item es clickeable
            if (item.getType() == Material.valueOf(menuConfig.getArenaAvailableId())) {
                player.closeInventory();
                plugin.getArenaVoteManager().startVote(player, arenaName);
            } else if (item.getType() == Material.valueOf(menuConfig.getArenaCurrentId())) {
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + 
                    menuConfig.getAlreadyInArenaMessage()));
                player.closeInventory();
            } else if (item.getType() == Material.valueOf(menuConfig.getArenaBlockedId())) {
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + 
                    menuConfig.getVoteUnavailableMessage()));
                player.closeInventory();
            }
        } else {
            player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + 
                menuConfig.getArenaNotFoundMessage()));
        }
    }

    private String formatTime(int timeLeft) {
        int minutes = timeLeft / 60;
        int seconds = timeLeft % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}