package mk.kvlzx.powerup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.arena.Arena;
import mk.kvlzx.arena.ArenaManager;
import mk.kvlzx.arena.Zone;
import mk.kvlzx.utils.MessageUtils;

public class PowerUpManager {
    private final MysthicKnockBack plugin;
    private final ArenaManager arenaManager;
    private final Map<String, List<PowerUp>> arenaPowerUps;
    private final Map<String, BukkitTask> arenaSpawnTasks;
    private final Random random;

    // Configuración
    private final int SPAWN_INTERVAL = MysthicKnockBack.getInstance().getMainConfig().getPowerUpSpawnInterval(); // Segundos entre spawns
    private final int MAX_POWERUPS_PER_ARENA = MysthicKnockBack.getInstance().getMainConfig().getPowerUpMaxPowerUp(); // Máximo de powerups por arena
    private final int MAX_SPAWN_ATTEMPTS = 50; // Aumentado de 30 a 50 para más intentos
    private final double MIN_DISTANCE_BETWEEN_POWERUPS = Math.max(2.0, MysthicKnockBack.getInstance().getMainConfig().getPowerUpMinDistance()); // Mínimo 2 bloques

    public PowerUpManager(MysthicKnockBack plugin, ArenaManager arenaManager) {
        this.plugin = plugin;
        this.arenaManager = arenaManager;
        this.arenaPowerUps = new ConcurrentHashMap<>();
        this.arenaSpawnTasks = new HashMap<>();
        this.random = new Random();
        
        startPowerUpSystem();
    }

    private void startPowerUpSystem() {
        // Inicializar powerups para cada arena
        for (Arena arena : arenaManager.getArenas()) {
            initializeArena(arena.getName());
        }

        // Task principal para cleanup y verificación - MÁS FRECUENTE
        new BukkitRunnable() {
            @Override
            public void run() {
                cleanupExpiredPowerUps();
            }
        }.runTaskTimer(plugin, 0L, 20L); // Cada segundo en lugar de cada 4*CHECK_INTERVAL
    }

    public void initializeArena(String arenaName) {
        if (!arenaPowerUps.containsKey(arenaName)) {
            arenaPowerUps.put(arenaName, Collections.synchronizedList(new ArrayList<>()));
        }

        // Cancelar task anterior si existe
        BukkitTask oldTask = arenaSpawnTasks.get(arenaName);
        if (oldTask != null) {
            oldTask.cancel();
        }

        // Crear task de spawn para esta arena
        BukkitTask spawnTask = new BukkitRunnable() {
            @Override
            public void run() {
                trySpawnPowerUp(arenaName);
            }
        }.runTaskTimer(plugin, 20L * SPAWN_INTERVAL, 20L * SPAWN_INTERVAL);

        arenaSpawnTasks.put(arenaName, spawnTask);
    }

