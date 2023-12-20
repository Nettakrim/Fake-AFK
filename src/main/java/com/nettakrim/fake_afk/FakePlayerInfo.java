package com.nettakrim.fake_afk;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

public class FakePlayerInfo {
    public FakePlayerInfo(ServerPlayerEntity player) {
        this.player = player;
        this.uuid = player.getUuid();
        this.name = getName(player);
        this.diedAt = -1L;
    }

    private ServerPlayerEntity player;
    public final UUID uuid;
    private final String name;

    private Long diedAt;
    private Long spawnedAt;

    private boolean ready;

    public void readyForDisconnect() {
        this.ready = true;
        FakeAFK.instance.say(player, "Fake You will appear when and where you leave");
    }

    public void updatePlayer(ServerPlayerEntity player) {
        this.player = player;
    }

    public void realPlayerJoin() {
        Long current = System.currentTimeMillis();
        if (diedAt > 0) {
            FakeAFK.instance.say(player, "Fake You died while you were AFK "+getTimeText(current-diedAt)+" ago, after "+getTimeText(diedAt-spawnedAt)+" of AFKing");
            diedAt = -1L;
        } else {
            resetVelocity();
            runCommand("player "+name+" kill");
            FakeAFK.instance.say(player, "Fake You was AFKing for "+getTimeText(current-spawnedAt));
        }
    }

    public void realPlayerDisconnect() {
        if (ready) {
            spawnFakePlayer();
        }
    }

    public void spawnFakePlayer() {
        runCommand("player "+name+" spawn in adventure");
        spawnedAt = System.currentTimeMillis();
    }

    public void deathTest(String name) {
        FakeAFK.LOGGER.info("death test "+name);
        if (this.name.equals(name)) {
            resetVelocity();
            diedAt = System.currentTimeMillis();
        }
    }

    private void runCommand(String command) {
        ServerCommandSource source = player.getCommandSource();
        MinecraftServer server = source.getServer();
        server.getCommandManager().executeWithPrefix(source, command);
    }

    public static String getName(ServerPlayerEntity player) {
        return (player.getNameForScoreboard()+"_afk").toLowerCase();
    }

    private void resetVelocity() {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        ServerPlayerEntity fakePlayer = server.getPlayerManager().getPlayer(name);
        if (fakePlayer == null) return;
        fakePlayer.setVelocity(0,0,0);
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
}
