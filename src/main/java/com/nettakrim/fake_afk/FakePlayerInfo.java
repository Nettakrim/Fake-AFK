package com.nettakrim.fake_afk;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.particle.EntityEffectParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.UserCache;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;

import java.util.*;

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

    private static int maxAFKTicks = -1;
    private static int maxSummonTicks = 6000;
    private static final int maxNameLength = 16;

    public static void LoadPlayerNames(PeekableScanner scanner) {
        boolean ready = false;
        while (scanner.hasNextLine()) {
            String s = scanner.nextLine();
            if (ready) {
                String[] halves = s.split(" ");
                playerNames.put(UUID.fromString(halves[0]), halves[1]);
            } else {
                if (s.equals("names:")) ready = true;
                else if (s.contains(": ")) {
                    String[] halves = s.split(": ");
                    int value = FakeAFK.parseInt(halves[1], -2);
                    switch (halves[0]) {
                        case "max_afk_ticks" -> maxAFKTicks = value == -2 ? maxAFKTicks : value;
                        case "max_summon_ticks" -> maxSummonTicks = value == -2 ? maxSummonTicks : value;
                    }
                }
            }
        }
    }

    public static String SavePlayerNames() {
        StringBuilder s = new StringBuilder();
        s.append("max_afk_ticks: ").append(maxAFKTicks).append("\n");
        s.append("max_summon_ticks: ").append(maxSummonTicks).append("\n");
        s.append("names:\n");
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

    private boolean afking;

    public void readyForDisconnect() {
        if (ready) {
            ready = false;
            FakeAFK.instance.say(player, "Fake-You will no longer be summoned");
        } else {
            ready = true;
            String s = "Fake-You will be summoned wherever you are once you leave the server";
            if (maxAFKTicks > 0) {
                s+=", dispelling automatically after "+getTimeText(50L*maxAFKTicks);
            }
            s+=", run the command again to cancel";
            FakeAFK.instance.say(player, s);
            if (FakeAFK.instance.connection.afkWontSpawnCheck()) {
                FakeAFK.instance.say(player, "Watch out! The maximum amount of fake players are currently AFKing, so Fake-You might not spawn");
            }
        }
    }

    public void cancelReady() {
        ready = false;
    }

    public void updatePlayer(ServerPlayerEntity player) {
        this.player = player;
    }

    public void realPlayerJoin() {
        if (afking) {
            long current = System.currentTimeMillis();
            if (diedAt > 0) {
                FakeAFK.instance.say(player, "Fake-You died while you were offline " + getTimeText(current - diedAt) + " ago, after " + getTimeText(diedAt - spawnedAt) + " of AFKing");
                diedAt = -1L;
            } else if (spawnedAt > 0) {
                killFakePlayer();
                FakeAFK.instance.say(player, "Fake-You was AFKing for " + getTimeText(current - spawnedAt));
            }
            afking = false;
        }
    }

    public void killFakePlayer() {
        ServerPlayerEntity fakePlayer = resetVelocity();
        if (fakePlayer != null) {
            runCommand("player "+name+" kill");
            ServerWorld serverWorld = (ServerWorld)fakePlayer.getWorld();
            serverWorld.spawnParticles(EntityEffectParticleEffect.create(ParticleTypes.ENTITY_EFFECT, ColorHelper.getArgb(255, 255, 255, 255)), fakePlayer.getX(), fakePlayer.getY()+0.5, fakePlayer.getZ(), 25, 0.5f, 1f, 0.5f, 1f);
            serverWorld.playSound(null, fakePlayer.getBlockPos(), SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE.value(), SoundCategory.PLAYERS, 1, 1);
        }
    }

    public boolean realPlayerDisconnect() {
        if (ready) {
            despawnInTicks = maxAFKTicks;
            ServerPlayerEntity fakePlayer = getFakePlayer();
            if (fakePlayer != null) {
                fakePlayer.teleportTo(new TeleportTarget(player.getServerWorld(), player.getPos(), Vec3d.ZERO, player.getYaw(), player.getPitch(), TeleportTarget.NO_OP));
            } else {
                spawnFakePlayer();
            }
            afking = true;
            ready = false;
            return true;
        }
        return false;
    }

    public void spawnFakePlayer() {
        runCommand("player "+name+" spawn in adventure");
        spawnedAt = System.currentTimeMillis();
        diedAt = -1;
        ServerWorld serverWorld = (ServerWorld)player.getWorld();
        serverWorld.spawnParticles(EntityEffectParticleEffect.create(ParticleTypes.ENTITY_EFFECT, ColorHelper.getArgb(255, 255, 255, 255)), player.getX(), player.getY()+0.5, player.getZ(), 25, 0.5f, 1f, 0.5f, 1f);
        serverWorld.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_EVOKER_CAST_SPELL, SoundCategory.PLAYERS, 1, 1);
    }

    public void tryLogFakeDeath(String name) {
        if (this.name.equals(name)) {
            resetVelocity();
            diedAt = System.currentTimeMillis();
            afking = false;
        }
    }

    public void toggleSummon() {
        if (getFakePlayer() == null) {
            spawnFakePlayer();
            despawnInTicks = maxSummonTicks;
            FakeAFK.instance.say(player, "Fake-You has been summoned for "+getTimeText(50L*maxSummonTicks)+", run the command again to dispel them"+(maxSummonTicks == -1 ? "":" earlier"));
        } else {
            killFakePlayer();
            FakeAFK.instance.say(player, "Fake-You has been dispelled");
        }
    }

    private void runCommand(String command) {
        //PlayerEntity playerEntity = player == null ? getFakePlayer() : player;
        ServerCommandSource source = player.getCommandSource().withLevel(4);
        MinecraftServer server = player.getServer();
        if (server == null) return;
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
        if (timeMillis < 0) return "unlimited time";
        StringBuilder s = new StringBuilder();
        long seconds = timeMillis/1000L;
        long minutes = seconds/60L;
        long hours = minutes/60L;
        if (hours > 0) s.append(hours).append(hours == 1 ? " hour " : " hours ");
        if (minutes%60L > 0) s.append(minutes%60L).append(minutes == 1 ? " minute " : " minutes ");
        if (minutes < 15 && seconds%60L > 0) s.append(seconds%60L).append(seconds == 1 ? " second " : " seconds ");
        return s.substring(0,s.length()-1);
    }

    public boolean setName(String name) {
        name = name.toLowerCase();
        if (name.equalsIgnoreCase(playerNames.get(uuid))) {
            FakeAFK.instance.say(player, "Fake-You has is already named "+name.toLowerCase());
            return true;
        }
        //disallow names that are too long for carpet
        if (name.length() > maxNameLength) {
            FakeAFK.instance.say(player, name+" is too long (max "+maxNameLength+" characters)");
            return false;
        }
        //disallow names that are already taken
        for (String s : playerNames.values()) {
            if (name.equalsIgnoreCase(s)) {
                FakeAFK.instance.say(player, name+" is already taken");
                return false;
            }
        }
        //disallow steve naming themselves alex-afk, since that's alex's reserved name, steve can do alex--afk, or afk-alex etc and they can also do steve-afk
        if (isReservedName(player.getNameForScoreboard(), name)) {
            FakeAFK.instance.say(player, name+" is reserved, try a slight variation that doesn't have the format name-afk (eg name--afk, afk-name, name-bot, name-2)");
            return false;
        }
        ServerPlayerEntity oldPlayer = getFakePlayer();
        if (oldPlayer != null) {
            recoverInventory(oldPlayer);
            killFakePlayer();
        } else {
            try {
                fakeRecovery(player.server);
            } catch (Exception e) {
                FakeAFK.info("Error transferring inventory:\n" + e);
            }
        }
        playerNames.put(uuid, name);
        this.name = name;
        FakeAFK.instance.say(player, "Fake-You has been renamed to "+name.toLowerCase());
        return true;
    }

    private void fakeRecovery(MinecraftServer server) {
        //very error-prone code since it does not fully load the new player
        UserCache userCache = server.getUserCache();
        if (userCache == null) return;
        GameProfile profile = server.getUserCache().findByName(this.name).orElse(null);
        if (profile == null) return;

        PlayerManager playerManager = server.getPlayerManager();
        SyncedClientOptions options = SyncedClientOptions.createDefault();

        ServerPlayerEntity oldPlayer = playerManager.createPlayer(profile, options);
        oldPlayer.networkHandler = new FakeNetworkHandler(server, oldPlayer);

        playerManager.loadPlayerData(oldPlayer);
        recoverInventory(oldPlayer);
        playerManager.remove(oldPlayer);
    }

    private void recoverInventory(ServerPlayerEntity oldPlayer) {
        PlayerInventory inventory = oldPlayer.getInventory();
        if (inventory.isEmpty()) return;

        oldPlayer.setPos(player.getX(), player.getY(), player.getZ());
        oldPlayer.setPitch(player.getPitch());
        oldPlayer.setYaw(player.getYaw());

        //try to merge inventory with the player, throwing any spare items
        for (ItemStack itemStack : inventory) {
            if (!itemStack.isEmpty()) {
                player.getInventory().insertStack(itemStack);
                if (!itemStack.isEmpty()) {
                    oldPlayer.dropItem(itemStack, false, false);
                }
            }
        }

        inventory.clear();
    }

    public String getName() {
        return name;
    }

    private String loadName(ServerPlayerEntity player) {
        String saved = playerNames.get(uuid);
        if (saved != null) {
            return saved;
        }
        return getDefaultName(player.getNameForScoreboard());
    }

    private static String getDefaultName(String name) {
        name = name.toLowerCase();
        if (name.length() < maxNameLength) {
            name += "-afk";
            return name.length() > maxNameLength ? name.substring(0, maxNameLength) : name;
        } else {
            return name.substring(0, name.length()-1)+"-";
        }
    }

    private static boolean isReservedName(String player, String name) {
        if (name.equals(getDefaultName(player))) {
            return false;
        }
        String afterFirst = name.substring(name.indexOf('-')+1);
        if (afterFirst.contains("-")) return false;
        if (afterFirst.equals("afk")) return true;
        return name.length() == maxNameLength && (afterFirst.equals("af") || afterFirst.equals("a"));
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

    public double getAFKTime(double currentTime) {
        return afking ? currentTime-spawnedAt : -1.0;
    }

    public boolean isAFKing() {
        return afking;
    }

    private static class FakeNetworkHandler extends ServerPlayNetworkHandler {
        //short-lived object that just needs to last long enough to stop errors from happening when it gets sent wildly into minecraft code
        public FakeNetworkHandler(MinecraftServer server, ServerPlayerEntity player) {
            super(server, new FakeConnection(), player, new ConnectedClientData(player.getGameProfile(), 0, player.getClientOptions(), false));
        }

        @Override
        public void requestTeleport(double x, double y, double z, float yaw, float pitch) {}

        @Override
        public void sendPacket(Packet<?> packet) {}

        private static class FakeConnection extends ClientConnection {
            public FakeConnection() {
                super(null);
            }

            @Override
            public boolean isLocal() {
                return false;
            }
        }
    }
}
