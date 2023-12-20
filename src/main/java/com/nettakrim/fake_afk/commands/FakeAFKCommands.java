package com.nettakrim.fake_afk.commands;

import com.mojang.brigadier.tree.RootCommandNode;
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
}
