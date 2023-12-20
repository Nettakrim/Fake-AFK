package com.nettakrim.fake_afk.commands;

import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.nettakrim.fake_afk.FakeAFK;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class FakeAFKCommands {
    public FakeAFKCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            RootCommandNode<ServerCommandSource> root = dispatcher.getRoot();

            registerReady(root);
        });
    }

    public void registerReady(RootCommandNode<ServerCommandSource> root) {
        LiteralCommandNode<ServerCommandSource> helpNode = CommandManager
                .literal("afk:ready")
                .executes(new ReadyCommand())
                .build();

        root.addChild(helpNode);
    }
}
