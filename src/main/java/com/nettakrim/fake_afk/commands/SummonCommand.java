package com.nettakrim.fake_afk.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.nettakrim.fake_afk.FakeAFK;
import com.nettakrim.fake_afk.FakePlayerInfo;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class SummonCommand implements Command<CommandSourceStack> {
    public static LiteralCommandNode<CommandSourceStack> getNode() {
        return Commands
                .literal("afk:summon")
                .requires(source -> FakeAFKCommands.hasPermission(source, FakeAFKCommands.summonPermissionLevel))
                .executes(new SummonCommand())
                .build();
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        FakePlayerInfo info = FakeAFK.instance.getFakePlayerInfo(context.getSource().getPlayer());
        if (info != null) {
            info.toggleSummon();
        }
        return 1;
    }
}