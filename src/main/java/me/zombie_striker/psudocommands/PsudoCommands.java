package me.zombie_striker.psudocommands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class PsudoCommands extends JavaPlugin {

    public static ReloadableRegistrarEvent<Commands> CURRENT_COMMANDS_LIFECYCLE = null;

    public void onEnable() {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("testcmd")
                .then(Commands.literal("argument_one"))
                .then(Commands.literal("argument_two"));

        LiteralCommandNode<CommandSourceStack> buildCommand = command.build();

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(buildCommand);
        });

        PsudoCommandExecutor executor = new PsudoCommandExecutor(this);

        PluginCommand[] commands = new PluginCommand[]{ getCommand("psudo"), getCommand("psudouuid"),
                getCommand("psudoas"), getCommand("psudoasraw"),
                getCommand("psudoasop"), getCommand("psudoasconsole") };

        getLogger().log(Level.INFO, "Using new Paper Brigadier registering !");
        PaperCommandRegistering.registerPaperBrigadierCommand(this, executor, commands);
    }
}