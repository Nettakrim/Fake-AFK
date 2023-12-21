package com.nettakrim.fake_afk;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class Connection {
    public Connection() {
        ServerPlayConnectionEvents.DISCONNECT.register(this::onDisconnect);
        ServerPlayConnectionEvents.JOIN.register(this::onConnect);
        ServerTickEvents.START_SERVER_TICK.register(this::onTick);
    }

    public int msptKickLimit = -1;
    public int msptKickType = 1; //1 kick oldest, 2 all at once
    public int maxAFKLimit = -1;
    public int maxAFKType = 1; //1 kick oldest, 2 don't allow new

    public void loadConfig(PeekableScanner scanner) {
        while (scanner.hasNextLine()) {
            String s = scanner.peek();
            if (!s.contains(": ")) {
                return;
            }
            String[] halves = s.split(": ");
            int value = FakeAFK.parseInt(halves[1], -1);
            switch (halves[0]) {
                case "mspt_kick_limit" -> msptKickLimit = value == -1 ? msptKickLimit : value;
                case "mspt_kick_type" -> msptKickType = value == -1 ? msptKickType : value;
                case "max_afk_limit" -> maxAFKLimit = value == -1 ? maxAFKLimit : value;
                case "max_afk_type" -> maxAFKType = value == -1 ? maxAFKType : value;
                default -> {
                    return;
                }
            }
            scanner.nextLine();
        }
    }

    public String saveConfig() {
        return "mspt_kick_limit: " + msptKickLimit + "\n" +
               "mspt_kick_type: " + msptKickType + "\n" +
               "max_afk_limit: " + maxAFKLimit + "\n" +
               "max_afk_type: " + maxAFKType + "\n";
    }

    public void onDisconnect(ServerPlayNetworkHandler handler, MinecraftServer server) {
        ServerPlayerEntity player = handler.getPlayer();
        FakePlayerInfo info = FakeAFK.instance.getFakePlayerInfo(player);
        if (info != null) {
            info.realPlayerDisconnect();
        }
    }

    public void onConnect(ServerPlayNetworkHandler handler, PacketSender packetSender, MinecraftServer server) {
        ServerPlayerEntity player = handler.getPlayer();
        FakePlayerInfo info = FakeAFK.instance.getFakePlayerInfo(player);

        //filter out fake players, this isnt needed on disconnect as that method doesn't trigger for fake players
        String name = player.getNameForScoreboard();
        if (name.contains("-")) return;

        if (info != null) {
            info.updatePlayer(player);
            info.realPlayerJoin();
        } else {
            info = new FakePlayerInfo(player);
            FakeAFK.instance.fakePlayers.add(info);
        }
    }

    public void logFakeDeath(ServerPlayerEntity player) {
        String name = player.getNameForScoreboard();
        if (!name.contains("-")) return;
        for (FakePlayerInfo fakePlayerInfo : FakeAFK.instance.fakePlayers) {
            fakePlayerInfo.tryLogFakeDeath(name);
        }
    }

    public void onTick(MinecraftServer server) {
        for (FakePlayerInfo info : FakeAFK.instance.fakePlayers) {
            info.tick();
        }
        if (server.getTicks()%1200 == 0) {
            msptCheck(server);
        }
    }

    private void msptCheck(MinecraftServer server) {
        if (msptKickLimit > 0 && msptKickType > 0) {
            long uspt = server.getAverageNanosPerTick()/1000L;
            if (uspt > msptKickLimit*1000L) {
                if (msptKickType == 1) {
                    FakePlayerInfo oldest = getOldest();
                    if (oldest != null) {
                        oldest.killFakePlayer();
                        FakeAFK.instance.say(server, "Server is lagging! Dispelling the longest Fake AFK player");
                    }
                }
                else if (msptKickType == 2) {
                    boolean found = false;
                    for (FakePlayerInfo fakePlayerInfo : FakeAFK.instance.fakePlayers) {
                        if (fakePlayerInfo.isAFKing()) {
                            fakePlayerInfo.killFakePlayer();
                            found = true;
                        }
                    }
                    if (found) {
                        FakeAFK.instance.say(server, "Server is lagging! Dispelling all Fake AFK players");
                    }
                }
            }
        }
    }

    private FakePlayerInfo getOldest() {
        long currentTime = System.currentTimeMillis();
        double oldestTime = 0.0;
        FakePlayerInfo oldest = null;
        for (FakePlayerInfo fakePlayerInfo : FakeAFK.instance.fakePlayers) {
            double t = fakePlayerInfo.getAFKTime(currentTime);
            if (t > oldestTime) {
                oldestTime = t;
                oldest = fakePlayerInfo;
            }
        }
        return oldest;
    }
}
