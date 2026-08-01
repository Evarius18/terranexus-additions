package net.evarius.tnadditions.item;

import net.evarius.tnadditions.block.custom.AnimatedGarageDoorBlock;
import net.evarius.tnadditions.config.InfrastructureConfig;
import net.evarius.tnadditions.garage.GarageAccessState;
import net.evarius.tnadditions.infrastructure.InfrastructureAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Owner-bound key or ranged remote for sectional garage doors. */
public final class GarageControllerItem extends Item {
    private static final String DATA_KEY = "tnadditions_garage";
    private final boolean remote;

    public GarageControllerItem(Settings settings, boolean remote) {
        super(settings);
        this.remote = remote;
    }

    @Override public ActionResult useOnBlock(ItemUsageContext context) {
        if (!(context.getWorld() instanceof ServerWorld world)
                || !(context.getPlayer() instanceof ServerPlayerEntity player)) return ActionResult.SUCCESS;
        BlockPos pos = context.getBlockPos();
        if (!(world.getBlockState(pos).getBlock() instanceof AnimatedGarageDoorBlock door)) return ActionResult.PASS;
        GarageAccessState access = GarageAccessState.get(world.getServer());
        if (!InfrastructureAccess.mayInteract(player, world, pos) || !access.permits(player, world, pos)) {
            player.sendMessage(Text.translatable("message.terranexus.garage.no_permission"), true);
            return ActionResult.FAIL;
        }
        ItemStack stack = context.getStack();
        if (player.isSneaking() || binding(stack) == null) {
            bind(stack, world, pos, player);
            player.sendMessage(Text.translatable("message.terranexus.garage.bound"), true);
            return ActionResult.SUCCESS_SERVER;
        }
        return door.toggleFromController(world, pos, player);
    }

    @Override public ActionResult use(World world, net.minecraft.entity.player.PlayerEntity user, Hand hand) {
        if (!remote || !(world instanceof ServerWorld currentWorld) || !(user instanceof ServerPlayerEntity player)) {
            return ActionResult.PASS;
        }
        Binding binding = binding(user.getStackInHand(hand));
        if (binding == null || !binding.holder().equals(player.getUuidAsString())) {
            player.sendMessage(Text.translatable("message.terranexus.garage.not_bound"), true);
            return ActionResult.FAIL;
        }
        ServerWorld targetWorld = currentWorld.getServer().getWorld(RegistryKey.of(RegistryKeys.WORLD,
                Identifier.of(binding.dimension())));
        BlockPos pos = BlockPos.fromLong(binding.position());
        if (targetWorld == null || targetWorld != currentWorld
                || player.squaredDistanceTo(pos.toCenterPos()) > (double) InfrastructureConfig.garageRemoteRange()
                * InfrastructureConfig.garageRemoteRange()) {
            player.sendMessage(Text.translatable("message.terranexus.garage.out_of_range"), true);
            return ActionResult.FAIL;
        }
        if (targetWorld.getBlockState(pos).getBlock() instanceof AnimatedGarageDoorBlock door) {
            return door.toggleFromController(targetWorld, pos, player);
        }
        player.sendMessage(Text.translatable("message.terranexus.garage.missing"), true);
        return ActionResult.FAIL;
    }

    private static void bind(ItemStack stack, ServerWorld world, BlockPos pos, ServerPlayerEntity player) {
        NbtCompound root = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        NbtCompound data = new NbtCompound();
        data.putString("dimension", world.getRegistryKey().getValue().toString());
        data.putLong("position", pos.asLong());
        data.putString("holder", player.getUuidAsString());
        root.put(DATA_KEY, data);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(root));
    }

    private static Binding binding(ItemStack stack) {
        NbtCompound root = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        NbtCompound data = root.getCompoundOrEmpty(DATA_KEY);
        String dimension = data.getString("dimension", "");
        String holder = data.getString("holder", "");
        if (dimension.isBlank() || holder.isBlank() || !data.contains("position")) return null;
        return new Binding(dimension, data.getLong("position", 0L), holder);
    }

    private record Binding(String dimension, long position, String holder) {}
}
