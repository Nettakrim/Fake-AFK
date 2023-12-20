package com.nettakrim.fake_afk;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

public class FakePlayerInfo {
    public FakePlayerInfo(ServerPlayerEntity player) {
        this.player = player;
        this.uuid = player.getUuid();
        this.name = loadName(player);
        this.diedAt = -1L;
        this.spawnedAt = -1L;
        this.despawnInTicks = -1L;
    }

    private ServerPlayerEntity player;
    private final UUID uuid;
    private String name;

    private long diedAt;
    private long spawnedAt;

    private boolean ready;
    private long despawnInTicks;

    public void readyForDisconnect() {
        this.ready = true;
        FakeAFK.instance.say(player, "Fake You will appear when and where you leave");
    }

    public void updatePlayer(ServerPlayerEntity player) {
        this.player = player;
    }

    public void realPlayerJoin() {
        long current = System.currentTimeMillis();
        if (diedAt > 0) {
            FakeAFK.instance.say(player, "Fake You died while you were AFK "+getTimeText(current-diedAt)+" ago, after "+getTimeText(diedAt-spawnedAt)+" of AFKing");
            diedAt = -1L;
        } else {
            killFakePlayer();
            FakeAFK.instance.say(player, "Fake You was AFKing for "+getTimeText(current-spawnedAt));
        }
    }

    private void killFakePlayer() {
        resetVelocity();
        runCommand("player "+name+" kill");
    }

    public void realPlayerDisconnect() {
        if (ready) {
            despawnInTicks = -1L;
            ServerPlayerEntity fakePlayer = getFakePlayer();
            if (fakePlayer != null) {
                fakePlayer.teleport(player.getServerWorld(), player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
            } else {
                spawnFakePlayer();
            }
            ready = false;
        }
    }

    public void spawnFakePlayer() {
        runCommand("player "+name+" spawn in adventure");
        spawnedAt = System.currentTimeMillis();
    }

    public void deathTest(String name) {
        if (this.name.equals(name)) {
            resetVelocity();
            diedAt = System.currentTimeMillis();
        }
    }

    public void toggleSummon() {
        if (getFakePlayer() == null) {
            spawnFakePlayer();
            despawnInTicks = 6000;
            FakeAFK.instance.say(player, "Fake You has been summoned for 5 Minutes, run the command again to dispel them earlier");
        } else {
            killFakePlayer();
            FakeAFK.instance.say(player, "Fake You has been dispelled");
        }
    }

    private void runCommand(String command) {
        ServerCommandSource source = player.getCommandSource();
        MinecraftServer server = source.getServer();
        server.getCommandManager().executeWithPrefix(source, command);
    }

    private void resetVelocity() {
        ServerPlayerEntity fakePlayer = getFakePlayer();
        if (fakePlayer == null) return;
        fakePlayer.setVelocity(0,0,0);
    }

    private ServerPlayerEntity getFakePlayer() {
        MinecraftServer server = player.getServer();
        if (server == null) return null;
        return server.getPlayerManager().getPlayer(name);
    }

    private String getTimeText(Long timeMillis) {
        StringBuilder s = new StringBuilder();
        long seconds = timeMillis/1000L;
        long minutes = seconds/60L;
        long hours = minutes/60L;
        if (hours > 0) s.append(hours).append(hours == 1 ? " hour " : " hours ");
        if (minutes > 0) s.append(minutes%60L).append(minutes == 1 ? " minute " : " minutes ");
        if (minutes < 15) s.append(seconds%60L).append(seconds == 1 ? " second " : " seconds ");
        return s.substring(0,s.length()-1);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String loadName(ServerPlayerEntity player) {
        return (player.getNameForScoreboard()+"-afk").toLowerCase();
    }

    public boolean uuidEquals(UUID other) {
        return uuid.equals(other);
    }

    public void tick() {
        if (despawnInTicks > 0L) {
            despawnInTicks--;
            if (despawnInTicks == 0L) {
                killFakePlayer();
                despawnInTicks = -1L;
            }
        }
    }
}
