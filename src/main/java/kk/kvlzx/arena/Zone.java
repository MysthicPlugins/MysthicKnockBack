package kk.kvlzx.arena;

import org.bukkit.Location;

public class Zone {
    private Location min;
    private Location max;
    private ZoneType type;

    public Zone(Location min, Location max, ZoneType type) {
        this.min = min;
        this.max = max;
        this.type = type;
    }

    public Location getMin() {
        return min;
    }

    public Location getMax() {
        return max;
    }

    public ZoneType getType() {
        return type;
    }

    public boolean isInside(Location loc) {
        return loc.getX() >= min.getX() && loc.getX() <= max.getX() &&
                loc.getY() >= min.getY() && loc.getY() <= max.getY() &&
                loc.getZ() >= min.getZ() && loc.getZ() <= max.getZ();
    }
}
