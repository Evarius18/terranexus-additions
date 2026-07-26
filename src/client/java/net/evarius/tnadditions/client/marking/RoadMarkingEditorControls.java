package net.evarius.tnadditions.client.marking;

import net.evarius.tnadditions.item.ModItems;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;

public final class RoadMarkingEditorControls {
    private static final String CATEGORY = "key.categories.tnadditions";
    private static final KeyBinding MENU = key("key.terranexus.marking_menu", InputUtil.GLFW_KEY_M);
    private static final KeyBinding MODE = key("key.terranexus.marking_mode", InputUtil.GLFW_KEY_B);
    private static final KeyBinding FINISH = key("key.terranexus.marking_finish", InputUtil.GLFW_KEY_ENTER);
    private static final KeyBinding REMOVE_POINT = key("key.terranexus.marking_remove_point", InputUtil.GLFW_KEY_DELETE);
    private static final KeyBinding DELETE_MARKING = key("key.terranexus.marking_delete", InputUtil.GLFW_KEY_X);
    private static final KeyBinding UNDO = key("key.terranexus.marking_undo", InputUtil.GLFW_KEY_BACKSPACE);
    private static final KeyBinding LANE_DIVIDER = key("key.terranexus.marking_lane_divider", InputUtil.GLFW_KEY_L);

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClient() || !player.getStackInHand(hand).isOf(ModItems.ROAD_MARKING_EDITOR)) {
                return ActionResult.PASS;
            }
            var side = hitResult.getSide();
            Vec3d point = MarkingPlacementSettings.snapToSurfaceGrid(hitResult.getPos(), side)
                    .add(side.getOffsetX() * 0.01, side.getOffsetY() * 0.01,
                            side.getOffsetZ() * 0.01);
            boolean changed = RoadMarkingEditorSession.handleWorldClick(point);
            if (!changed && RoadMarkingEditorSession.workflowMode() == RoadMarkingEditorSession.WorkflowMode.EDIT) {
                player.sendMessage(Text.translatable("message.terranexus.no_marking_found"), true);
            }
            return ActionResult.SUCCESS;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            boolean holdingTool = client.player.getMainHandStack().isOf(ModItems.ROAD_MARKING_EDITOR)
                    || client.player.getOffHandStack().isOf(ModItems.ROAD_MARKING_EDITOR);
            if (!holdingTool) return;

            while (MENU.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new RoadMarkingEditorScreen());
            }
            while (MODE.wasPressed()) toggleMode(client);
            while (FINISH.wasPressed()) {
                if (client.currentScreen == null && RoadMarkingEditorSession.save()) {
                    message(client, "message.terranexus.road_marking_saved");
                }
            }
            while (REMOVE_POINT.wasPressed()) {
                if (client.currentScreen == null && RoadMarkingEditorSession.removeSelectedPoint()) {
                    message(client, "message.terranexus.control_point_removed");
                }
            }
            while (DELETE_MARKING.wasPressed()) {
                if (client.currentScreen == null && RoadMarkingEditorSession.delete()) {
                    message(client, "message.terranexus.road_marking_deleted");
                }
            }
            while (UNDO.wasPressed()) {
                if (client.currentScreen == null && RoadMarkingEditorSession.removeLastPoint()) {
                    message(client, "message.terranexus.last_point_removed");
                }
            }
            while (LANE_DIVIDER.wasPressed()) activateLaneDivider(client);
        });
    }

    private static void toggleMode(MinecraftClient client) {
        if (client.currentScreen != null) return;
        var next = RoadMarkingEditorSession.workflowMode() == RoadMarkingEditorSession.WorkflowMode.PLACE
                ? RoadMarkingEditorSession.WorkflowMode.EDIT
                : RoadMarkingEditorSession.WorkflowMode.PLACE;
        if (!RoadMarkingEditorSession.setWorkflowMode(next)) {
            message(client, "message.terranexus.finish_current_marking");
            return;
        }
        message(client, next == RoadMarkingEditorSession.WorkflowMode.PLACE
                ? "message.terranexus.mode_place" : "message.terranexus.mode_edit");
    }

    private static void activateLaneDivider(MinecraftClient client) {
        if (client.currentScreen != null) return;
        if (RoadMarkingEditorSession.active() && RoadMarkingEditorSession.dirty()) {
            message(client, "message.terranexus.finish_current_marking");
            return;
        }
        if (!RoadMarkingEditorSession.setWorkflowMode(RoadMarkingEditorSession.WorkflowMode.PLACE)) {
            message(client, "message.terranexus.finish_current_marking");
            return;
        }
        RoadMarkingEditorSession.setType(net.evarius.tnadditions.marking.MarkingTypes.LANE_DIVIDER);
        var style = RoadMarkingEditorSession.style();
        RoadMarkingEditorSession.setStyle(new net.evarius.tnadditions.marking.MarkingStyle(
                0.12, style.color(), style.material(), style.opacity(), style.wear(), style.dirt(),
                3.0, 6.0, style.heightOffset(), style.lateralOffset(), style.cornerRadius(),
                style.renderOrder(), style.collision()
        ));
        message(client, "message.terranexus.lane_divider_ready");
    }

    private static void message(MinecraftClient client, String key) {
        if (client.player != null) client.player.sendMessage(Text.translatable(key), true);
    }

    private static KeyBinding key(String translation, int code) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(
                translation, InputUtil.Type.KEYSYM, code, CATEGORY
        ));
    }

    private RoadMarkingEditorControls() {
    }
}
