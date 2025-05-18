package kk.kvlzx.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class LocationUtils {
    
    public static String serialize(Location loc) {
        if (loc == null) return null;
        return String.format("%s,%f,%f,%f,%f,%f",
            loc.getWorld().getName(),
            loc.getX(),
            loc.getY(),
            loc.getZ(),
            loc.getYaw(),
            loc.getPitch());
    }

    public static Location deserialize(String str) {
        if (str == null) return null;
        String[] parts = str.split(",");
        if (parts.length != 6) return null;
        
        try {
            return new Location(
                Bukkit.getWorld(parts[0]),
                Double.parseDouble(parts[1]),
                Double.parseDouble(parts[2]),
                Double.parseDouble(parts[3]),
                Float.parseFloat(parts[4]),
                Float.parseFloat(parts[5])
            );
        } catch (Exception e) {
            return null;
        }
    }
}
