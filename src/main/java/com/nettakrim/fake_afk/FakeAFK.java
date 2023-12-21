package com.nettakrim.fake_afk;

import com.nettakrim.fake_afk.commands.FakeAFKCommands;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.UUID;

public class FakeAFK implements ModInitializer {
	public static FakeAFK instance;
    private static final Logger LOGGER = LoggerFactory.getLogger("fake-afk");

	private ArrayList<FakePlayerInfo> fakePlayers;

	private final TextColor textColor = TextColor.fromRgb(0xAAAAAA);
	private final TextColor nameTextColor = TextColor.fromRgb(0xF07F1D);

	private static File data = null;

	private FakeAFKCommands commands;

	@Override
	public void onInitialize() {
		instance = this;
		fakePlayers = new ArrayList<>();

		commands = new FakeAFKCommands();

		ServerPlayConnectionEvents.DISCONNECT.register(this::onDisconnect);
		ServerPlayConnectionEvents.JOIN.register(this::onConnect);
		ServerLifecycleEvents.SERVER_STOPPED.register(this::onServerClose);
		ServerTickEvents.START_SERVER_TICK.register(this::onTick);

		ResolveDataFile();
		try {
			data.createNewFile();
			PeekableScanner scanner = new PeekableScanner(new Scanner(data));
			commands.loadPermissions(scanner);
			FakePlayerInfo.LoadPlayerNames(scanner);
			scanner.close();
		} catch (IOException e) {
			FakeAFK.info("Failed to load data");
		}
	}

	private static void ResolveDataFile() {
		if (data != null) return;
		data = FabricLoader.getInstance().getConfigDir().resolve("fake_afk.txt").toFile();
	}

	private void onServerClose(MinecraftServer server) {
		ResolveDataFile();
		try {
			String s = "";
			s += commands.savePermissions();
			s += FakePlayerInfo.SavePlayerNames();
			FileWriter writer = new FileWriter(data);
			writer.write(s);
			writer.close();
		} catch (IOException e) {
			FakeAFK.info("Failed to save data");
		}
	}

	private void onDisconnect(ServerPlayNetworkHandler handler, MinecraftServer server) {
		ServerPlayerEntity player = handler.getPlayer();
		FakePlayerInfo info = getFakePlayerInfo(player);
		if (info != null) {
			info.realPlayerDisconnect();
		}
	}

	private void onConnect(ServerPlayNetworkHandler handler, PacketSender packetSender, MinecraftServer server) {
		ServerPlayerEntity player = handler.getPlayer();
		FakePlayerInfo info = getFakePlayerInfo(player);

		//filter out fake players, this isnt needed on disconnect as that method doesn't trigger for fake players
		String name = player.getNameForScoreboard();
		if (name.contains("-")) return;

		if (info != null) {
			info.updatePlayer(player);
			info.realPlayerJoin();
		} else {
			info = new FakePlayerInfo(player);
			fakePlayers.add(info);
		}
	}

	public void logFakeDeath(ServerPlayerEntity player) {
		String name = player.getNameForScoreboard();
		if (!name.contains("-")) return;
		for (FakePlayerInfo fakePlayerInfo : fakePlayers) {
			fakePlayerInfo.tryLogFakeDeath(name);
		}
	}

	public FakePlayerInfo getFakePlayerInfo(ServerPlayerEntity player) {
		if (player == null) return null;
		UUID uuid = player.getUuid();
		for (FakePlayerInfo info : fakePlayers) {
			if (info.uuidEquals(uuid)) {
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

	public void onTick(MinecraftServer server) {
		for (FakePlayerInfo info : fakePlayers) {
			info.tick();
		}
	}

	public static void info(String s) {
		LOGGER.info(s);
	}
}