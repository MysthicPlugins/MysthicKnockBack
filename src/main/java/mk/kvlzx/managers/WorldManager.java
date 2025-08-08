package mk.kvlzx.managers;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.generator.ChunkGenerator;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.utils.MessageUtils;

public class WorldManager {
    
    private final MysthicKnockBack plugin;
    
    public WorldManager(MysthicKnockBack plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Inicializa el WorldManager y crea el mundo KBFFA si no existe
     */
    public void initialize() {
        getOrCreateKBFFAWorld();
    }
    
    /**
     * Crea un mundo vacío llamado "kbffa" con configuraciones optimizadas para PvP
     */
    public World createKBFFAWorld() {
        String worldName = "kbffa";
        
        // Verificar si el mundo ya existe
        World existingWorld = Bukkit.getWorld(worldName);
        if (existingWorld != null) {
            return existingWorld;
        }
        
        try {
            MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&bℹ&8] &7Creating empty world '" + worldName + "'...");
            
            // Crear el mundo con generador vacío
            WorldCreator creator = new WorldCreator(worldName);
            creator.generator(new EmptyChunkGenerator());
            creator.type(WorldType.FLAT);
            creator.generateStructures(false);
            
            World world = creator.createWorld();
            
            if (world != null) {
                // Configurar el mundo para KBFFA
                setupWorldForKBFFA(world);
                
                MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&a✔&8] &aWorld '" + worldName + "' created successfully!");
                
                return world;
            } else {
                MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&c!&8] &cFailed to create world '" + worldName + "'");
                return null;
            }
            
        } catch (Exception e) {
            MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&c!&8] &cError creating world: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Configura el mundo para KBFFA con todas las configuraciones necesarias
     */
    private void setupWorldForKBFFA(World world) {
        // Configurar gamerules para PvP
        world.setGameRuleValue("doDaylightCycle", "false");
        // Configurar tiempo y clima
        world.setTime(6000); // Mediodía
        world.setStorm(false);
        world.setThundering(false);
        world.setWeatherDuration(999999);
        world.setThunderDuration(0);
        
        // Configurar spawn
        world.setSpawnLocation(0, 65, 0);
        
        // Crear una pequeña plataforma de spawn de bedrock
        createSpawnPlatform(world);
        
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&a✔&8] &7World gamerules and settings configured");
    }
    
    /**
     * Crea una pequeña plataforma de spawn en bedrock
     */
    private void createSpawnPlatform(World world) {
        Location spawnLoc = new Location(world, 0, 64, 0);
        
        // Crear una plataforma de 5x5 de bedrock
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                Location blockLoc = spawnLoc.clone().add(x, 0, z);
                world.getBlockAt(blockLoc).setType(Material.BEDROCK);
            }
        }
    }
    
    /**
     * Obtiene el mundo KBFFA o lo crea si no existe
     */
    public World getOrCreateKBFFAWorld() {
        World world = Bukkit.getWorld("kbffa");
        if (world == null) {
            world = createKBFFAWorld();
        }
        return world;
    }
    
    /**
     * Generador de chunks vacíos
     */
    public static class EmptyChunkGenerator extends ChunkGenerator {
        
        @Override
        public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
            // Retorna un chunk completamente vacío (solo aire)
            return createChunkData(world);
        }
        
        @Override
        public boolean canSpawn(World world, int x, int z) {
            return true;
        }
        
        @Override
        public Location getFixedSpawnLocation(World world, Random random) {
            return new Location(world, 0, 65, 0);
        }
    }
}