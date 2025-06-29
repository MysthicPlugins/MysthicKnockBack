package mk.kvlzx.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import mk.kvlzx.MysthicKnockBack;

public class ItemVerificationManager {
    
    private final MysthicKnockBack plugin;
    private BukkitTask verificationTask;
    private final Map<UUID, Boolean> playerVerificationStatus = new HashMap<>();
    
    // Items permitidos en la zona de spawn
    private static final Material[] ALLOWED_SPAWN_ITEMS = {
        Material.SKULL_ITEM
    };
    
    public ItemVerificationManager(MysthicKnockBack plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Inicia el sistema de verificación de items para todos los jugadores
     */
    public void startVerification() {
        if (verificationTask != null) {
            verificationTask.cancel();
        }
        
        verificationTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getGameMode() == GameMode.CREATIVE) {
                        continue; // Saltar jugadores en modo creativo
                    }
                    
                    String currentZone = plugin.getArenaManager().getPlayerZone(player);
                    
                    // Solo verificar items en la zona de spawn
                    if (currentZone != null && currentZone.equals("spawn")) {
                        verifyPlayerItems(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 5L); // Verificar cada 5 ticks (0.25 segundos)
    }
    
    /**
     * Detiene el sistema de verificación de items
     */
    public void stopVerification() {
        if (verificationTask != null) {
            verificationTask.cancel();
            verificationTask = null;
        }
        playerVerificationStatus.clear();
    }
    
    /**
     * Verifica y limpia los items ilegales del inventario de un jugador específico
     * @param player El jugador a verificar
     */
    public void verifyPlayerItems(Player player) {
        if (!player.isOnline()) {
            return;
        }
        
        String currentZone = plugin.getArenaManager().getPlayerZone(player);
        if (currentZone == null || !currentZone.equals("spawn")) {
            // Remover al jugador de la verificación si no está en spawn
            playerVerificationStatus.remove(player.getUniqueId());
            return;
        }
        
        boolean itemsRemoved = false;
        
        // Verificar cada slot del inventario
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null) {
                boolean shouldRemove = false;
                
                // Verificar si el material no está permitido
                if (!isAllowedInSpawn(item.getType())) {
                    shouldRemove = true;
                }
                
                // NUEVA LÓGICA: Verificar si el item está en cooldown
                if (isItemInCooldown(player, item)) {
                    shouldRemove = true;
                }
                
                if (shouldRemove) {
                    player.getInventory().setItem(i, null);
                    itemsRemoved = true;
                }
            }
        }
        
        if (itemsRemoved && !playerVerificationStatus.getOrDefault(player.getUniqueId(), false)) {
            playerVerificationStatus.put(player.getUniqueId(), true);
        }
        
        // Resetear el estado cuando el jugador no tenga items ilegales
        if (!itemsRemoved && playerVerificationStatus.getOrDefault(player.getUniqueId(), false)) {
            playerVerificationStatus.put(player.getUniqueId(), false);
        }
    }

    /**
     * Verifica si un item específico está en cooldown
     * @param player El jugador propietario del item
     * @param item El item a verificar
     * @return true si está en cooldown, false si no
     */
    private boolean isItemInCooldown(Player player, ItemStack item) {
        if (item == null) return false;
        
        Material material = item.getType();
        
        // Verificar cooldowns según el tipo de item
        switch (material) {
            case BOW:
                return plugin.getCooldownManager().isOnCooldown(player, "BOW");
            case FEATHER:
                return plugin.getCooldownManager().isOnCooldown(player, "FEATHER");
            case GOLD_PLATE:
                return plugin.getCooldownManager().isOnCooldown(player, "PLATE");
            default:
                return false;
        }
    }
    
    /**
     * Verifica si un material está permitido en la zona de spawn
     * @param material El material a verificar
     * @return true si está permitido, false si no
     */
    private boolean isAllowedInSpawn(Material material) {
        for (Material allowed : ALLOWED_SPAWN_ITEMS) {
            if (material == allowed) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Añade un material a la lista de items permitidos en spawn
     * @param material El material a añadir
     */
    public void addAllowedSpawnItem(Material material) {
        // Crear una nueva lista con el material adicional
        Material[] newAllowed = new Material[ALLOWED_SPAWN_ITEMS.length + 1];
        System.arraycopy(ALLOWED_SPAWN_ITEMS, 0, newAllowed, 0, ALLOWED_SPAWN_ITEMS.length);
        newAllowed[ALLOWED_SPAWN_ITEMS.length] = material;
        
        // Nota: Para hacer esto dinámico, deberías usar una List en lugar de un array
        // Por ahora, puedes modificar directamente el array ALLOWED_SPAWN_ITEMS
    }
    
    /**
     * Fuerza la verificación inmediata de un jugador específico
     * @param player El jugador a verificar inmediatamente
     */
    public void forceVerifyPlayer(Player player) {
        if (player != null && player.isOnline()) {
            verifyPlayerItems(player);
        }
    }
    
    /**
     * Limpia el estado de verificación de un jugador cuando se desconecta
     * @param player El jugador que se desconecta
     */
    public void removePlayer(Player player) {
        playerVerificationStatus.remove(player.getUniqueId());
    }
    
    /**
     * Obtiene el estado de verificación actual
     * @return true si la verificación está activa, false si no
     */
    public boolean isVerificationActive() {
        return verificationTask != null;
    }
}
