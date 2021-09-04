package com.hanwoo.playerhider;


import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;


@SuppressWarnings("NullableProblems")
public final class PlayerHider extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {
    ProfileChanger changer = new ProfileChanger();
    HashMap<UUID, String> name = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(getCommand("kill")).setExecutor(this);
        Objects.requireNonNull(getCommand("kill")).setTabCompleter(this);
        Objects.requireNonNull(getCommand("who")).setExecutor(this);
        Objects.requireNonNull(getCommand("who")).setTabCompleter(this);

        for (World world : getServer().getWorlds()) {
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
            world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false);
            world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
        }
    }

    @Override
    public void onDisable() {

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String lable, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (command.getName().equalsIgnoreCase("kill")) {
                player.setHealth(0);
            }
            if (command.getName().equalsIgnoreCase("who")) {
                if (player.getSpectatorTarget() == null) {
                    sender.sendMessage("엔티티를 관전중이 아닙니다");
                    return true;
                }
                if (name.get(player.getSpectatorTarget().getUniqueId()) != null) {
                    sender.sendMessage("관전중인 플레이어의 이름은 " + name.get(player.getSpectatorTarget().getUniqueId()) + "입니다");
                } else {
                    sender.sendMessage("관전중인 엔티티의 UUID는 " + player.getSpectatorTarget().getUniqueId() + "입니다");
                }
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String lable, String[] args) {
        return Collections.emptyList();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.setJoinMessage(null);
        Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + e.getPlayer().getName() + " joined the game");
        name.put(e.getPlayer().getUniqueId(), e.getPlayer().getName());
        changer.changeSkin(e.getPlayer());
        changer.changeName(e.getPlayer(), "???");
        Scoreboard score = Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard();
        if (score.getTeam("player") == null) {
            Team t = score.registerNewTeam("player");
            t.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
            t.setCanSeeFriendlyInvisibles(false);
        }
        Objects.requireNonNull(score.getTeam("player")).addEntry(e.getPlayer().getName());
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> e.getPlayer().setNoDamageTicks(0));
        if (e.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!(player.getGameMode().equals(GameMode.SPECTATOR))) {
                    player.hidePlayer(this, e.getPlayer());
                }
            }
        } else {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getGameMode().equals(GameMode.SPECTATOR)) {
                    e.getPlayer().hidePlayer(this, player);
                }
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        e.setQuitMessage(null);
        if (name.get(e.getPlayer().getUniqueId()) != null) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + name.get(e.getPlayer().getUniqueId()) + " left the game");
        } else {
            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + e.getPlayer().getUniqueId().toString() + " left the game");
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getEntity().getPlayer();
        if (player == null) {
            return;
        }
        Location loc = player.getLocation();
        if (name.get(player.getUniqueId()) != null) {
            Bukkit.getConsoleSender().sendMessage(name.get(player.getUniqueId()) + " died at " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
            Bukkit.getBanList(BanList.Type.NAME).addBan(name.get(player.getUniqueId()), "사망했습니다!", new Date(System.currentTimeMillis() + 60000), null);
            Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> player.kickPlayer(ChatColor.RED + "사망했습니다!\n" + ChatColor.GRAY + "1분후에 다시 접속이 가능합니다"));
        } else {
            Bukkit.getConsoleSender().sendMessage(player.getUniqueId() + " died at " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
        }
    }

    @EventHandler
    public void onLogin(AsyncPlayerPreLoginEvent e) {
        BanEntry entry = Bukkit.getBanList(BanList.Type.NAME).getBanEntry(e.getName());
        if (entry == null) {
            return;
        }
        if (entry.getExpiration() == null) {
            return;
        }
        if (entry.getExpiration().getTime() > System.currentTimeMillis()) {
            e.setKickMessage(ChatColor.RED + "사망했습니다!\n" + ChatColor.GRAY + "1분후에 다시 접속이 가능합니다");
            e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_BANNED);
        }
    }

    @EventHandler
    public void onGamemode(PlayerGameModeChangeEvent e) {
        if (e.getNewGameMode().equals(GameMode.SPECTATOR)) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!(player.getGameMode().equals(GameMode.SPECTATOR))) {
                    player.hidePlayer(this, e.getPlayer());
                }
            }
        } else {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.showPlayer(this, e.getPlayer());
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if (!(e.getMessage().equalsIgnoreCase("/kill")) && !(e.getPlayer().isOp())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onTab(PlayerCommandSendEvent e) {
        if (!(e.getPlayer().isOp())) {
            e.getCommands().clear();
            e.getCommands().add("kill");
        }
    }

    @EventHandler
    public void onSign(SignChangeEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onBook(PlayerEditBookEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onAnvil(PrepareAnvilEvent e) {
        if (e.getResult() != null) {
            ItemStack it = e.getResult();
            ItemMeta meta = it.getItemMeta();
            if (meta == null) {
                return;
            }
            if(it.getType().equals(Material.NAME_TAG)){
                meta.setDisplayName("name");
            }else{
                meta.setDisplayName(null);
            }
            it.setItemMeta(meta);
            e.setResult(it);
            if(e.getInventory().getItem(0) != null){
                if(it.equals(e.getInventory().getItem(0))){
                    e.setResult(null);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        if (e.getInventory().getType().equals(InventoryType.LOOM)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onCraft(PrepareItemCraftEvent e) {
        ItemStack it = e.getInventory().getResult();
        if(it == null){
            return;
        }
        ItemMeta itMeta = it.getItemMeta();
        if(itMeta == null){
            return;
        }
        if(it.getType().equals(Material.SHIELD)){
            if(Arrays.stream(e.getInventory().getContents()).anyMatch(i -> Tag.BANNERS.isTagged(i.getType()))){
                e.getInventory().setResult(null);
            }
        }
        if(itMeta instanceof LeatherArmorMeta){
            LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) itMeta;
            if(!(leatherArmorMeta.getColor().equals(Color.fromRGB(160,101,64)))) {
                e.getInventory().setResult(null);
            }
        }
    }
}
