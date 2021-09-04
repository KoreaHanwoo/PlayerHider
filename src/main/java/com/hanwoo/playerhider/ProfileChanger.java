package com.hanwoo.playerhider;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;

public class ProfileChanger {

    public void changeSkin(Player player){
        GameProfile profile = ((CraftPlayer)player).getHandle().getProfile();
        PlayerConnection connection = ((CraftPlayer)player).getHandle().playerConnection;

        connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, ((CraftPlayer)player).getHandle()));
        profile.getProperties().removeAll("textures");
        profile.getProperties().put("textures", new Property("textures", "ewogICJ0aW1lc3RhbXAiIDogMTYyOTY4Mjg4ODM1MywKICAicHJvZmlsZUlkIiA6ICI3ZmIyOGQ1N2FhZmQ0MmQ1YTcwNWNlZjE4YWI1MzEzZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJjaXJjdWl0MTAiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWMyYWQyYzM4YTBiZTdhZDI0MWM4ODgwOTZkODM2NmRiNjU2ZDFkMmIwN2Y0YzM3NWE2MzVkMWFhMTRjMWQ1ZCIKICAgIH0KICB9Cn0=", "hJrD2oQ44oK0Gqw1J9ciFPUNXXKDqF6qcqkK6g869jE/Zt5j7zhaeO6JNPDonVI6ZFeZwGiecDTMHzRxgaC79YdLBD2iHbcBy34eFNHAmvTWtxiqandeLGxAXXIrw8FPoZvOcFXUEPUdlQs5D5Eh7ocxoXAKLcQQRuCDUtFXQ1k8BJiU9YNMnIXt7g3BXDu1am09a7PBhqfO8M1p3RhYWAAH+DntL7gmvl0Q4YWREvHeekXEGlQBjGqVycGg5MXHlcDW5+3Nia5lGLqw5KNXVoPGJYeekgwRjBDYl5Mj/z/PAZufldHq66XcfktmKooyKTqiBD2GPtniWwGCPoZ0JHLN8OEqYGGnr2x1Cjs7TJyZMvwja7Tk8sZV5T08CdzI8H3lEJ1aVqkkiZZS9ODg/m3LKu7h1vu0KMFKzLbFmVGBbBYGdrPXOtEpjUOCBIX5caeZyDIFPhIVO3l2aXA5p/CUlkuTBJYwbphsD9MfufuxpZsxdAboZPNb3wbL8i/hEOxCMcKKUofZ9qC2FkpjoOYhhvdZyWcd3tNlQyPg1EguN/bFCwlg/BaQ826svaQUquXnp3rhHmSbRwv++3Sjs2LX7iligArsNrFpWvbAgHmTqOdVVLgIOyeLWhkMtKqcNa+taddwL06bq2aL+ifp0bUBv4vIrwuU1byeX5IpoTM="));

        connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, ((CraftPlayer)player).getHandle()));
    }

    public void changeName(Player player, String name){
        try {
            GameProfile playerProfile = ((CraftPlayer) player).getHandle().getProfile();
            Field ff = playerProfile.getClass().getDeclaredField("name");
            ff.setAccessible(true);
            ff.set(playerProfile, name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}