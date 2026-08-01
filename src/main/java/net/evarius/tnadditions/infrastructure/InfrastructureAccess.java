package net.evarius.tnadditions.infrastructure;

import net.evarius.tnadditions.TerraNexusAdditions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.lang.reflect.Method;

/** Optional bridge to TerraNexus land protection without making the core mod mandatory. */
public final class InfrastructureAccess {
    private static final Method LAND_CHECK = resolveLandCheck();

    public static boolean mayConfigure(ServerPlayerEntity player) { return player.hasPermissionLevel(2); }

    public static boolean mayInteract(ServerPlayerEntity player, ServerWorld world, BlockPos pos) {
        if (player.hasPermissionLevel(2)) return true;
        if (LAND_CHECK == null) return true;
        try { return Boolean.TRUE.equals(LAND_CHECK.invoke(null, player, world, pos, "interact")); }
        catch (ReflectiveOperationException exception) {
            TerraNexusAdditions.LOGGER.warn("TerraNexus land-protection bridge failed safely", exception);
            return false;
        }
    }

    private static Method resolveLandCheck() {
        if (!FabricLoader.getInstance().isModLoaded("terranexus")) return null;
        try {
            Class<?> protection=Class.forName("net.evarius.terranexus.landlord.LandlordProtection",false,InfrastructureAccess.class.getClassLoader());
            return protection.getMethod("isAllowed",ServerPlayerEntity.class,net.minecraft.world.World.class,BlockPos.class,String.class);
        } catch (ReflectiveOperationException exception) {
            TerraNexusAdditions.LOGGER.warn("TerraNexus is installed, but its public land-protection bridge is unavailable", exception);
            return null;
        }
    }
    private InfrastructureAccess() {}
}