    private void trySpawnPowerUp(String arenaName) {
        try {
            Arena arena = arenaManager.getArena(arenaName);
            if (arena == null) {
                return;
            }

            // VERIFICACIÓN MEJORADA DE JUGADORES
            Set<Player> playersInArena = arenaManager.getPlayersInArena(arenaName);
            
            // También verificar jugadores online en el mundo de la arena
            if (playersInArena.isEmpty()) {
                Location spawnLoc = arena.getSpawnLocation();
                if (spawnLoc != null && spawnLoc.getWorld() != null) {
                    for (Player player : spawnLoc.getWorld().getPlayers()) {
                        String playerArena = arenaManager.getPlayerArena(player);
                        if (arenaName.equals(playerArena)) {
                            playersInArena.add(player);
                        }
                    }
                }
            }

            if (playersInArena.isEmpty()) {
                return;
            }

            // LIMPIEZA MEJORADA DE LA LISTA
            List<PowerUp> powerUps = arenaPowerUps.get(arenaName);
            if (powerUps == null) {
                powerUps = Collections.synchronizedList(new ArrayList<>());
                arenaPowerUps.put(arenaName, powerUps);
            }
            
            // Limpiar powerups removidos/expirados/recolectados
            synchronized (powerUps) {
                powerUps.removeIf(powerUp -> powerUp.isRemoved() || powerUp.isExpired() || powerUp.isCollected());
            }
            
            int activePowerUps = powerUps.size();
            
            if (activePowerUps >= MAX_POWERUPS_PER_ARENA) {
                return;
            }

            // Obtener zona PVP
            Zone pvpZone = arena.getZone("pvp");
            if (pvpZone == null) {
                return;
            }

            // ALGORITMO MEJORADO DE UBICACIÓN
            Location spawnLocation = getValidGroundLocationInZone(pvpZone, arenaName);
            if (spawnLocation == null) {
                return;
            }

            // Crear powerup aleatorio
            PowerUpType randomType = PowerUpType.getRandom();
            PowerUp powerUp = new PowerUp(randomType, spawnLocation, plugin);
            
            synchronized (powerUps) {
                powerUps.add(powerUp);
            }

            // Notificar a los jugadores
            for (Player player : playersInArena) {
                player.sendMessage(MessageUtils.getColor(
                        MysthicKnockBack.getPrefix() + 
                        plugin.getMainConfig().getPowerUpMessageAppeared()
                        .replace("%powerup%", randomType.getDisplayName())
                    ));
                player.playSound(player.getLocation(), Sound.NOTE_PLING, 0.8f, 1.2f);
            }
            
        } catch (Exception e) {
            plugin.getLogger().severe("Error spawning PowerUp in arena " + arenaName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Location getValidGroundLocationInZone(Zone zone, String arenaName) {
        Location min = zone.getMin();
        Location max = zone.getMax();
        
        // Lista para almacenar todas las ubicaciones válidas
        List<Location> validLocations = new ArrayList<>();
        
        // BÚSQUEDA EN GRILLA SISTEMÁTICA PRIMERO
        double stepX = Math.max(1.0, (max.getX() - min.getX()) / 10);
        double stepZ = Math.max(1.0, (max.getZ() - min.getZ()) / 10);
        
        for (double x = min.getX() + 1; x < max.getX() - 1; x += stepX) {
            for (double z = min.getZ() + 1; z < max.getZ() - 1; z += stepZ) {
                Location groundLocation = findBestGroundLocation(min.getWorld(), x, z, max.getY(), min.getY());
                if (groundLocation != null && canPlacePowerUpRelaxed(groundLocation, arenaName)) {
                    validLocations.add(groundLocation);
                }
            }
        }
        
        // Si encontramos ubicaciones en la grilla, elegir la mejor
        if (!validLocations.isEmpty()) {
            return getBestLocationFromList(validLocations, arenaName);
        }
        
        // BÚSQUEDA ALEATORIA COMO RESPALDO
        for (int attempt = 0; attempt < MAX_SPAWN_ATTEMPTS; attempt++) {
            double x = min.getX() + 1 + random.nextDouble() * (max.getX() - min.getX() - 2);
            double z = min.getZ() + 1 + random.nextDouble() * (max.getZ() - min.getZ() - 2);
            
            Location groundLocation = findBestGroundLocation(min.getWorld(), x, z, max.getY(), min.getY());
            if (groundLocation != null && canPlacePowerUpRelaxed(groundLocation, arenaName)) {
                validLocations.add(groundLocation);
            }
        }
        
        if (!validLocations.isEmpty()) {
            return getBestLocationFromList(validLocations, arenaName);
        }
        return null;
    }

    private Location getBestLocationFromList(List<Location> locations, String arenaName) {
        if (locations.isEmpty()) return null;
        if (locations.size() == 1) return locations.get(0);
        
        List<PowerUp> existingPowerUps = arenaPowerUps.get(arenaName);
        if (existingPowerUps == null || existingPowerUps.isEmpty()) {
            return locations.get(random.nextInt(locations.size()));
        }
        
        Location bestLocation = null;
        double bestMinDistance = 0;
        
        for (Location candidate : locations) {
            double minDistanceToOthers = Double.MAX_VALUE;
            
            synchronized (existingPowerUps) {
                for (PowerUp powerUp : existingPowerUps) {
                    if (powerUp.isRemoved()) continue;
                    double distance = candidate.distance(powerUp.getLocation());
                    if (distance < minDistanceToOthers) {
                        minDistanceToOthers = distance;
                    }
                }
            }
            
            if (minDistanceToOthers > bestMinDistance) {
                bestMinDistance = minDistanceToOthers;
                bestLocation = candidate;
            }
        }
        
        return bestLocation != null ? bestLocation : locations.get(0);
    }

    private Location findBestGroundLocation(World world, double x, double z, double maxY, double minY) {
        // Buscar desde arriba hacia abajo para encontrar la superficie
        for (double y = maxY - 1; y >= minY + 2; y--) {
            Location checkLocation = new Location(world, x, y, z);
            
            Block currentBlock = checkLocation.getBlock();
            Block belowBlock = currentBlock.getRelative(BlockFace.DOWN);
            Block aboveBlock = currentBlock.getRelative(BlockFace.UP);
            Block above2Block = aboveBlock.getRelative(BlockFace.UP);
            
            // Encontrar superficie: aire arriba, bloque sólido abajo
            if (currentBlock.getType() == Material.AIR && 
                aboveBlock.getType() == Material.AIR &&
                above2Block.getType() == Material.AIR &&
                belowBlock.getType().isSolid() && 
                !isUnsafeBlock(belowBlock.getType())) {
                
                return checkLocation;
            }
        }
        
        return null;
    }

    private boolean canPlacePowerUpRelaxed(Location location, String arenaName) {
        Block block = location.getBlock();
        Block below = block.getRelative(BlockFace.DOWN);
        
        // Verificar que el bloque actual sea aire
        if (block.getType() != Material.AIR) {
            return false;
        }
        
        // Verificar que haya un bloque sólido debajo y que no sea peligroso
        if (!below.getType().isSolid() || isUnsafeBlock(below.getType())) {
            return false;
        }

        // Verificar que haya suficiente espacio arriba
        Block above = block.getRelative(BlockFace.UP);
        if (above.getType() != Material.AIR) {
            return false;
        }

        // VERIFICACIÓN DE DISTANCIA RELAJADA
        double minimumDistance = MIN_DISTANCE_BETWEEN_POWERUPS;
        List<PowerUp> existingPowerUps = arenaPowerUps.get(arenaName);
        
        if (existingPowerUps != null) {
            synchronized (existingPowerUps) {
                for (PowerUp powerUp : existingPowerUps) {
                    if (powerUp.isRemoved() || powerUp.isExpired() || powerUp.isCollected()) continue;
                    
                    if (location.distance(powerUp.getLocation()) < minimumDistance) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private boolean isUnsafeBlock(Material material) {
        return material == Material.LAVA || 
                material == Material.STATIONARY_LAVA ||
                material == Material.FIRE || 
                material == Material.CACTUS ||
                material == Material.COBBLE_WALL ||
                material.name().contains("PRESSURE_PLATE") ||
                material.name().contains("TRIPWIRE");
    }

    private void cleanupExpiredPowerUps() {
        for (Map.Entry<String, List<PowerUp>> entry : arenaPowerUps.entrySet()) {
            List<PowerUp> powerUps = entry.getValue();
            
            if (powerUps == null) continue;
            
            synchronized (powerUps) {
                Iterator<PowerUp> iterator = powerUps.iterator();
                
                while (iterator.hasNext()) {
                    PowerUp powerUp = iterator.next();
                    
                    if (powerUp.isExpired() || powerUp.isCollected() || powerUp.isRemoved()) {
                        if (!powerUp.isRemoved()) {
                            powerUp.remove();
                        }
                        iterator.remove();
                    }
                }
            }
        }
    }

    public void cleanupArenaPowerUpsOnly(String arenaName) {
        // Solo remover los powerups existentes, NO cancelar tasks ni eliminar la arena del sistema
        List<PowerUp> powerUps = arenaPowerUps.get(arenaName);
        if (powerUps != null) {
            synchronized (powerUps) {
                for (PowerUp powerUp : powerUps) {
                    powerUp.remove(); // Solo remover visualmente
                }
                powerUps.clear(); // Limpiar la lista
            }
        }
    }

    public void cleanupAllPowerUpEntities() {
        for (World world : plugin.getServer().getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.hasMetadata("MysthicKnockBack_PowerUp")) {
                    entity.remove();
                } else if (entity instanceof Item) {
                    if (entity.hasMetadata("POWERUP_PROTECTED")) {
                        entity.remove();
                    }
                }
            }
        }
    }

    public void reactivateArena(String arenaName) {
        // Si la arena existe en el mapa pero no tiene task activo, reactivarla
        if (arenaPowerUps.containsKey(arenaName)) {
            BukkitTask currentTask = arenaSpawnTasks.get(arenaName);
            if (currentTask == null) {
                initializeArena(arenaName); // Esto creará un nuevo task
            }
        }
    }

    public void cleanupArena(String arenaName) {
        
        // Remover todos los powerups de la arena
        List<PowerUp> powerUps = arenaPowerUps.get(arenaName);
        if (powerUps != null) {
            synchronized (powerUps) {
                for (PowerUp powerUp : powerUps) {
                    powerUp.remove();
                }
                powerUps.clear();
            }
        }

        // Cancelar task de spawn
        BukkitTask spawnTask = arenaSpawnTasks.remove(arenaName);
        if (spawnTask != null) {
            spawnTask.cancel();
        }
    }

    public void shutdown() {
        
        // Limpiar todas las arenas
        for (String arenaName : new HashSet<>(arenaPowerUps.keySet())) {
            cleanupArena(arenaName);
        }

        // Cancelar todos los tasks
        arenaSpawnTasks.values().forEach(BukkitTask::cancel);
        arenaSpawnTasks.clear();
    }

    public void addArena(String arenaName) {
        initializeArena(arenaName);
    }

    public void removeArena(String arenaName) {
        cleanupArena(arenaName);
        arenaPowerUps.remove(arenaName);
    }

    public List<PowerUp> getPowerUpsInArena(String arenaName) {
        List<PowerUp> powerUps = arenaPowerUps.get(arenaName);
        if (powerUps == null) return new ArrayList<>();
        
        synchronized (powerUps) {
            return new ArrayList<>(powerUps);
        }
    }

    public void forcePowerUpSpawn(String arenaName) {
        new BukkitRunnable() {
            @Override
            public void run() {
                trySpawnPowerUp(arenaName);
            }
        }.runTask(plugin);
    }

    // Método para obtener estadísticas de powerups
    public int getTotalActivePowerUps() {
        return arenaPowerUps.values().stream()
                .mapToInt(list -> {
                    synchronized (list) {
                        return (int) list.stream()
                                .filter(p -> !p.isRemoved() && !p.isExpired() && !p.isCollected())
                                .count();
                    }
                })
                .sum();
    }

    public int getActivePowerUpsInArena(String arenaName) {
        List<PowerUp> powerUps = arenaPowerUps.get(arenaName);
        if (powerUps == null) return 0;
        
        synchronized (powerUps) {
            return (int) powerUps.stream()
                    .filter(p -> !p.isRemoved() && !p.isExpired() && !p.isCollected())
                    .count();
        }
    }

    public void clearAllPowerUpEffects(Player player) {
        // Remover efectos de pociones
        player.removePotionEffect(PotionEffectType.JUMP);
        player.removePotionEffect(PotionEffectType.INVISIBILITY);

        if (player.hasMetadata("explosive_arrow")) {
            player.removeMetadata("explosive_arrow", plugin);
        }

        if (player.hasMetadata("blackhole_active")) {
            player.removeMetadata("blackhole_active", plugin);
        }

        if (player.hasMetadata("double_pearl_powerup")) {
            player.removeMetadata("double_pearl_powerup", plugin);
        }

        plugin.getCombatManager().removePowerupKnockback(player);
    }
}