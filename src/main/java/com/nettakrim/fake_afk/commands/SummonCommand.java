package com.nettakrim.fake_afk.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.nettakrim.fake_afk.FakeAFK;
import com.nettakrim.fake_afk.FakePlayerInfo;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class SummonCommand implements Command<ServerCommandSource> {
    public static LiteralCommandNode<ServerCommandSource> getNode() {
        return CommandManager
                .literal("afk:summon")
                .requires((source)->source.hasPermissionLevel(FakeAFKCommands.summonPermissionLevel))
                .executes(new SummonCommand())
                .build();
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        FakePlayerInfo info = FakeAFK.instance.getFakePlayerInfo(context.getSource().getPlayer());
        if (info != null) {
            info.toggleSummon();
        }
        return 1;
    }
}