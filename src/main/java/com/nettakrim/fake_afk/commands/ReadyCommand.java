package com.nettakrim.fake_afk.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.nettakrim.fake_afk.FakeAFK;
import com.nettakrim.fake_afk.FakePlayerInfo;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ReadyCommand implements Command<CommandSourceStack> {
    public static LiteralCommandNode<CommandSourceStack> getNode() {
        return Commands
                .literal("afk:ready")
                .requires(source -> FakeAFKCommands.hasPermission(source, FakeAFKCommands.readyPermissionLevel))
                .executes(new ReadyCommand())
                .build();
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        FakePlayerInfo fakePlayerInfo = FakeAFK.instance.getFakePlayerInfo(context.getSource().getPlayer());
        if (fakePlayerInfo == null) return 0;
        fakePlayerInfo.readyForDisconnect();
        return 1;
    }
}