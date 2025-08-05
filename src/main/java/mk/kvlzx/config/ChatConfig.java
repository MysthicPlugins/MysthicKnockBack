package mk.kvlzx.config;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.utils.config.CustomConfig;

public class ChatConfig {
    private final CustomConfig configFile;
    private final MysthicKnockBack plugin;

    private Boolean chatEnabled;
    private String defaultFormat;
    private Map<String, String> groupFormats;

    public ChatConfig(MysthicKnockBack plugin) {
        this.plugin = plugin;
        configFile = new CustomConfig("chat.yml", "config/global", plugin);
        configFile.registerConfig();
        loadConfig();
    }

    public void loadConfig() {
        FileConfiguration config = configFile.getConfig();

        // Cargar configuración básica
        chatEnabled = config.getBoolean("chat.enabled", true);
        defaultFormat = config.getString("chat.default-format", "&7%player_name%&7: &f%message%");

        // Cargar formatos por grupo
        loadGroupFormats(config);
    }

    private void loadGroupFormats(FileConfiguration config) {
        groupFormats = new HashMap<>();
        
        if (config.contains("chat.group-formats")) {
            ConfigurationSection groupFormatsSection = config.getConfigurationSection("chat.group-formats");
            if (groupFormatsSection != null) {
                for (String groupName : groupFormatsSection.getKeys(false)) {
                    String format = groupFormatsSection.getString(groupName);
                    if (format != null && !format.trim().isEmpty()) {
                        groupFormats.put(groupName.toLowerCase(), format);
                    }
                }
            }
        }
        
        // Si no hay formato para 'default', usar el defaultFormat
        if (!groupFormats.containsKey("default")) {
            groupFormats.put("default", defaultFormat);
        }
    }

    public void reload() {
        configFile.reloadConfig();
        loadConfig();
    }

    // Getters
    public Boolean isChatEnabled() {
        return chatEnabled;
    }

    public String getDefaultFormat() {
        return defaultFormat;
    }

    public String getGroupFormat(String groupName) {
        if (groupName == null || groupName.trim().isEmpty()) {
            return getDefaultFormat();
        }
        
        String format = groupFormats.get(groupName.toLowerCase());
        return format != null ? format : getDefaultFormat();
    }

    public Map<String, String> getAllGroupFormats() {
        return new HashMap<>(groupFormats);
    }

    public boolean hasGroupFormat(String groupName) {
        if (groupName == null || groupName.trim().isEmpty()) {
            return false;
        }
        
        return groupFormats.containsKey(groupName.toLowerCase());
    }

    // Setters para modificar configuración programáticamente
    public void setChatEnabled(boolean enabled) {
        this.chatEnabled = enabled;
        configFile.getConfig().set("chat.enabled", enabled);
        configFile.saveConfig();
    }

    public void setDefaultFormat(String format) {
        this.defaultFormat = format;
        configFile.getConfig().set("chat.default-format", format);
        configFile.saveConfig();
    }

    public void setGroupFormat(String groupName, String format) {
        if (groupName != null && !groupName.trim().isEmpty()) {
            groupFormats.put(groupName.toLowerCase(), format);
            configFile.getConfig().set("chat.group-formats." + groupName.toLowerCase(), format);
            configFile.saveConfig();
        }
    }

    public void removeGroupFormat(String groupName) {
        if (groupName != null && !groupName.trim().isEmpty()) {
            groupFormats.remove(groupName.toLowerCase());
            configFile.getConfig().set("chat.group-formats." + groupName.toLowerCase(), null);
            configFile.saveConfig();
        }
    }

    // Método para obtener el formato efectivo (considerando jerarquía)
    public String getEffectiveFormat(String primaryGroup) {
        // Primero intentar con el grupo primario
        String format = getGroupFormat(primaryGroup);
        
        // Si no existe formato específico, usar el default
        if (format.equals(defaultFormat) && !primaryGroup.equals("default")) {
            format = getGroupFormat("default");
        }
        
        return format;
    }

    // Método para validar formato (verificar placeholders básicos)
    public boolean isValidFormat(String format) {
        if (format == null || format.trim().isEmpty()) {
            return false;
        }
        
        // Verificar que contenga al menos el placeholder del mensaje
        return format.contains("%message%");
    }

    // Método para listar todos los grupos disponibles
    public String[] getAvailableGroups() {
        return groupFormats.keySet().toArray(new String[0]);
    }

    // Método para contar formatos configurados
    public int getGroupFormatCount() {
        return groupFormats.size();
    }
}
