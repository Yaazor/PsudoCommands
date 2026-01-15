package me.zombie_striker.psudocommands;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.LocalCoordinates;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Entity;

import java.util.*;

public class PsudoReflection {

    public static CommandSender getBukkitSender(CommandSourceStack commandWrapperListener) {
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
            nms = entitySelector.findEntities(commandSourceStack);
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

    public static Map<String, Command> getKnownCommands() {
        CommandMap commandMap = Bukkit.getCommandMap();
        return commandMap.getKnownCommands();
    }

    public static boolean dispatchCommandIgnorePerms(CommandSourceStack sourceStack, String commandstr) {
        String[] args = commandstr.split(" ");
        if (args.length == 0) {
            return false;
        }
        String sentCommandLabel = args[0].toLowerCase(java.util.Locale.ENGLISH);
        Command command = getKnownCommands().get(sentCommandLabel.toLowerCase(java.util.Locale.ENGLISH));
        var returned = command != null;
        var executor = sourceStack.getSender();
        if(sourceStack.getExecutor() != null) executor = sourceStack.getExecutor();

        Bukkit.getServer().dispatchCommand(executor, commandstr);
        return returned;
    }
}
