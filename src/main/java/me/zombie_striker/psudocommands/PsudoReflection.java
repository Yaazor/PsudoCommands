package me.zombie_striker.psudocommands;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.LocalCoordinates;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.core.Direction;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.command.VanillaCommandWrapper;
import org.bukkit.entity.Entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

// https://mappings.dev/1.21.1/net/minecraft/commands/CommandSourceStack.html
public class PsudoReflection {

    public static CommandSender getBukkitSender(CommandSourceStack commandWrapperListener) {
        Objects.requireNonNull(commandWrapperListener, "commandWrapperListener");

        net.minecraft.world.entity.Entity entity = commandWrapperListener.getEntity();
        if (entity == null) {
            return null;
        } else {
            return getCommandSource(entity).getBukkitSender(commandWrapperListener);
        }
    }

    public static CommandSource getCommandSource(net.minecraft.world.entity.Entity entity) {
        if(entity instanceof ServerPlayer serverPlayer) {
            return serverPlayer.commandSource();
        }else{
            return entity.createCommandSourceStackForNameResolution(entity.level().getMinecraftWorld()).source;
        }
    }

    public static CommandSender getBukkitBasedSender(CommandSourceStack commandWrapperListener) {
        return commandWrapperListener.getBukkitSender();
    }

    public static Location getBukkitLocation(CommandSourceStack commandWrapperListener) {
        return commandWrapperListener.getBukkitLocation();
    }

    // Partially extracted from CraftServer class
    public static List<Entity> selectEntities(CommandSourceStack commandSourceStack, String selector) {
        List<? extends net.minecraft.world.entity.Entity> nms;
        List<Entity> result = new ArrayList<>();

        try {
            EntityArgument arg_entities = EntityArgument.entities();
            StringReader reader = new StringReader(selector);

            EntitySelector entitySelector = arg_entities.parse(reader);
            nms = entitySelector.findEntities((CommandSourceStack) commandSourceStack);
            Preconditions.checkArgument(!reader.canRead(), "Spurious trailing data in selector: " + selector);

            for (net.minecraft.world.entity.Entity entity : nms) {
                // use getBukkitSender because on entity it just returns the BukkitEntity
                result.add((Entity) getCommandSource(entity).getBukkitSender(commandSourceStack));
            }

        } catch (CommandSyntaxException ex) {
            throw new IllegalArgumentException("Could not parse selector: " + selector, ex);
        }

        return result;
    }

    public static Location getLocalCoord(double x, double y, double z, CommandSourceStack commandWrapperListener) {
        LocalCoordinates localCoordinates = new LocalCoordinates(x, y, z);
        Vec3 position = localCoordinates.getPosition(commandWrapperListener);

        Location loc = getBukkitLocation(commandWrapperListener);
        return new Location(loc.getWorld(), position.get(Direction.Axis.X), position.get(Direction.Axis.Y), position.get(Direction.Axis.Z));
    }

    public static CommandSourceStack getCommandWrapperListenerObject(CommandSender sender) {
        return VanillaCommandWrapper.getListener(sender);
    }

    public static Map<String, Command> getKnownCommands() {
        CommandMap commandMap = Bukkit.getCommandMap();
        return commandMap.getKnownCommands();
    }

    public static boolean dispatchCommandIgnorePerms(CommandSender sender, String commandstr) {
        // TODO : org.apache.commons.lang3 will be removed in the future, keep an eye here
        //String[] args = StringUtils.split(commandstr, ' ');
        String[] args = commandstr.split(" ");
        if (args.length == 0) {
            return false;
        }
        String sentCommandLabel = args[0].toLowerCase(java.util.Locale.ENGLISH);
        Command command = getKnownCommands().get(sentCommandLabel.toLowerCase(java.util.Locale.ENGLISH));
        if (command == null) {
            return false;
        }

        PaperCommandDispatcher.dispatchCommandPaper(sender, commandstr, command, sentCommandLabel, args);
        return true;
    }

    public static boolean executeIgnorePerms(Command command, CommandSender sender, String label, String[] args) {
        if (command instanceof PluginCommand) {
            PluginCommand pluginCommand = (PluginCommand) command;
            boolean success;
            if (!pluginCommand.getPlugin().isEnabled()) {
                throw new CommandException("Cannot execute command '" + label + "' in plugin " + pluginCommand.getPlugin().getDescription().getFullName() + " - plugin is disabled.");
            }
            try {
                success = pluginCommand.getExecutor().onCommand(sender, pluginCommand, label, args);
            } catch (Throwable ex) {
                throw new CommandException("Unhandled exception executing command '" + label + "' in plugin " + pluginCommand.getPlugin().getDescription().getFullName(), ex);
            }
            if (!success && !pluginCommand.getUsage().isEmpty()) {
                for (String line : pluginCommand.getUsage().replace("<command>", label).split("\n")) {
                    sender.sendMessage(line);
                }
            }
            return success;
        } else {
            // don't check VanillaCommandWrapper type because we can ignore psudo and use vanilla behavior
            return command.execute(sender, label, args);
        }
    }
}
