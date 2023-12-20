package com.nettakrim.fake_afk.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nettakrim.fake_afk.FakeAFK;
import net.minecraft.server.command.ServerCommandSource;

public class ReadyCommand implements Command<ServerCommandSource> {
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return FakeAFK.instance.readyPlayer(context.getSource().getPlayer()) ? 1 : 0;
    }
}