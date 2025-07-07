package mk.kvlzx.powerup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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
    private final Map<String, List<PowerUp>> arenaPowerUps; // Arena -> Lista de PowerUps
    private final Map<String, BukkitTask> arenaSpawnTasks; // Arena -> Task de spawn
    private final Map<String, BukkitTask> arenaCheckTasks; // Arena -> Task de verificación
    private final Random random;

    // Configuración
    private final int SPAWN_INTERVAL = 30; // Segundos entre spawns
    private final int MAX_POWERUPS_PER_ARENA = 3; // Máximo de powerups por arena
    private final int CHECK_INTERVAL = 10; // Ticks entre verificaciones de jugadores cerca
    private final int MAX_SPAWN_ATTEMPTS = 20; // Máximo de intentos para encontrar una ubicación válida

    public PowerUpManager(MysthicKnockBack plugin, ArenaManager arenaManager) {
        this.plugin = plugin;
        this.arenaManager = arenaManager;
        this.arenaPowerUps = new HashMap<>();
        this.arenaSpawnTasks = new HashMap<>();
        this.arenaCheckTasks = new HashMap<>();
        this.random = new Random();
        
        startPowerUpSystem();
    }

    private void startPowerUpSystem() {
        // Inicializar powerups para cada arena
        for (Arena arena : arenaManager.getArenas()) {
            initializeArena(arena.getName());
        }

        // Task principal para verificar jugadores cerca de powerups
        new BukkitRunnable() {
            @Override
            public void run() {
                checkPlayersNearPowerUps();
            }
        }.runTaskTimer(plugin, 0L, CHECK_INTERVAL);
    }

    public void initializeArena(String arenaName) {
        if (!arenaPowerUps.containsKey(arenaName)) {
            arenaPowerUps.put(arenaName, new ArrayList<>());
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
        if (powerUps.size() >= MAX_POWERUPS_PER_ARENA) return;

        // Obtener zona PVP
        Zone pvpZone = arena.getZone("pvp");
        if (pvpZone == null) return;

        // Generar ubicación válida en el suelo dentro de la zona PVP
        Location spawnLocation = getValidGroundLocationInZone(pvpZone);
        if (spawnLocation == null) return;

        // Crear powerup aleatorio
        PowerUpType randomType = PowerUpType.getRandom();
        PowerUp powerUp = new PowerUp(randomType, spawnLocation);
        powerUps.add(powerUp);

        // Notificar a los jugadores
        for (Player player : playersInArena) {
            player.sendMessage(MessageUtils.getColor("&e¡Ha aparecido un powerup en la arena!"));
        }
    }

    private Location getValidGroundLocationInZone(Zone zone) {
        Location min = zone.getMin();
        Location max = zone.getMax();
        
        // Intentar múltiples ubicaciones aleatorias dentro de la zona
        for (int attempt = 0; attempt < MAX_SPAWN_ATTEMPTS; attempt++) {
            double x = min.getX() + random.nextDouble() * (max.getX() - min.getX());
            double z = min.getZ() + random.nextDouble() * (max.getZ() - min.getZ());
            
            // Empezar desde la parte superior de la zona
            Location startLocation = new Location(min.getWorld(), x, max.getY(), z);
            
            // Buscar el suelo válido
            Location groundLocation = findGroundLocationInZone(startLocation, min.getY());
            if (groundLocation != null && canPlacePowerUp(groundLocation)) {
                return groundLocation;
            }
        }
        
        return null;
    }

    private Location findGroundLocationInZone(Location startLocation, double minY) {
        Location searchLocation = startLocation.clone();
        
        // Primero, subir hasta encontrar aire si estamos dentro de un bloque
        while (searchLocation.getBlock().getType() != Material.AIR && searchLocation.getY() < 256) {
            searchLocation.add(0, 1, 0);
        }
        
        // Luego, buscar hacia abajo hasta encontrar un bloque sólido
        for (double y = searchLocation.getY(); y >= minY; y--) {
            searchLocation.setY(y);
            Block currentBlock = searchLocation.getBlock();
            Block belowBlock = searchLocation.clone().add(0, -1, 0).getBlock();
            
            // Si el bloque actual es aire y el de abajo es sólido, esta es una buena ubicación
            if (currentBlock.getType() == Material.AIR && belowBlock.getType().isSolid()) {
                return searchLocation.clone();
            }
        }
        
        return null;
    }

    private boolean canPlacePowerUp(Location location) {
        Block block = location.getBlock();
        Block below = block.getRelative(BlockFace.DOWN);
        
        // Verificar que el bloque actual sea aire
        if (block.getType() != Material.AIR) {
            return false;
        }
        
        // Verificar que haya un bloque sólido debajo
        if (!below.getType().isSolid()) {
            return false;
        }

        // Verificar que no haya otros powerups muy cerca (opcional)
        List<PowerUp> nearbyPowerUps = getAllPowerUpsNearLocation(location, 3.0);
        if (!nearbyPowerUps.isEmpty()) {
            return false;
        }

        // Verificar que haya suficiente espacio arriba (2 bloques de altura)
        Block above = block.getRelative(BlockFace.UP);
        if (above.getType() != Material.AIR) {
            return false;
        }

        return true;
    }

    private List<PowerUp> getAllPowerUpsNearLocation(Location location, double distance) {
        List<PowerUp> nearbyPowerUps = new ArrayList<>();
        
        for (List<PowerUp> powerUps : arenaPowerUps.values()) {
            for (PowerUp powerUp : powerUps) {
                if (powerUp.getLocation().getWorld().equals(location.getWorld()) &&
                    powerUp.getLocation().distance(location) < distance) {
                    nearbyPowerUps.add(powerUp);
                }
            }
        }
        
        return nearbyPowerUps;
    }

    private void checkPlayersNearPowerUps() {
        for (Map.Entry<String, List<PowerUp>> entry : arenaPowerUps.entrySet()) {
            String arenaName = entry.getKey();
            List<PowerUp> powerUps = entry.getValue();
            Set<Player> playersInArena = arenaManager.getPlayersInArena(arenaName);

            // Verificar cada powerup
            Iterator<PowerUp> iterator = powerUps.iterator();
            while (iterator.hasNext()) {
                PowerUp powerUp = iterator.next();

                // Remover powerups expirados o recogidos
                if (powerUp.isExpired() || powerUp.isCollected()) {
                    powerUp.remove();
                    iterator.remove();
                    continue;
                }

                // Verificar si algún jugador está cerca
                for (Player player : playersInArena) {
                    if (powerUp.isNearPlayer(player)) {
                        powerUp.applyEffect(player);
                        iterator.remove();
                        break;
                    }
                }
            }
        }
    }

    public void cleanupArena(String arenaName) {
        // Remover todos los powerups de la arena
        List<PowerUp> powerUps = arenaPowerUps.get(arenaName);
        if (powerUps != null) {
            for (PowerUp powerUp : powerUps) {
                powerUp.remove();
            }
            powerUps.clear();
        }

        // Cancelar tasks
        BukkitTask spawnTask = arenaSpawnTasks.remove(arenaName);
        if (spawnTask != null) {
            spawnTask.cancel();
        }

        BukkitTask checkTask = arenaCheckTasks.remove(arenaName);
        if (checkTask != null) {
            checkTask.cancel();
        }
    }

    public void shutdown() {
        // Limpiar todas las arenas
        for (String arenaName : new HashSet<>(arenaPowerUps.keySet())) {
            cleanupArena(arenaName);
        }

        // Cancelar todos los tasks
        arenaSpawnTasks.values().forEach(BukkitTask::cancel);
        arenaCheckTasks.values().forEach(BukkitTask::cancel);
    }

    public void addArena(String arenaName) {
        initializeArena(arenaName);
    }

    public void removeArena(String arenaName) {
        cleanupArena(arenaName);
        arenaPowerUps.remove(arenaName);
    }

    public List<PowerUp> getPowerUpsInArena(String arenaName) {
        return arenaPowerUps.getOrDefault(arenaName, new ArrayList<>());
    }

    public void forcePowerUpSpawn(String arenaName) {
        trySpawnPowerUp(arenaName);
    }

    // Método para verificar si un jugador tiene el efecto de knockback especial
    public boolean hasKnockbackPowerUp(Player player) {
        // Acá se podria agregar lógica adicional para trackear efectos especiales
        return player.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE);
    }
}
