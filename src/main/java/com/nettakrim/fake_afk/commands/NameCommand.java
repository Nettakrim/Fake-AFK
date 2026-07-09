package com.nettakrim.fake_afk.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.nettakrim.fake_afk.FakeAFK;
import com.nettakrim.fake_afk.FakePlayerInfo;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class NameCommand implements Command<CommandSourceStack> {
    public static LiteralCommandNode<CommandSourceStack> getNode() {
        return Commands
                .literal("afk:name")
                .requires(context -> FakeAFKCommands.hasPermission(context, FakeAFKCommands.namePermissionLevel))
                .then(
                        Commands.argument("name", StringArgumentType.word())
                       .executes(new NameCommand())
                )
                .executes(NameCommand::help)
                .build();
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayer();
        String name = StringArgumentType.getString(context, "name");
        if (!(name.contains("-") || FakeAFKCommands.hasPermission(context.getSource(), FakeAFKCommands.allowRealNamesPermissionLevel))) {
            FakeAFK.instance.say(player, "you must have a - somewhere in the name to distinguish Fake-You from real players (for instance is-steve-afk)");
            return 0;
        }
        FakePlayerInfo fakePlayerInfo = FakeAFK.instance.getFakePlayerInfo(context.getSource().getPlayer());
        if (fakePlayerInfo == null) return 0;
        return fakePlayerInfo.setName(name) ? 1 : 0;
    }

    private static int help(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        FakePlayerInfo fakePlayerInfo = FakeAFK.instance.getFakePlayerInfo(player);
        if (fakePlayerInfo == null) return 0;
        FakeAFK.instance.say(player, "Fake-You is currently called "+fakePlayerInfo.getName()+"\nuse /afk:name <name> to rename them");
        return 1;
    }
}