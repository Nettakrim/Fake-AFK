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
        diedAt = -1L;
    }

    private ServerPlayerEntity player;
    public final UUID uuid;
    private final String name;

    private Long diedAt;

    public void updatePlayer(ServerPlayerEntity player) {
        this.player = player;
    }

    public void realPlayerJoin() {
        if (diedAt > 0) {
            long deathSecondsAgo = (System.currentTimeMillis()-diedAt)/1000L;
            FakeAFK.LOGGER.info("died "+deathSecondsAgo+" seconds ago");
        } else {
            FakeAFK.LOGGER.info("killing "+name);
            resetVelocity();
            runCommand("player "+name+" kill");
        }
    }

    public void spawnFakePlayer() {
        runCommand("player "+name+" spawn in adventure");
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
        return ("fake_"+player.getNameForScoreboard()).toLowerCase();
    }

    private void resetVelocity() {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        ServerPlayerEntity fakePlayer = server.getPlayerManager().getPlayer(name);
        if (fakePlayer == null) return;
        fakePlayer.setVelocity(0,0,0);
    }
}
