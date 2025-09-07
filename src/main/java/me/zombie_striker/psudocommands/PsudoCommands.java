package me.zombie_striker.psudocommands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class PsudoCommands extends JavaPlugin {

    public void onEnable() {
        PsudoCommandExecutor executor = new PsudoCommandExecutor();
        getLogger().log(Level.INFO, "Using new Paper Brigadier registering !");

        registerBrigadierCommand(executor, new String[]{"psudo", "psudouuid", "psudoas", "psudoasraw", "psudoasop", "psudoasconsole"});
    }

    private void registerBrigadierCommand(PsudoCommandExecutor executor, String[] commandList) {
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            for(String commandName : commandList) {
                LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(commandName);

                command.executes(ctx -> {
                    ctx.getSource().getSender().sendPlainMessage(CommandUtils.EMPTY_COMMAND_ERROR);
                    return 1;
                });

                RequiredArgumentBuilder<CommandSourceStack, String> psudoArguments = Commands.argument("psudoargs", StringArgumentType.greedyString());
                psudoArguments.suggests((ctx, builder) -> CommandUtils.getArgumentSuggestion(ctx, builder, executor, commandName));
                psudoArguments.executes(ctx -> CommandUtils.getArgumentExecutes(ctx, executor, PsudoCommandExecutor.PsudoCommandType.getType(commandName)));

                command.then(psudoArguments);

                LiteralCommandNode<CommandSourceStack> buildCommand = command.build();
                commands.registrar().register(buildCommand);
            }
        });
    }
}