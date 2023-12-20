package com.nettakrim.fake_afk;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

public class FakePlayerInfo {
    public FakePlayerInfo(ServerPlayerEntity player) {
        this.player = player;
        this.uuid = player.getUuid();
    }

    private ServerPlayerEntity player;
    public final UUID uuid;

    public void updatePlayer(ServerPlayerEntity player) {
        this.player = player;
    }

    public void killFakePlayer() {
        runCommand("player Fake"+getName()+" kill");
    }

    public void spawnFakePlayer() {
        runCommand("player Fake"+getName()+" spawn in adventure");
    }

    private void runCommand(String command) {
        ServerCommandSource source = player.getCommandSource();
        MinecraftServer server = source.getServer();
        server.getCommandManager().executeWithPrefix(source, command);
    }

    public String getName() {
        return player.getNameForScoreboard();
    }
}
