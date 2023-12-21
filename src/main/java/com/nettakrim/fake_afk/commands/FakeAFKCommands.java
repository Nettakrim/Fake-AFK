package com.nettakrim.fake_afk.commands;

import com.mojang.brigadier.tree.RootCommandNode;
import com.nettakrim.fake_afk.FakeAFK;
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

    public static int namePermissionLevel = 0;
    public static int readyPermissionLevel = 0;
    public static int summonPermissionLevel = 0;
    public static int allowRealNamesPermissionLevel = 3;

    public void loadPermissions(PeekableScanner scanner) {
        while (scanner.hasNextLine()) {
            String s = scanner.peek();
            if (!s.contains(": ")) {
                return;
            }
            String[] halves = s.split(": ");
            int value = FakeAFK.parseInt(halves[1], -1);
            switch (halves[0]) {
                case "name_permission_level" -> namePermissionLevel = value == -1 ? namePermissionLevel : value;
                case "ready_permission_level" -> readyPermissionLevel = value == -1 ? readyPermissionLevel : value;
                case "summon_permission_level" -> summonPermissionLevel = value == -1 ? summonPermissionLevel : value;
                case "allow_real_names_permission_level" -> allowRealNamesPermissionLevel = value == -1 ? allowRealNamesPermissionLevel : value;
                default -> {
                    return;
                }
            }
            scanner.nextLine();
        }
    }

    public String savePermissions() {
        return "name_permission_level: " + namePermissionLevel + "\n" +
               "ready_permission_level: " + readyPermissionLevel + "\n" +
               "summon_permission_level: " + summonPermissionLevel + "\n" +
               "allow_real_names_permission_level: " + allowRealNamesPermissionLevel + "\n";
    }
}
