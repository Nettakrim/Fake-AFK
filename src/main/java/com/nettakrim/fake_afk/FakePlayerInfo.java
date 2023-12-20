package com.nettakrim.fake_afk;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

public class FakePlayerInfo {
    public FakePlayerInfo(ServerPlayerEntity player) {
        this.player = player;
        this.uuid = player.getUuid();
        this.name = loadName(player);
        this.diedAt = -1L;
        this.spawnedAt = -1L;
        this.despawnInTicks = -1;
    }

    private static final HashMap<UUID, String> playerNames = new HashMap<>();

    private static File data = null;

    public static void LoadPlayerNames() {
        ResolveDataFile();
        try {
            data.createNewFile();
            Scanner scanner = new Scanner(data);
            while (scanner.hasNextLine()) {
                String s = scanner.nextLine();
                String[] halves = s.split(" ");
                playerNames.put(UUID.fromString(halves[0]), halves[1]);
            }
            scanner.close();
        } catch (IOException e) {
            FakeAFK.info("Failed to load data");
        }
    }

    public static void SavePlayerNames() {
        ResolveDataFile();
        try {
            StringBuilder s = new StringBuilder();
            for (Map.Entry<UUID, String> entry : playerNames.entrySet()) {
                s.append(entry.getKey()).append(' ').append(entry.getValue()).append('\n');
            }
            FileWriter writer = new FileWriter(data);
            writer.write(s.toString());
            writer.close();
        } catch (IOException e) {
            FakeAFK.info("Failed to save data");
        }
    }

    private static void ResolveDataFile() {
        if (data != null) return;
        data = FabricLoader.getInstance().getConfigDir().resolve("fake_afk.txt").toFile();
    }

    private ServerPlayerEntity player;
    private final UUID uuid;
    private String name;

    private long diedAt;
    private long spawnedAt;

    private boolean ready;
    private int despawnInTicks;

    public void readyForDisconnect() {
        if (ready) {
            ready = false;
            FakeAFK.instance.say(player, "Fake-You will no longer be summoned");
        } else {
            ready = true;
            FakeAFK.instance.say(player, "Fake-You will be summoned wherever you are once you leave the server, run the command again to cancel");
        }
    }

    public void updatePlayer(ServerPlayerEntity player) {
        this.player = player;
    }

    public void realPlayerJoin() {
        long current = System.currentTimeMillis();
        if (diedAt > 0) {
            FakeAFK.instance.say(player, "Fake-You died while you were offline "+getTimeText(current-diedAt)+" ago, after "+getTimeText(diedAt-spawnedAt)+" of AFKing");
            diedAt = -1L;
        } else if (spawnedAt > 0) {
            killFakePlayer();
            FakeAFK.instance.say(player, "Fake-You was AFKing for "+getTimeText(current-spawnedAt));
        }
    }

    private void killFakePlayer() {
        resetVelocity();
        runCommand("player "+name+" kill");
    }

    public void realPlayerDisconnect() {
        if (ready) {
            despawnInTicks = -1;
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
        diedAt = -1;
    }

    public void tryLogFakeDeath(String name) {
        if (this.name.equals(name)) {
            resetVelocity();
            diedAt = System.currentTimeMillis();
        }
    }

    public void toggleSummon() {
        if (getFakePlayer() == null) {
            spawnFakePlayer();
            despawnInTicks = 6000;
            FakeAFK.instance.say(player, "Fake-You has been summoned for 5 Minutes, run the command again to dispel them earlier");
        } else {
            killFakePlayer();
            FakeAFK.instance.say(player, "Fake-You has been dispelled");
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
        playerNames.put(uuid, name);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    private String loadName(ServerPlayerEntity player) {
        String saved = playerNames.get(uuid);
        if (saved != null) {
            return saved;
        }
        return (player.getNameForScoreboard()+"-afk").toLowerCase();
    }

    public boolean uuidEquals(UUID other) {
        return uuid.equals(other);
    }

    public void tick() {
        if (despawnInTicks > 0) {
            despawnInTicks--;
            if (despawnInTicks == 0) {
                killFakePlayer();
                despawnInTicks = -1;
            }
        }
    }
}
