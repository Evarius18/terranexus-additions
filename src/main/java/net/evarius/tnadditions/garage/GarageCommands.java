package net.evarius.tnadditions.garage;

import net.evarius.tnadditions.block.custom.AnimatedGarageDoorBlock;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/** Owner-facing authorization management for garage controllers. */
public final class GarageCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registry, environment) -> dispatcher.register(
                literal("tngarage")
                        .then(literal("grant").then(argument("door", BlockPosArgumentType.blockPos())
                                .then(argument("player", EntityArgumentType.player()).executes(context -> change(
                                        context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "door"),
                                        EntityArgumentType.getPlayer(context, "player"), true)))))
                        .then(literal("revoke").then(argument("door", BlockPosArgumentType.blockPos())
                                .then(argument("player", EntityArgumentType.player()).executes(context -> change(
                                        context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "door"),
                                        EntityArgumentType.getPlayer(context, "player"), false)))))));
    }

    private static int change(ServerCommandSource source, BlockPos pos, ServerPlayerEntity target, boolean grant) {
        ServerPlayerEntity actor;
        try { actor = source.getPlayerOrThrow(); }
        catch (com.mojang.brigadier.exceptions.CommandSyntaxException exception) { return 0; }
        ServerWorld world = source.getWorld();
        if (!(world.getBlockState(pos).getBlock() instanceof AnimatedGarageDoorBlock)) {
            source.sendError(Text.translatable("message.terranexus.garage.missing"));
            return 0;
        }
        GarageAccessState state = GarageAccessState.get(source.getServer());
        if (!state.mayManage(actor, world, pos)) {
            source.sendError(Text.translatable("message.terranexus.garage.no_permission"));
            return 0;
        }
        boolean changed = grant ? state.grant(world, pos, target.getUuid()) : state.revoke(world, pos, target.getUuid());
        if (!changed) return 0;
        source.sendFeedback(() -> Text.translatable(grant
                ? "message.terranexus.garage.granted" : "message.terranexus.garage.revoked", target.getDisplayName()), true);
        return 1;
    }

    private GarageCommands() {}
}
