package com.nettakrim.fake_afk.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.nettakrim.fake_afk.FakeAFK;
import com.nettakrim.fake_afk.FakePlayerInfo;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class ReadyCommand implements Command<ServerCommandSource> {
    public static LiteralCommandNode<ServerCommandSource> getNode() {
        return CommandManager
                .literal("afk:ready")
                .requires((source)->source.hasPermissionLevel(FakeAFKCommands.readyPermissionLevel))
                .executes(new ReadyCommand())
                .build();
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        FakePlayerInfo fakePlayerInfo = FakeAFK.instance.getFakePlayerInfo(context.getSource().getPlayer());
        if (fakePlayerInfo == null) return 0;
        fakePlayerInfo.readyForDisconnect();
        return 1;
    }
}