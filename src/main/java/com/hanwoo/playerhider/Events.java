package com.hanwoo.playerhider;

import org.bukkit.*;
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
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Arrays;
import java.util.Objects;

public class Events implements Listener {
    PlayerHider plugin;

    public Events(PlayerHider plugin) {
        this.plugin = plugin;
    }


    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.setJoinMessage(null);
        plugin.manager.logMessage(ChatColor.YELLOW + e.getPlayer().getName() + " joined the game");
        plugin.manager.addNameMap(e.getPlayer().getUniqueId(), e.getPlayer().getName());
        plugin.manager.changeProfile(e.getPlayer());
        if (plugin.manager.confHideNameTag) {
            hideNameTag(e.getPlayer().getName());
        } else {
            Team team = e.getPlayer().getScoreboard().getTeam("player");
            if (team != null) {
                team.removeEntry(e.getPlayer().getName());
            }
        }
        if (plugin.manager.confNoJoinInvincibility) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> e.getPlayer().setNoDamageTicks(0));
        }
        if (plugin.manager.confHideSpectators) {
            checkSpectator(e.getPlayer());
        }
    }

    private void hideNameTag(String name) {
        Scoreboard score = Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard();
        if (score.getTeam("player") == null) {
            Team t = score.registerNewTeam("player");
            t.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
            t.setCanSeeFriendlyInvisibles(false);
        }
        Objects.requireNonNull(score.getTeam("player")).addEntry(name);
    }

    private void checkSpectator(Player player) {
        if (player.getGameMode().equals(GameMode.SPECTATOR)) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!(p.getGameMode().equals(GameMode.SPECTATOR))) {
                    p.hidePlayer(plugin, player);
                }
            }
        } else {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getGameMode().equals(GameMode.SPECTATOR)) {
                    player.hidePlayer(plugin, p);
                }
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        e.setQuitMessage(null);
        plugin.manager.logMessage(ChatColor.YELLOW + plugin.manager.getName(e.getPlayer().getUniqueId()) + " left the game");
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        if(!plugin.manager.confDeathBan){
            return;
        }
        if (e.getEntity().isOp() && plugin.manager.confDeathOpBypass) {
            return;
        }
        plugin.manager.addDeathMap(e.getEntity().getUniqueId(), System.currentTimeMillis());
        Location loc = e.getEntity().getLocation();
        plugin.manager.logMessage(plugin.manager.getName(e.getEntity().getUniqueId()) + " died at " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        if(!plugin.manager.confDeathBan){
            return;
        }
        if (e.getPlayer().isOp() && plugin.manager.confDeathOpBypass) {
            return;
        }
        if (plugin.manager.getDeathMap().containsKey(e.getPlayer().getUniqueId())) {
            long time = System.currentTimeMillis() - plugin.manager.getDeathMap().get(e.getPlayer().getUniqueId());
            if (time <= plugin.manager.confDeathBanTime) {
                e.getPlayer().kickPlayer(plugin.manager.getDeathMessage(time));
            }
        }
    }

    @EventHandler
    public void onLogin(AsyncPlayerPreLoginEvent e) {
        if(!plugin.manager.confDeathBan){
            return;
        }
        if (Bukkit.getOperators().stream().anyMatch(o -> o.getUniqueId().equals(e.getUniqueId())) && plugin.manager.confDeathOpBypass) {
            return;
        }
        if (plugin.manager.getDeathMap().containsKey(e.getUniqueId())) {
            long time = System.currentTimeMillis() - plugin.manager.getDeathMap().get(e.getUniqueId());
            if (time <= plugin.manager.confDeathBanTime) {
                e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
                e.setKickMessage(plugin.manager.getDeathMessage(time));
            }
        }
    }

    @EventHandler
    public void onGamemode(PlayerGameModeChangeEvent e) {
        if (plugin.manager.confHideSpectators) {
            if (e.getNewGameMode().equals(GameMode.SPECTATOR)) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getGameMode().equals(GameMode.SPECTATOR)) {
                        e.getPlayer().showPlayer(plugin, player);
                    } else {
                        player.hidePlayer(plugin, e.getPlayer());
                    }
                }
            } else {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.showPlayer(plugin, e.getPlayer());
                    if (player.getGameMode().equals(GameMode.SPECTATOR)) {
                        e.getPlayer().hidePlayer(plugin, player);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (plugin.manager.eventOp(e.getPlayer())) {
            e.setCancelled(true);
            plugin.manager.logMessage(ChatColor.RED + "<" + plugin.manager.getName(e.getPlayer().getUniqueId()) + "> " + ChatColor.WHITE + e.getMessage());
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if (plugin.manager.eventOp(e.getPlayer())) {
            if (!e.getMessage().equalsIgnoreCase("/kill")) {
                e.setCancelled(true);
            } else {
                if (!plugin.manager.confKillCommand) {
                    e.setCancelled(true);
                }
            }
        }
        plugin.manager.logMessage(plugin.manager.getName(e.getPlayer().getUniqueId()) + ": " + e.getMessage());
    }

    @EventHandler
    public void onTab(PlayerCommandSendEvent e) {
        if (plugin.manager.eventOp(e.getPlayer()) && plugin.manager.confHideCommand) {
            e.getCommands().clear();
            if (plugin.manager.confKillCommand) {
                e.getCommands().add("kill");
            }
        } else {
            if (!plugin.manager.confKillCommand) {
                e.getCommands().remove("kill");
                e.getCommands().remove("playerhider:kill");
            }
        }
    }

    @EventHandler
    public void onSign(SignChangeEvent e) {
        if (plugin.manager.eventOp(e.getPlayer()) && plugin.manager.confDisableSign) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBook(PlayerEditBookEvent e) {
        if (plugin.manager.eventOp(e.getPlayer()) && plugin.manager.confDisableBook) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onAnvil(PrepareAnvilEvent e) {
        if (plugin.manager.eventOp((Player) e.getView().getPlayer()) && plugin.manager.confDisableAnvil) {
            if (e.getResult() == null) {
                return;
            }
            ItemStack it = e.getResult();
            ItemMeta meta = it.getItemMeta();
            if (meta == null) {
                return;
            }
            if (it.getType().equals(Material.NAME_TAG)) {
                meta.setDisplayName(plugin.manager.confDefaultNameTag);
            } else {
                meta.setDisplayName(null);
            }
            it.setItemMeta(meta);
            e.setResult(it);
            if (e.getInventory().getItem(0) != null) {
                if (it.equals(e.getInventory().getItem(0))) {
                    e.setResult(null);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        if (e.getInventory().getType().equals(InventoryType.LOOM) && plugin.manager.confDisableLoom && plugin.manager.eventOp((Player) e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onCraft(PrepareItemCraftEvent e) {
        ItemStack it = e.getInventory().getResult();
        if (it == null) {
            return;
        }
        ItemMeta itMeta = it.getItemMeta();
        if (itMeta == null) {
            return;
        }
        if (it.getType().equals(Material.SHIELD) && plugin.manager.eventOp((Player) e.getView().getPlayer()) && plugin.manager.confDisableBannerShield) {
            if (Arrays.stream(e.getInventory().getContents()).anyMatch(i -> Tag.BANNERS.isTagged(i.getType()))) {
                e.getInventory().setResult(null);
            }
        }
        if (itMeta instanceof LeatherArmorMeta) {
            LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) itMeta;
            if ((!(leatherArmorMeta.getColor().equals(Color.fromRGB(160, 101, 64)))) && plugin.manager.eventOp((Player) e.getView().getPlayer()) && plugin.manager.confDisableLeatherDye) {
                e.getInventory().setResult(null);
            }
        }
    }

}
