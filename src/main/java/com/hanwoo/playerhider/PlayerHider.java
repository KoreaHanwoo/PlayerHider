package com.hanwoo.playerhider;


import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;


@SuppressWarnings("NullableProblems")
public final class PlayerHider extends JavaPlugin {
    Manager manager = new Manager(this);

    @Override
    public void onEnable() {
        enableLoad();

        getServer().getPluginManager().registerEvents(new Events(this), this);

        if (manager.confForceGameRule) {
            for (World world : getServer().getWorlds()) {
                world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
                world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false);
                world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
            }
        }
    }

    @Override
    public void onDisable() {
        disableLoad();
    }

    private void enableLoad() {
        saveDefaultConfig();
        manager.loadConfig();
        manager.loadNameMap();
        manager.loadDeathMap();
    }

    private void disableLoad() {
        manager.saveNameMap();
        manager.saveDeathMap();
    }

    public boolean onCommand(CommandSender sender, Command command, String lable, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (command.getName().equalsIgnoreCase("kill") && manager.confKillCommand) {
                player.setHealth(0);
            }
            if (command.getName().equalsIgnoreCase("who")) {
                if (player.getSpectatorTarget() == null) {
                    sender.sendMessage("엔티티를 관전중이 아닙니다");
                    return true;
                }
                if (manager.getNameMap().get(player.getSpectatorTarget().getUniqueId()) != null) {
                    sender.sendMessage("관전중인 플레이어의 이름은 " + manager.getNameMap().get(player.getSpectatorTarget().getUniqueId()) + "입니다");
                } else {
                    sender.sendMessage("관전중인 엔티티의 UUID는 " + player.getSpectatorTarget().getUniqueId() + "입니다");
                }
            }
        }
        if (command.getName().equalsIgnoreCase("list")) {
            List<String> names = new ArrayList<>();
            Bukkit.getOnlinePlayers().forEach(p -> names.add(ChatColor.WHITE + manager.getName(p.getUniqueId())));
            sender.sendMessage(String.format("최대 %d명 중 %d명이 온라인입니다: %s", Bukkit.getMaxPlayers(), Bukkit.getOnlinePlayers().size(), String.join(ChatColor.GRAY + ", ", names)));
        }
        if (command.getName().equalsIgnoreCase("playerhider")) {
            disableLoad();
            enableLoad();
            sender.sendMessage("설정을 리로드 하였습니다");
        }
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String lable, String[] args) {
        return Collections.emptyList();
    }
}
