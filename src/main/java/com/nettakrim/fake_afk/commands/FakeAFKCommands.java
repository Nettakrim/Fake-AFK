package com.nettakrim.fake_afk.commands;

import com.mojang.brigadier.tree.RootCommandNode;
import com.nettakrim.fake_afk.PeekableScanner;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;

public class FakeAFKCommands {
    public FakeAFKCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            RootCommandNode<ServerCommandSource> root = dispatcher.getRoot();

            root.addChild(NameCommand.getNode());
            root.addChild(ReadyCommand.getNode());
            root.addChild(SummonCommand.getNode());
        });
    }

    public static int namePermissionLevel;
    public static int readyPermissionLevel;
    public static int summonPermissionLevel;

    public void loadPermissions(PeekableScanner scanner) {
        while (scanner.hasNextLine()) {
            String s = scanner.peek();
            if (!s.contains(":")) {
                return;
            }
            String[] halves = s.split(": ");
            int value = 0;
            try {
                value = Integer.parseInt(halves[1]);
            } catch (Exception ignored) {

            }
            switch (halves[0]) {
                case "name_permission_level" -> namePermissionLevel = value;
                case "ready_permission_level" -> readyPermissionLevel = value;
                case "summon_permission_level" -> summonPermissionLevel = value;
                default -> {
                    return;
                }
            }
            scanner.nextLine();
        }
    }

    public String savePermissions() {
        StringBuilder s = new StringBuilder();
        s.append("name_permission_level: ").append(namePermissionLevel).append("\n");
        s.append("ready_permission_level: ").append(readyPermissionLevel).append("\n");
        s.append("summon_permission_level: ").append(summonPermissionLevel).append("\n");
        return s.toString();
    }
}
