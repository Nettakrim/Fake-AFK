package com.nettakrim.fake_afk;

import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import java.util.HashMap;
import java.util.Map;
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

    public static void LoadPlayerNames(PeekableScanner scanner) {
        while (scanner.hasNextLine()) {
            String s = scanner.nextLine();
            String[] halves = s.split(" ");
            playerNames.put(UUID.fromString(halves[0]), halves[1]);
        }
    }

    public static String SavePlayerNames() {
        StringBuilder s = new StringBuilder();
        for (Map.Entry<UUID, String> entry : playerNames.entrySet()) {
            s.append(entry.getKey()).append(' ').append(entry.getValue()).append('\n');
        }
        return s.toString();
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
        ServerPlayerEntity fakePlayer = resetVelocity();
        runCommand("player "+name+" kill");
        if (fakePlayer != null) {
            ServerWorld serverWorld = (ServerWorld)fakePlayer.getWorld();
            serverWorld.spawnParticles(ParticleTypes.ENTITY_EFFECT, fakePlayer.getX(), fakePlayer.getY()+0.5, fakePlayer.getZ(), 25, 0.5f, 1f, 0.5f, 1f);
            serverWorld.playSound(null, fakePlayer.getBlockPos(), SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE.value(), SoundCategory.PLAYERS, 1, 1);
        }
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
        ServerWorld serverWorld = (ServerWorld)player.getWorld();
        serverWorld.spawnParticles(ParticleTypes.ENTITY_EFFECT, player.getX(), player.getY()+0.5, player.getZ(), 25, 0.5f, 1f, 0.5f, 1f);
        serverWorld.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_EVOKER_CAST_SPELL, SoundCategory.PLAYERS, 1, 1);
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
        ServerCommandSource source = player.getCommandSource().withLevel(5);
        MinecraftServer server = player.getServer();
        server.getCommandManager().executeWithPrefix(source, command);
    }

    private ServerPlayerEntity resetVelocity() {
        ServerPlayerEntity fakePlayer = getFakePlayer();
        if (fakePlayer == null) return null;
        fakePlayer.setVelocity(0,0,0);
        return fakePlayer;
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

    public boolean setName(String name) {
        name = name.toLowerCase();
        if (name.equalsIgnoreCase(playerNames.get(uuid))) {
            return true;
        }

        //disallow names that are already taken
        for (String s : playerNames.values()) {
            if (name.equals(s)) return false;
        }
        //disallow steve naming themselves alex-afk, since that's alex's reserved name, steve can do alex--afk, or afk-alex etc and they can also do steve-afk
        if (name.endsWith("-afk") && name.substring(name.indexOf('-')).length() <= 4 && !name.equalsIgnoreCase(player.getNameForScoreboard()+"-afk")) {
            return false;
        }
        playerNames.put(uuid, name);
        this.name = name;
        return true;
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
