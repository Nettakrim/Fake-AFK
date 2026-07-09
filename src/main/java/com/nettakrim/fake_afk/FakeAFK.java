package com.nettakrim.fake_afk;

import com.nettakrim.fake_afk.commands.FakeAFKCommands;
import net.fabricmc.api.ModInitializer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.UUID;

public class FakeAFK implements ModInitializer {
	public static FakeAFK instance;
    public static final Logger LOGGER = LoggerFactory.getLogger("fake-afk");

	public ArrayList<FakePlayerInfo> fakePlayers;

	private final TextColor textColor = TextColor.fromRgb(0xAAAAAA);
	private final TextColor nameTextColor = TextColor.fromRgb(0xF07F1D);

	public FakeAFKCommands commands;
	public Connection connection;
	public Data data;

	@Override
	public void onInitialize() {
		instance = this;
		fakePlayers = new ArrayList<>();

		commands = new FakeAFKCommands();
		connection = new Connection();
		data = new Data();
	}

	public FakePlayerInfo getFakePlayerInfo(ServerPlayer player) {
		if (player == null) return null;
		UUID uuid = player.getUUID();
		for (FakePlayerInfo info : fakePlayers) {
			if (info.uuidEquals(uuid)) {
				return info;
			}
		}
		return null;
	}

	public void say(ServerPlayer player, String message, Object... args) {
		if (player == null) return;
		player.sendSystemMessage(formatText(message, args));
	}

	public void say(MinecraftServer server, String message, Object... args) {
		if (server == null) return;
		Component text = formatText(message, args);
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			player.sendSystemMessage(text);
		}
	}

	private Component formatText(String message, Object... args) {
		return Component.literal("[Fake AFK] ").setStyle(Style.EMPTY.withColor(nameTextColor)).append(Component.literal(message.formatted(args)).setStyle(Style.EMPTY.withColor(textColor)));
	}

	public static void info(String s) {
		LOGGER.info(s);
	}

	public static int parseInt(String s, int defaultValue) {
		try {
			return Integer.parseInt(s);
		} catch (Exception ignored) {
			return defaultValue;
		}
	}
}