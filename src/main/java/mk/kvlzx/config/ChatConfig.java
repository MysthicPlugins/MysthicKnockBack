package mk.kvlzx.config;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.utils.config.CustomConfig;

public class ChatConfig {
    private final CustomConfig configFile;
    private final MysthicKnockBack plugin;

    // Chat configuration
    private Boolean chatEnabled;
    private String defaultFormat;
    private Map<String, String> groupFormats;

    // Tab configuration
    private Boolean tabEnabled;
    private String tabDefaultFormat;
    private String tabDefaultDisplayName;
    private LinkedHashMap<String, TabGroupFormat> tabGroupFormats;

    // Join messages configuration
    private Boolean joinMessagesEnabled;
    private Map<String, String> groupJoinMessages;

    public ChatConfig(MysthicKnockBack plugin) {
        this.plugin = plugin;
        configFile = new CustomConfig("chat.yml", "config/global", plugin);
        configFile.registerConfig();
        loadConfig();
    }

    public void loadConfig() {
        FileConfiguration config = configFile.getConfig();

        // Cargar configuración de chat
        loadChatConfig(config);
        
        // Cargar configuración de tab
        loadTabConfig(config);
        
        // Cargar configuración de join messages
        loadJoinMessagesConfig(config);
    }

    private void loadChatConfig(FileConfiguration config) {
        chatEnabled = config.getBoolean("chat.enabled", true);
        defaultFormat = config.getString("chat.default-format", "&7%player_name%&7: &f%message%");

        // Cargar formatos por grupo para chat
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

    private void loadTabConfig(FileConfiguration config) {
        tabEnabled = config.getBoolean("tab.enabled", true);
        tabDefaultFormat = config.getString("tab.default-format", "%kbffa_rank% &f%player_name% &8[&f%player_ping%ms&8]");
        tabDefaultDisplayName = config.getString("tab.default-display-name", "%kbffa_rank% &f%player_name%");

        // LIMPIAR COMPLETAMENTE el LinkedHashMap antes de recargar
        tabGroupFormats = new LinkedHashMap<>();
        
        if (config.contains("tab.group-formats")) {
            ConfigurationSection tabGroupFormatsSection = config.getConfigurationSection("tab.group-formats");
            if (tabGroupFormatsSection != null) {
                // IMPORTANTE: Obtener las keys en el orden exacto que aparecen en el archivo YAML
                for (String groupName : tabGroupFormatsSection.getKeys(false)) {
                    ConfigurationSection groupSection = tabGroupFormatsSection.getConfigurationSection(groupName);
                    if (groupSection != null) {
                        // Nueva estructura: cada grupo tiene tab-format y display-name
                        String tabFormat = groupSection.getString("tab-format");
                        String displayName = groupSection.getString("display-name");
                        
                        if (tabFormat != null && !tabFormat.trim().isEmpty()) {
                            // Si no hay display-name, usar el tab-format sin ping como fallback
                            if (displayName == null || displayName.trim().isEmpty()) {
                                displayName = tabFormat.replaceAll("\\s*&8\\[&f%player_ping%ms&8\\]", "").trim();
                            }
                            
                            // MANTENER EL ORDEN: usar put directamente en el LinkedHashMap
                            tabGroupFormats.put(groupName.toLowerCase(), new TabGroupFormat(tabFormat, displayName));
                        }
                    } else {
                        // Soporte para formato legacy (string directo)
                        String legacyFormat = tabGroupFormatsSection.getString(groupName);
                        if (legacyFormat != null && !legacyFormat.trim().isEmpty()) {
                            String displayName = legacyFormat.replaceAll("\\s*&8\\[&f%player_ping%ms&8\\]", "").trim();
                            tabGroupFormats.put(groupName.toLowerCase(), new TabGroupFormat(legacyFormat, displayName));
                        }
                    }
                }
            }
        }
        
        // Si no hay formato para 'default', usar los defaults
        if (!tabGroupFormats.containsKey("default")) {
            tabGroupFormats.put("default", new TabGroupFormat(tabDefaultFormat, tabDefaultDisplayName));
        }
    }

    private void loadJoinMessagesConfig(FileConfiguration config) {
        joinMessagesEnabled = config.getBoolean("join-messages.enabled", true);
        
        // Cargar mensajes por grupo para join messages
        groupJoinMessages = new HashMap<>();
        if (config.contains("join-messages.group-messages")) {
            ConfigurationSection groupJoinMessagesSection = config.getConfigurationSection("join-messages.group-messages");
            if (groupJoinMessagesSection != null) {
                for (String groupName : groupJoinMessagesSection.getKeys(false)) {
                    String message = groupJoinMessagesSection.getString(groupName);
                    if (message != null && !message.trim().isEmpty()) {
                        groupJoinMessages.put(groupName.toLowerCase(), message);
                    }
                }
            }
        }
    }

    public void reload() {
        configFile.reloadConfig();
        
        // IMPORTANTE: Limpiar completamente las estructuras antes de recargar
        if (groupFormats != null) {
            groupFormats.clear();
        }
        if (tabGroupFormats != null) {
            tabGroupFormats.clear();
        }
        if (groupJoinMessages != null) {
            groupJoinMessages.clear();
        }
        
        // Recargar completamente la configuración
        loadConfig();
    }

    // ======== CHAT GETTERS ========
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

    // ======== TAB GETTERS ========
    public Boolean isTabEnabled() {
        return tabEnabled;
    }

    public String getTabDefaultFormat() {
        return tabDefaultFormat;
    }

    public String getTabDefaultDisplayName() {
        return tabDefaultDisplayName;
    }

    public String getTabGroupFormat(String groupName) {
        if (groupName == null || groupName.trim().isEmpty()) {
            return getTabDefaultFormat();
        }
        
        TabGroupFormat format = tabGroupFormats.get(groupName.toLowerCase());
        return format != null ? format.getTabFormat() : getTabDefaultFormat();
    }

    public String getTabGroupDisplayName(String groupName) {
        if (groupName == null || groupName.trim().isEmpty()) {
            return getTabDefaultDisplayName();
        }
        
        TabGroupFormat format = tabGroupFormats.get(groupName.toLowerCase());
        return format != null ? format.getDisplayName() : getTabDefaultDisplayName();
    }

    public LinkedHashMap<String, TabGroupFormat> getAllTabGroupFormats() {
        return new LinkedHashMap<>(tabGroupFormats);
    }

    public boolean hasTabGroupFormat(String groupName) {
        if (groupName == null || groupName.trim().isEmpty()) {
            return false;
        }
        
        return tabGroupFormats.containsKey(groupName.toLowerCase());
    }

    // Método MEJORADO para obtener la prioridad de un grupo basado en su orden en la configuración
    public int getGroupPriority(String groupName) {
        if (groupName == null || groupName.trim().isEmpty()) {
            return Integer.MAX_VALUE; // Prioridad más baja para grupos sin nombre
        }
        
        // Buscar en el LinkedHashMap que mantiene el orden
        int priority = 0;
        for (String configuredGroup : tabGroupFormats.keySet()) {
            if (configuredGroup.equals(groupName.toLowerCase())) {
                return priority;
            }
            priority++;
        }
        
        return Integer.MAX_VALUE; // Si no se encuentra, dar prioridad más baja
    }

    // Método para obtener todos los grupos ordenados por prioridad
    public String[] getGroupsByPriority() {
        return tabGroupFormats.keySet().toArray(new String[0]);
    }

    // ======== JOIN MESSAGES GETTERS ========
    public Boolean isJoinMessagesEnabled() {
        return joinMessagesEnabled;
    }

    public String getGroupJoinMessage(String groupName) {
        if (groupName == null || groupName.trim().isEmpty()) {
            return null; // Sin mensaje personalizado, usar el sistema default
        }
        
        return groupJoinMessages.get(groupName.toLowerCase());
    }

    public Map<String, String> getAllGroupJoinMessages() {
        return new HashMap<>(groupJoinMessages);
    }

    public boolean hasGroupJoinMessage(String groupName) {
        if (groupName == null || groupName.trim().isEmpty()) {
            return false;
        }
        
        return groupJoinMessages.containsKey(groupName.toLowerCase());
    }

    // ======== CHAT SETTERS ========
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

    // ======== TAB SETTERS ========
    public void setTabEnabled(boolean enabled) {
        this.tabEnabled = enabled;
        configFile.getConfig().set("tab.enabled", enabled);
        configFile.saveConfig();
    }

    public void setTabDefaultFormat(String format) {
        this.tabDefaultFormat = format;
        configFile.getConfig().set("tab.default-format", format);
        configFile.saveConfig();
    }

    public void setTabDefaultDisplayName(String displayName) {
        this.tabDefaultDisplayName = displayName;
        configFile.getConfig().set("tab.default-display-name", displayName);
        configFile.saveConfig();
    }

    public void setTabGroupFormat(String groupName, String tabFormat, String displayName) {
        if (groupName != null && !groupName.trim().isEmpty()) {
            tabGroupFormats.put(groupName.toLowerCase(), new TabGroupFormat(tabFormat, displayName));
            configFile.getConfig().set("tab.group-formats." + groupName.toLowerCase() + ".tab-format", tabFormat);
            configFile.getConfig().set("tab.group-formats." + groupName.toLowerCase() + ".display-name", displayName);
            configFile.saveConfig();
        }
    }

    public void removeTabGroupFormat(String groupName) {
        if (groupName != null && !groupName.trim().isEmpty()) {
            tabGroupFormats.remove(groupName.toLowerCase());
            configFile.getConfig().set("tab.group-formats." + groupName.toLowerCase(), null);
            configFile.saveConfig();
        }
    }

    // ======== JOIN MESSAGES SETTERS ========
    public void setJoinMessagesEnabled(boolean enabled) {
        this.joinMessagesEnabled = enabled;
        configFile.getConfig().set("join-messages.enabled", enabled);
        configFile.saveConfig();
    }

    public void setGroupJoinMessage(String groupName, String message) {
        if (groupName != null && !groupName.trim().isEmpty()) {
            if (message != null && !message.trim().isEmpty()) {
                groupJoinMessages.put(groupName.toLowerCase(), message);
                configFile.getConfig().set("join-messages.group-messages." + groupName.toLowerCase(), message);
            } else {
                groupJoinMessages.remove(groupName.toLowerCase());
                configFile.getConfig().set("join-messages.group-messages." + groupName.toLowerCase(), null);
            }
            configFile.saveConfig();
        }
    }

    public void removeGroupJoinMessage(String groupName) {
        if (groupName != null && !groupName.trim().isEmpty()) {
            groupJoinMessages.remove(groupName.toLowerCase());
            configFile.getConfig().set("join-messages.group-messages." + groupName.toLowerCase(), null);
            configFile.saveConfig();
        }
    }

    // ======== UTILITY METHODS ========
    
    // Método para obtener el formato efectivo de chat (considerando jerarquía)
    public String getEffectiveChatFormat(String primaryGroup) {
        String format = getGroupFormat(primaryGroup);
        
        if (format.equals(defaultFormat) && !primaryGroup.equals("default")) {
            format = getGroupFormat("default");
        }
        
        return format;
    }

    // Método para obtener el formato efectivo de tab (considerando jerarquía)
    public String getEffectiveTabFormat(String primaryGroup) {
        String format = getTabGroupFormat(primaryGroup);
        
        if (format.equals(tabDefaultFormat) && !primaryGroup.equals("default")) {
            format = getTabGroupFormat("default");
        }
        
        return format;
    }

    // Método para obtener el display name efectivo (considerando jerarquía)
    public String getEffectiveTabDisplayName(String primaryGroup) {
        String displayName = getTabGroupDisplayName(primaryGroup);
        
        if (displayName.equals(tabDefaultDisplayName) && !primaryGroup.equals("default")) {
            displayName = getTabGroupDisplayName("default");
        }
        
        return displayName;
    }

    // Método para obtener el mensaje de join efectivo (considerando jerarquía)
    public String getEffectiveJoinMessage(String primaryGroup) {
        return getGroupJoinMessage(primaryGroup);
    }

    // Método para validar formato (verificar placeholders básicos)
    public boolean isValidChatFormat(String format) {
        if (format == null || format.trim().isEmpty()) {
            return false;
        }
        
        return format.contains("%message%");
    }

    public boolean isValidTabFormat(String format) {
        if (format == null || format.trim().isEmpty()) {
            return false;
        }
        
        return format.contains("%player_name%");
    }

    public boolean isValidJoinMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return false;
        }
        
        return message.contains("%player%") || message.contains("%player_name%");
    }

    // Método para listar todos los grupos disponibles
    public String[] getAvailableChatGroups() {
        return groupFormats.keySet().toArray(new String[0]);
    }

    public String[] getAvailableTabGroups() {
        return tabGroupFormats.keySet().toArray(new String[0]);
    }

    public String[] getAvailableJoinMessageGroups() {
        return groupJoinMessages.keySet().toArray(new String[0]);
    }

    // Método para contar formatos configurados
    public int getChatGroupFormatCount() {
        return groupFormats.size();
    }

    public int getTabGroupFormatCount() {
        return tabGroupFormats.size();
    }

    public int getJoinMessageGroupCount() {
        return groupJoinMessages.size();
    }

    // Clase interna para representar formatos de tab
    public static class TabGroupFormat {
        private final String tabFormat;
        private final String displayName;

        public TabGroupFormat(String tabFormat, String displayName) {
            this.tabFormat = tabFormat != null ? tabFormat : "";
            this.displayName = displayName != null ? displayName : "";
        }

        public String getTabFormat() {
            return tabFormat;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}