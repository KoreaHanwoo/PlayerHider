package com.hanwoo.playerhider;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;

public class ProfileChanger {

    public void changeProfile(Player player, String name, String textures, String signature, int ping) {
        GameProfile profile = ((CraftPlayer) player).getHandle().getProfile();
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        try {
            Field ff = profile.getClass().getDeclaredField("name");
            ff.setAccessible(true);
            ff.set(profile, name);
        } catch (Exception e) {
            e.printStackTrace();
        }

        connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, ((CraftPlayer) player).getHandle()));
        profile.getProperties().removeAll("textures");
        profile.getProperties().put("textures", new Property("textures", textures, signature));
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        entityPlayer.ping = ping;
        connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityPlayer));
    }
}