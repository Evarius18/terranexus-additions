package net.evarius.tnadditions.client.marking;

import net.evarius.tnadditions.item.ModItems;
import net.evarius.tnadditions.marking.RoadMarking;
import net.evarius.tnadditions.marking.geometry.MarkingGeometry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class RoadMarkingRenderer {
    private static final double MAX_RENDER_DISTANCE_SQUARED = 384.0 * 384.0;
    private static final double SURFACE_OFFSET = 0.003;

    public static void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        Vec3d camera = context.camera().getPos();
        BlockHitResult surface = targetedSurface(client);
        Vec3d hoveredPoint = surface == null ? null : snappedSurfacePoint(surface);

        List<RoadMarking> visible = new ArrayList<>(ClientRoadMarkings.around(camera, 25));
        RoadMarking preview = hoveredPoint == null
                ? RoadMarkingEditorSession.preview()
                : RoadMarkingEditorSession.previewWithPoint(hoveredPoint);
        if (preview != null) {
            visible.removeIf(marking -> marking.id().equals(preview.id()));
            visible.add(preview);
        }
        visible.removeIf(marking -> !marking.enabled()
                || marking.bounds().squaredMagnitude(camera) > MAX_RENDER_DISTANCE_SQUARED);
        visible.sort(Comparator.comparingInt(marking -> marking.style().renderOrder()));

        VertexConsumer quads = context.consumers().getBuffer(RenderLayer.getDebugQuads());
        for (RoadMarking marking : visible) {
            MarkingGeometry geometry = ClientRoadMarkings.GEOMETRY_CACHE.get(marking);
            if (context.frustum() != null && !context.frustum().isVisible(geometry.bounds())) continue;
            for (MarkingGeometry.Quad quad : geometry.quads()) {
                int color = effectiveColor(marking, quadCenter(quad), client);
                vertex(quads, quad.a(), camera, color);
                vertex(quads, quad.b(), camera, color);
                vertex(quads, quad.c(), camera, color);
                vertex(quads, quad.d(), camera, color);
            }
        }

        if (surface != null && hoveredPoint != null) {
            drawSurfaceGrid(context, quads, surface, hoveredPoint, camera);
        }
        drawControlPoints(context, camera);
    }

    /**
     * The debug quad layer is intentionally unlit, therefore markings receive
     * sampled vanilla sky/block light here. No material produces light by
     * itself; "reflective" only changes paint albedo very slightly.
     */
    private static int effectiveColor(RoadMarking marking, Vec3d sample, MinecraftClient client) {
        int base = marking.style().color();
        int light = 15;
        if (client.world != null) {
            BlockPos pos = BlockPos.ofFloored(sample.add(0.0, 0.05, 0.0));
            int blockLight = client.world.getLightLevel(LightType.BLOCK, pos);
            int skyLight = Math.max(0,
                    client.world.getLightLevel(LightType.SKY, pos) - client.world.getAmbientDarkness());
            light = Math.max(blockLight, skyLight);
        }

        float materialAlbedo = switch (marking.style().material()) {
            case "temporary" -> 0.92F;
            case "reflective" -> 1.02F;
            default -> 1.0F;
        };
        float illumination = 0.12F + 0.88F * (light / 15.0F);
        float grime = (1.0F - marking.style().dirt() * 0.45F) * materialAlbedo * illumination;
        int red = Math.min(255, Math.round(((base >> 16) & 255) * grime));
        int green = Math.min(255, Math.round(((base >> 8) & 255) * grime));
        int blue = Math.min(255, Math.round((base & 255) * grime));
        int alpha = Math.round(255 * marking.style().opacity()
                * (1.0F - marking.style().wear() * 0.65F));
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    private static BlockHitResult targetedSurface(MinecraftClient client) {
        if (client.player == null || client.currentScreen != null
                || !(client.crosshairTarget instanceof BlockHitResult hit)
                || hit.getType() != HitResult.Type.BLOCK) return null;
        boolean holdingEditor = client.player.getMainHandStack().isOf(ModItems.ROAD_MARKING_EDITOR)
                || client.player.getOffHandStack().isOf(ModItems.ROAD_MARKING_EDITOR);
        return holdingEditor ? hit : null;
    }

    private static Vec3d snappedSurfacePoint(BlockHitResult hit) {
        Direction normal = hit.getSide();
        Vec3d snapped = MarkingPlacementSettings.snapToSurfaceGrid(hit.getPos(), normal);
        return snapped.add(normal.getOffsetX() * 0.01, normal.getOffsetY() * 0.01,
                normal.getOffsetZ() * 0.01);
    }

    private static void drawSurfaceGrid(WorldRenderContext context, VertexConsumer quads,
                                        BlockHitResult hit, Vec3d active, Vec3d camera) {
        Direction face = hit.getSide();
        Vec3d normal = new Vec3d(face.getOffsetX(), face.getOffsetY(), face.getOffsetZ());
        Vec3d origin = faceOrigin(hit.getBlockPos(), face).add(normal.multiply(SURFACE_OFFSET));
        Vec3d u = face.getAxis() == Direction.Axis.X ? new Vec3d(0, 0, 1) : new Vec3d(1, 0, 0);
        Vec3d v = face.getAxis() == Direction.Axis.Y ? new Vec3d(0, 0, 1) : new Vec3d(0, 1, 0);
        double step = MarkingPlacementSettings.gridSize();
        if (step > 0.0) {
            int divisions = Math.max(1, (int) Math.round(1.0 / step));
            double actualStep = 1.0 / divisions;
            for (int i = 0; i <= divisions; i++) {
                double offset = i * actualStep;
                drawStrip(quads, origin.add(v.multiply(offset)), u, v, camera, 0x8839BFFF);
                drawStrip(quads, origin.add(u.multiply(offset)), v, u, camera, 0x8839BFFF);
                for (int j = 0; j <= divisions; j++) {
                    Vec3d point = origin.add(u.multiply(offset)).add(v.multiply(j * actualStep))
                            .subtract(camera);
                    VertexRendering.drawFilledBox(context.matrixStack(), quads,
                            point.x - 0.012, point.y - 0.012, point.z - 0.012,
                            point.x + 0.012, point.y + 0.012, point.z + 0.012,
                            0.2F, 0.65F, 1.0F, 0.55F);
                }
            }
        }

        Vec3d highlighted = active.subtract(camera);
        VertexRendering.drawFilledBox(context.matrixStack(), quads,
                highlighted.x - 0.055, highlighted.y - 0.055, highlighted.z - 0.055,
                highlighted.x + 0.055, highlighted.y + 0.055, highlighted.z + 0.055,
                1.0F, 0.55F, 0.05F, 0.92F);
    }

    private static void drawStrip(VertexConsumer consumer, Vec3d start, Vec3d direction,
                                  Vec3d widthAxis, Vec3d camera, int color) {
        Vec3d halfWidth = widthAxis.multiply(0.003);
        Vec3d a = start.subtract(halfWidth);
        Vec3d b = start.add(direction).subtract(halfWidth);
        Vec3d c = start.add(direction).add(halfWidth);
        Vec3d d = start.add(halfWidth);
        vertex(consumer, a, camera, color);
        vertex(consumer, b, camera, color);
        vertex(consumer, c, camera, color);
        vertex(consumer, d, camera, color);
    }

    private static Vec3d faceOrigin(BlockPos pos, Direction face) {
        return switch (face) {
            case UP -> new Vec3d(pos.getX(), pos.getY() + 1, pos.getZ());
            case DOWN, NORTH, WEST -> new Vec3d(pos.getX(), pos.getY(), pos.getZ());
            case SOUTH -> new Vec3d(pos.getX(), pos.getY(), pos.getZ() + 1);
            case EAST -> new Vec3d(pos.getX() + 1, pos.getY(), pos.getZ());
        };
    }

    private static void drawControlPoints(WorldRenderContext context, Vec3d camera) {
        if (!RoadMarkingEditorSession.active()) return;
        VertexConsumer boxes = context.consumers().getBuffer(RenderLayer.getDebugFilledBox());
        List<Vec3d> points = RoadMarkingEditorSession.points();
        for (int i = 0; i < points.size(); i++) {
            Vec3d p = points.get(i).subtract(camera);
            boolean selected = i == RoadMarkingEditorSession.selectedPoint();
            VertexRendering.drawFilledBox(context.matrixStack(), boxes,
                    p.x - 0.09, p.y - 0.09, p.z - 0.09,
                    p.x + 0.09, p.y + 0.09, p.z + 0.09,
                    selected ? 1.0F : 0.1F, selected ? 0.5F : 0.8F, 0.1F, 0.85F);
        }
    }

    private static Vec3d quadCenter(MarkingGeometry.Quad quad) {
        return quad.a().add(quad.b()).add(quad.c()).add(quad.d()).multiply(0.25);
    }

    private static void vertex(VertexConsumer consumer, Vec3d point, Vec3d camera, int color) {
        consumer.vertex((float) (point.x - camera.x), (float) (point.y - camera.y),
                        (float) (point.z - camera.z))
                .color(color);
    }

    private RoadMarkingRenderer() {
    }
}
