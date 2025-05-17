package kk.kvlzx.arena;

public enum ZoneType {
    SPAWN("spawn"),
    PVP("pvp"),
    VOID("void");

    private final String id;

    ZoneType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static ZoneType fromString(String text) {
        for (ZoneType type : ZoneType.values()) {
            if (type.getId().equalsIgnoreCase(text)) {
                return type;
            }
        }
        return null;
    }
}
