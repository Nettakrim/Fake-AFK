package com.nettakrim.fake_afk;

import com.nettakrim.fake_afk.commands.FakeAFKCommands;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.UUID;

public class FakeAFK implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("fake-afk");
	public static FakeAFK instance;

	public FakeAFKCommands commands;

	public ArrayList<FakePlayerInfo> fakePlayers;

	private final TextColor textColor = TextColor.fromRgb(0xAAAAAA);
	private final TextColor nameTextColor = TextColor.fromRgb(0xF07F1D);

	@Override
	public void onInitialize() {
		instance = this;
		fakePlayers = new ArrayList<>();

		commands = new FakeAFKCommands();

		ServerPlayConnectionEvents.DISCONNECT.register(this::onDisconnect);
		ServerPlayConnectionEvents.JOIN.register(this::onConnect);
	}

	public boolean readyPlayer(ServerPlayerEntity player) {
		if (player == null || getFakePlayerInfo(player) != null) return false;
		FakePlayerInfo fakePlayerInfo = new FakePlayerInfo(player);
		fakePlayerInfo.readyForDisconnect();
		fakePlayers.add(fakePlayerInfo);
		return true;
	}

	private void onDisconnect(ServerPlayNetworkHandler handler, MinecraftServer server) {
		ServerPlayerEntity player = handler.getPlayer();
		FakePlayerInfo info = getFakePlayerInfo(player);
		if (info != null) {
			info.realPlayerDisconnect();
		}
	}

	public void deathTest(ServerPlayerEntity player) {
		String name = player.getNameForScoreboard();
		for (FakePlayerInfo fakePlayerInfo : fakePlayers) {
			fakePlayerInfo.deathTest(name);
		}
	}

	private void onConnect(ServerPlayNetworkHandler handler, PacketSender packetSender, MinecraftServer server) {
		ServerPlayerEntity player = handler.getPlayer();
		FakePlayerInfo info = getFakePlayerInfo(player);
		if (info != null) {
			fakePlayers.remove(info);
			info.updatePlayer(player);
			info.realPlayerJoin();
		}
	}

	public FakePlayerInfo getFakePlayerInfo(ServerPlayerEntity player) {
		UUID uuid = player.getUuid();
		for (FakePlayerInfo info : fakePlayers) {
			if (info.uuid.equals(uuid)) {
				return info;
			}
		}
		return null;
	}

	public void say(ServerPlayerEntity player, String message, Object... args) {
		if (player == null) return;
		Text text = Text.literal("[Fake AFK] ").setStyle(Style.EMPTY.withColor(nameTextColor)).append(Text.literal(message.formatted(args)).setStyle(Style.EMPTY.withColor(textColor)));
		player.sendMessage(text);
	}
}