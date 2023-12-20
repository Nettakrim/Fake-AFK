package com.nettakrim.fake_afk.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.nettakrim.fake_afk.FakeAFK;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class NameCommand implements Command<ServerCommandSource> {
    public static LiteralCommandNode<ServerCommandSource> getNode() {
        return CommandManager
                .literal("afk:name")
                .then(
                        CommandManager.argument("name", StringArgumentType.word())
                       .executes(new NameCommand())
                )
                .executes(NameCommand::help)
                .build();
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        String name = StringArgumentType.getString(context, "name");
        if (!name.contains("-")) {
            FakeAFK.instance.say(player, "you must have a - somewhere in the name to distinguish Fake You from real players (for instance is-steve-afk)");
            return 0;
        }
        FakeAFK.instance.say(player, "Fake You is now called "+name);
        return 1;
    }

    private static int help(CommandContext<ServerCommandSource> context) {
        FakeAFK.instance.say(context.getSource().getPlayer(), "use /afk:name <name> to rename Fake You");
        return 1;
    }
}