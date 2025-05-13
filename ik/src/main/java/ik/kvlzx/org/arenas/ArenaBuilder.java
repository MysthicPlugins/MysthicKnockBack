package ik.kvlzx.org.arenas;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ArenaBuilder {

    private final List<Location> arenaBlocks = new ArrayList<>();
    private final Random random = new Random();

    private static final Material[] BORDER_MATERIALS = {
        Material.STONE, Material.COBBLESTONE, Material.ANDESITE, Material.POLISHED_ANDESITE
    };

    private static final Material[] SURFACE_MATERIALS = {
        Material.GRASS, Material.DIRT, Material.WOOL, Material.WOOL, Material.WOOL
    };

    private static final byte[] SURFACE_DATA = {
        (byte) 0x5, (byte) 0x5, (byte) 0x2, (byte) 0xA, (byte) 0xD
    };

    public void buildArenaOnStart(World world, Location spawnPoint) {
        int centerX = spawnPoint.getBlockX();
        int centerY = spawnPoint.getBlockY();
        int centerZ = spawnPoint.getBlockZ();

        buildCircularPlatform(new Location(world, centerX, centerY, centerZ), 16, 5);
        buildRing(new Location(world, centerX, centerY, centerZ), 16, 36, 5);
        buildMiniIslands(new Location(world, centerX, centerY, centerZ), 20);
        arenaBlocks.clear(); // Limpiar duplicados
    }

    private void buildCircularPlatform(Location center, int radius, int borderHeight) {
        World world = center.getWorld();
        int centerX = center.getBlockX();
        int centerY = center.getBlockY();
        int centerZ = center.getBlockZ();

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z <= radius * radius) {
                    Location surfaceLoc = new Location(world, centerX + x, centerY, centerZ + z);
                    setRandomSurfaceBlock(surfaceLoc);
                    arenaBlocks.add(surfaceLoc);

                    Location supportLoc = new Location(world, centerX + x, centerY - 1, centerZ + z);
                    world.getBlockAt(supportLoc).setType(Material.DIRT);
                    arenaBlocks.add(supportLoc);

                    if (Math.abs(x) == radius || Math.abs(z) == radius) {
                        for (int y = 1; y <= borderHeight; y++) {
                            Location borderLoc = new Location(world, centerX + x, centerY + y, centerZ + z);
                            world.getBlockAt(borderLoc).setType(getRandomBorderMaterial());
                            arenaBlocks.add(borderLoc);
                        }
                    }
                }
            }
        }
    }

    private void buildRing(Location center, int innerRadius, int outerRadius, int borderHeight) {
        World world = center.getWorld();
        int centerX = center.getBlockX();
        int centerY = center.getBlockY();
        int centerZ = center.getBlockZ();

        for (int x = -outerRadius; x <= outerRadius; x++) {
            for (int z = -outerRadius; z <= outerRadius; z++) {
                int distance = (int) Math.sqrt(x * x + z * z);
                if (distance >= innerRadius && distance <= outerRadius) {
                    Location surfaceLoc = new Location(world, centerX + x, centerY, centerZ + z);
                    setRandomSurfaceBlock(surfaceLoc);
                    arenaBlocks.add(surfaceLoc);

                    Location supportLoc = new Location(world, centerX + x, centerY - 1, centerZ + z);
                    world.getBlockAt(supportLoc).setType(Material.DIRT);
                    arenaBlocks.add(supportLoc);

                    if (distance >= outerRadius - 1) {
                        for (int y = 1; y <= borderHeight; y++) {
                            Location borderLoc = new Location(world, centerX + x, centerY + y, centerZ + z);
                            world.getBlockAt(borderLoc).setType(getRandomBorderMaterial());
                            arenaBlocks.add(borderLoc);
                        }
                    }
                }
            }
        }
    }

    private void buildMiniIslands(Location center, int baseHeight) {
        World world = center.getWorld();
        int centerX = center.getBlockX();
        int centerZ = center.getBlockZ();

        for (int i = 0; i < 8; i++) {
            int offsetX = random.nextInt(40) - 20;
            int offsetZ = random.nextInt(40) - 20;
            int heightOffset = 15 + random.nextInt(6);
            Location islandCenter = new Location(world, centerX + offsetX, centerY + baseHeight + heightOffset, centerZ + offsetZ);
            buildMiniIsland(islandCenter, 2);
        }
    }

    private void buildMiniIsland(Location center, int radius) {
        World world = center.getWorld();
        int centerX = center.getBlockX();
        int centerY = center.getBlockY();
        int centerZ = center.getBlockZ();

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z <= radius * radius) {
                    Location surfaceLoc = new Location(world, centerX + x, centerY, centerZ + z);
                    setRandomSurfaceBlock(surfaceLoc);
                    arenaBlocks.add(surfaceLoc);

                    Location supportLoc = new Location(world, centerX + x, centerY - 1, centerZ + z);
                    world.getBlockAt(supportLoc).setType(Material.DIRT);
                    arenaBlocks.add(supportLoc);
                }
            }
        }
    }

    private void setRandomSurfaceBlock(Location loc) {
        int index = random.nextInt(SURFACE_MATERIALS.length);
        Block block = loc.getBlock();
        block.setType(SURFACE_MATERIALS[index]);
        if (SURFACE_DATA.length > index) {
            block.setData(SURFACE_DATA[index]);
        }
    }

    private Material getRandomBorderMaterial() {
        return BORDER_MATERIALS[random.nextInt(BORDER_MATERIALS.length)];
    }

    public List<Location> getArenaBlocks() {
        return new ArrayList<>(arenaBlocks);
    }

    public void clearArena(Location center, int radius) {
        World world = center.getWorld();
        int centerX = center.getBlockX();
        int centerY = center.getBlockY();
        int centerZ = center.getBlockZ();
        int clearRadius = 40;

        for (int x = -clearRadius; x <= clearRadius; x++) {
            for (int z = -clearRadius; z <= clearRadius; z++) {
                for (int y = -10; y <= 30; y++) {
                    Location loc = new Location(world, centerX + x, centerY + y, centerZ + z);
                    world.getBlockAt(loc).setType(Material.AIR);
                }
            }
        }
        arenaBlocks.clear();
    }
}