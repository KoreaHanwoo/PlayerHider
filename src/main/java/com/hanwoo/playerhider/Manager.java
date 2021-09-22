package com.hanwoo.playerhider;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("FieldMayBeFinal")
public class Manager {
    private PlayerHider plugin;
    private ProfileChanger changer = new ProfileChanger();
    private HashMap<UUID, String> nameMap = new HashMap<>();
    private HashMap<UUID, Long> deathMap = new HashMap<>();
    private File nameMapFile;
    private FileConfiguration nameMapConfig;
    private File deathMapFile;
    private FileConfiguration deathMapConfig;
    public String confName;
    public String confTextures;
    public String confSignature;
    public int confPing;
    public boolean confLogConsole;
    public boolean confLogOp;
    public boolean confNoJoinInvincibility;
    public boolean confHideNameTag;
    public boolean confHideSpectators;
    public boolean confDeathOpBypass;
    public boolean confDeathBan;
    public long confDeathBanTime;
    public String confDeathMessage;
    public boolean confForceGameRule;
    public boolean confKillCommand;
    public boolean confEventOpBypass;
    public boolean confDisableLoom;
    public boolean confDisableSign;
    public boolean confDisableBook;
    public boolean confDisableAnvil;
    public String confDefaultNameTag;
    public boolean confHideCommand;
    public boolean confDisableCommand;
    public boolean confDisableLeatherDye;
    public boolean confDisableBannerShield;

    public Manager(PlayerHider plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.reloadConfig();
        nameMapFile = new File(plugin.getDataFolder(), "name.yml");
        nameMapConfig = YamlConfiguration.loadConfiguration(nameMapFile);
        deathMapFile = new File(plugin.getDataFolder(), "death.yml");
        deathMapConfig = YamlConfiguration.loadConfiguration(deathMapFile);

        confName = plugin.getConfig().getString("profile.name", "???");
        confTextures = plugin.getConfig().getString("profile.textures", "");
        confSignature = plugin.getConfig().getString("profile.signature", "");
        confPing = plugin.getConfig().getInt("profile.ping", 0);
        confLogConsole = plugin.getConfig().getBoolean("log.console", true);
        confLogOp = plugin.getConfig().getBoolean("log.op", true);
        confNoJoinInvincibility = plugin.getConfig().getBoolean("noJoinInvincibility", true);
        confHideNameTag = plugin.getConfig().getBoolean("hideNameTag", true);
        confHideSpectators = plugin.getConfig().getBoolean("hideSpectators", true);
        confDeathOpBypass = plugin.getConfig().getBoolean("death.opBypass", true);
        confDeathBan = plugin.getConfig().getBoolean("death.ban", true);
        confDeathBanTime = plugin.getConfig().getLong("death.time", 60000);
        confDeathMessage = plugin.getConfig().getString("death.message", "");
        confForceGameRule = plugin.getConfig().getBoolean("forceGameRule", true);
        confKillCommand = plugin.getConfig().getBoolean("killCommand", true);
        confEventOpBypass = plugin.getConfig().getBoolean("event.opBypass", true);
        confDisableLoom = plugin.getConfig().getBoolean("event.disableLoom", true);
        confDisableSign = plugin.getConfig().getBoolean("event.disableSign", true);
        confDisableBook = plugin.getConfig().getBoolean("event.disableBook", true);
        confDisableAnvil = plugin.getConfig().getBoolean("event.disableAnvil", true);
        confDefaultNameTag = plugin.getConfig().getString("event.defaultNameTag", "Name");
        confHideCommand = plugin.getConfig().getBoolean("event.hideCommand", true);
        confDisableCommand = plugin.getConfig().getBoolean("event.disableCommand", true);
        confDisableLeatherDye = plugin.getConfig().getBoolean("event.disableLeatherDye", true);
        confDisableBannerShield = plugin.getConfig().getBoolean("event.disableBannerShield", true);
    }

    public boolean eventOp(Player p) {
        return !(p.isOp() && confEventOpBypass);
    }

    public HashMap<UUID, Long> getDeathMap() {
        return deathMap;
    }

    public void addDeathMap(UUID uuid, Long time) {
        deathMap.put(uuid, time);
    }

    public String getDeathMessage(long time) {
        return String.format(ChatColor.translateAlternateColorCodes('&', confDeathMessage), TimeUnit.MILLISECONDS.toSeconds(Math.round((confDeathBanTime - time) / 1000.0) * 1000L)).replace("\\n", "\n");
    }

    public void saveDeathMap() {
        List<String> mapList = new ArrayList<>();
        for (Map.Entry<UUID, Long> entry : deathMap.entrySet()) {
            mapList.add(entry.getKey() + "," + entry.getValue());
        }
        deathMapConfig.set("deathMap", mapList);
        try {
            deathMapConfig.save(deathMapFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadDeathMap() {
        for (String s : deathMapConfig.getStringList("deathMap")) {
            String[] strings = s.split(",");
            if (strings.length != 2) {
                continue;
            }
            UUID uuid;
            try {
                uuid = UUID.fromString(strings[0]);
            } catch (Exception ignored) {
                continue;
            }
            long time;
            try {
                time = Long.parseLong(strings[1]);
            } catch (Exception ignored) {
                continue;
            }
            deathMap.put(uuid, time);
        }
        try {
            //noinspection ResultOfMethodCallIgnored
            deathMapFile.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HashMap<UUID, String> getNameMap() {
        return nameMap;
    }

    public void addNameMap(UUID uuid, String name) {
        nameMap.put(uuid, name);
    }

    public void changeProfile(Player player) {
        changer.changeProfile(player, confName, confTextures, confSignature, confPing);
    }

    public void saveNameMap() {
        if (Bukkit.getOnlinePlayers().size() < 1) {
            return;
        }
        List<String> mapList = new ArrayList<>();
        for (Map.Entry<UUID, String> entry : nameMap.entrySet()) {
            mapList.add(entry.getKey() + "," + entry.getValue());
        }
        nameMapConfig.set("nameMap", mapList);
        try {
            nameMapConfig.save(nameMapFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadNameMap() {
        if (Bukkit.getOnlinePlayers().size() > 0) {
            for (String s : nameMapConfig.getStringList("nameMap")) {
                String[] strings = s.split(",");
                if (strings.length != 2) {
                    continue;
                }
                UUID uuid;
                try {
                    uuid = UUID.fromString(strings[0]);
                } catch (Exception ignored) {
                    continue;
                }
                nameMap.put(uuid, strings[1]);
            }
        }
        try {
            //noinspection ResultOfMethodCallIgnored
            nameMapFile.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getName(UUID uuid) {
        String name = getNameMap().get(uuid);
        return name == null ? uuid.toString() : name;
    }

    public void logMessage(String message) {
        if (confLogConsole) {
            Bukkit.getConsoleSender().sendMessage(message);
        }
        if (confLogOp) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.isOp()) {
                    p.sendMessage(message);
                }
            }
        }
    }


}
