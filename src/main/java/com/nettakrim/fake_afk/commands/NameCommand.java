package com.nettakrim.fake_afk.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.nettakrim.fake_afk.FakeAFK;
import com.nettakrim.fake_afk.FakePlayerInfo;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class NameCommand implements Command<ServerCommandSource> {
    public static LiteralCommandNode<ServerCommandSource> getNode() {
        return CommandManager
                .literal("afk:name")
                .requires((source)->source.hasPermissionLevel(FakeAFKCommands.namePermissionLevel))
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
        //admins can bypass the - requirement
        if (!(name.contains("-") || context.getSource().hasPermissionLevel(3))) {
            FakeAFK.instance.say(player, "you must have a - somewhere in the name to distinguish Fake-You from real players (for instance is-steve-afk)");
            return 0;
        }
        FakePlayerInfo fakePlayerInfo = FakeAFK.instance.getFakePlayerInfo(context.getSource().getPlayer());
        if (fakePlayerInfo == null) return 0;
        if (fakePlayerInfo.setName(name)) {
            FakeAFK.instance.say(player, "Fake-You has been renamed to "+name.toLowerCase());
            return 1;
        }
        FakeAFK.instance.say(player, name+" is already taken, or the name is otherwise reserved");
        return 0;
    }

    private static int help(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        FakePlayerInfo fakePlayerInfo = FakeAFK.instance.getFakePlayerInfo(player);
        if (fakePlayerInfo == null) return 0;
        FakeAFK.instance.say(player, "Fake-You is currently called "+fakePlayerInfo.getName()+"\nuse /afk:name <name> to rename them");
        return 1;
    }
}