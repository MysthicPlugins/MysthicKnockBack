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
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.enchantments.Enchantment;
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
    private final int SPAWN_INTERVAL = 25; // Segundos entre spawns
    private final int MAX_POWERUPS_PER_ARENA = 4; // Máximo de powerups por arena
    private final int CHECK_INTERVAL = 5; // Ticks entre verificaciones
    private final int MAX_SPAWN_ATTEMPTS = 30; // Máximo de intentos para encontrar una ubicación válida
    private final double MIN_DISTANCE_BETWEEN_POWERUPS = 4.0; // Distancia mínima entre powerups

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

        // Task principal para cleanup y verificación
        new BukkitRunnable() {
            @Override
            public void run() {
                cleanupExpiredPowerUps();
            }
        }.runTaskTimer(plugin, 0L, CHECK_INTERVAL * 4);
    }

    public void initializeArena(String arenaName) {
        if (!arenaPowerUps.containsKey(arenaName)) {
            arenaPowerUps.put(arenaName, Collections.synchronizedList(new ArrayList<>()));
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
        Arena arena = arenaManager.getArena(arenaName);
        if (arena == null) return;

        // Verificar si hay jugadores en la arena
        Set<Player> playersInArena = arenaManager.getPlayersInArena(arenaName);
        if (playersInArena.isEmpty()) return;

        // Verificar si ya hay suficientes powerups
        List<PowerUp> powerUps = arenaPowerUps.get(arenaName);
        if (powerUps == null) return;
        
        // Limpiar powerups removidos de la lista
        powerUps.removeIf(PowerUp::isRemoved);
        
        if (powerUps.size() >= MAX_POWERUPS_PER_ARENA) return;

        // Obtener zona PVP
        Zone pvpZone = arena.getZone("pvp");
        if (pvpZone == null) return;

        // Generar ubicación válida en el suelo dentro de la zona PVP
        Location spawnLocation = getValidGroundLocationInZone(pvpZone, arenaName);
        if (spawnLocation == null) return;

        // Crear powerup aleatorio
        PowerUpType randomType = PowerUpType.getRandom();
        PowerUp powerUp = new PowerUp(randomType, spawnLocation, plugin);
        powerUps.add(powerUp);

        // Notificar a los jugadores
        for (Player player : playersInArena) {
            player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + "&eA " + randomType.getDisplayName() + " &epowerup has appeared in the arena!"));
            player.playSound(player.getLocation(), Sound.NOTE_PLING, 0.8f, 1.2f);
        }
    }

    private Location getValidGroundLocationInZone(Zone zone, String arenaName) {
        Location min = zone.getMin();
        Location max = zone.getMax();
        
        // Generar múltiples candidatos y elegir el mejor
        List<Location> candidates = new ArrayList<>();
        
        for (int attempt = 0; attempt < MAX_SPAWN_ATTEMPTS; attempt++) {
            double x = min.getX() + random.nextDouble() * (max.getX() - min.getX());
            double z = min.getZ() + random.nextDouble() * (max.getZ() - min.getZ());
            
            // Buscar desde arriba hacia abajo
            Location groundLocation = findBestGroundLocation(min.getWorld(), x, z, max.getY(), min.getY());
            if (groundLocation != null && canPlacePowerUp(groundLocation, arenaName)) {
                candidates.add(groundLocation);
            }
        }
        
        // Si no hay candidatos, intentar con criterios más relajados
        if (candidates.isEmpty()) {
            return findFallbackLocation(zone, arenaName);
        }
        
        // Elegir el mejor candidato (más alejado de otros powerups)
        return getBestCandidateLocation(candidates, arenaName);
    }

    private Location findBestGroundLocation(org.bukkit.World world, double x, double z, double maxY, double minY) {
        Location checkLocation = new Location(world, x, minY, z);
        
        // Buscar desde abajo hacia arriba para encontrar el primer espacio válido
        for (double y = minY; y <= maxY - 2; y++) {
            checkLocation.setY(y);
            
            Block currentBlock = checkLocation.getBlock();
            Block aboveBlock = currentBlock.getRelative(BlockFace.UP);
            Block above2Block = aboveBlock.getRelative(BlockFace.UP);
            
            // Verificar si tenemos un bloque sólido como base
            if (currentBlock.getType().isSolid() && 
                !isUnsafeBlock(currentBlock.getType()) &&
                aboveBlock.getType() == Material.AIR && 
                above2Block.getType() == Material.AIR) {
                
                // Retornar la posición encima del bloque sólido
                return checkLocation.clone().add(0, 1, 0);
            }
        }
        
        return null;
    }

    private Location findFallbackLocation(Zone zone, String arenaName) {
        Location min = zone.getMin();
        Location max = zone.getMax();
        
        // Buscar en una grilla más sistemática
        double stepX = (max.getX() - min.getX()) / 5;
        double stepZ = (max.getZ() - min.getZ()) / 5;
        
        for (double x = min.getX() + stepX; x < max.getX(); x += stepX) {
            for (double z = min.getZ() + stepZ; z < max.getZ(); z += stepZ) {
                Location groundLocation = findBestGroundLocation(min.getWorld(), x, z, max.getY(), min.getY());
                if (groundLocation != null && canPlacePowerUpRelaxed(groundLocation, arenaName)) {
                    return groundLocation;
                }
            }
        }
        
        return null;
    }

    private Location getBestCandidateLocation(List<Location> candidates, String arenaName) {
        if (candidates.isEmpty()) return null;
        if (candidates.size() == 1) return candidates.get(0);
        
        List<PowerUp> existingPowerUps = arenaPowerUps.get(arenaName);
        if (existingPowerUps == null || existingPowerUps.isEmpty()) {
            return candidates.get(random.nextInt(candidates.size()));
        }
        
        Location bestLocation = null;
        double bestMinDistance = 0;
        
        for (Location candidate : candidates) {
            double minDistance = Double.MAX_VALUE;
            
            for (PowerUp powerUp : existingPowerUps) {
                if (powerUp.isRemoved()) continue;
                double distance = candidate.distance(powerUp.getLocation());
                if (distance < minDistance) {
                    minDistance = distance;
                }
            }
            
            if (minDistance > bestMinDistance) {
                bestMinDistance = minDistance;
                bestLocation = candidate;
            }
        }
        
        return bestLocation != null ? bestLocation : candidates.get(0);
    }

    private boolean canPlacePowerUp(Location location, String arenaName) {
        return canPlacePowerUpRelaxed(location, arenaName) && 
                getAllPowerUpsNearLocation(location, MIN_DISTANCE_BETWEEN_POWERUPS, arenaName).isEmpty();
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

        // Verificar que no esté muy cerca de otros powerups
        List<PowerUp> nearbyPowerUps = getAllPowerUpsNearLocation(location, 2.0, arenaName);
        if (nearbyPowerUps.size() > 0) {
            return false;
        }

        return true;
    }

    private boolean isUnsafeBlock(Material material) {
        return material == Material.LAVA || 
                material == Material.FIRE || 
                material == Material.CACTUS ||
                material == Material.COBBLE_WALL ||
                material.name().contains("PRESSURE_PLATE") ||
                material.name().contains("TRIPWIRE");
    }

    private List<PowerUp> getAllPowerUpsNearLocation(Location location, double distance, String arenaName) {
        List<PowerUp> nearbyPowerUps = new ArrayList<>();
        List<PowerUp> arenaPowerUpList = arenaPowerUps.get(arenaName);
        
        if (arenaPowerUpList != null) {
            for (PowerUp powerUp : arenaPowerUpList) {
                if (powerUp.isRemoved()) continue;
                if (powerUp.getLocation().getWorld().equals(location.getWorld()) &&
                    powerUp.getLocation().distance(location) < distance) {
                    nearbyPowerUps.add(powerUp);
                }
            }
        }
        
        return nearbyPowerUps;
    }

    private void cleanupExpiredPowerUps() {
        for (Map.Entry<String, List<PowerUp>> entry : arenaPowerUps.entrySet()) {
            List<PowerUp> powerUps = entry.getValue();
            
            synchronized (powerUps) {
                Iterator<PowerUp> iterator = powerUps.iterator();
                while (iterator.hasNext()) {
                    PowerUp powerUp = iterator.next();
                    
                    if (powerUp.isExpired() || powerUp.isCollected() || powerUp.isRemoved()) {
                        powerUp.remove();
                        iterator.remove();
                    }
                }
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
        return powerUps != null ? new ArrayList<>(powerUps) : new ArrayList<>();
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
                .mapToInt(List::size)
                .sum();
    }

    public int getActivePowerUpsInArena(String arenaName) {
        List<PowerUp> powerUps = arenaPowerUps.get(arenaName);
        if (powerUps == null) return 0;
        
        synchronized (powerUps) {
            return (int) powerUps.stream()
                    .filter(p -> !p.isRemoved() && !p.isExpired())
                    .count();
        }
    }

    public void clearAllPowerUpEffects(Player player) {
        // Remover efectos de pociones
        player.removePotionEffect(PotionEffectType.JUMP);
        player.removePotionEffect(PotionEffectType.INVISIBILITY);

        // Resetear knocker
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && item.containsEnchantment(Enchantment.KNOCKBACK)) {
                int defaultKnockbackLevel = MysthicKnockBack.getInstance().getMainConfig().getKnockerKnockbackLevel();
                ItemStack defaultKnocker = item.clone();
                defaultKnocker.removeEnchantment(Enchantment.KNOCKBACK);
                if (defaultKnockbackLevel > 0) {
                    defaultKnocker.addUnsafeEnchantment(Enchantment.KNOCKBACK, defaultKnockbackLevel);
                }
                player.getInventory().setItem(i, defaultKnocker);
                break;
            }
        }
        player.updateInventory();
    }
}