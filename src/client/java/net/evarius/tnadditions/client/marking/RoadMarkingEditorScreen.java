package net.evarius.tnadditions.client.marking;

import net.evarius.tnadditions.marking.MarkingStyle;
import net.evarius.tnadditions.marking.MarkingType;
import net.evarius.tnadditions.marking.MarkingTypes;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.DoubleConsumer;

public final class RoadMarkingEditorScreen extends Screen {
    private static final double[] OFFSET_PRESETS = {0.0, -0.5, 0.5, -1.75, 1.75};
    private static final String[] OFFSET_NAMES = {"center", "slight_left", "slight_right", "left", "right"};

    private final List<Identifier> types = new ArrayList<>();
    private int typeIndex;
    private int offsetPresetIndex;

    public RoadMarkingEditorScreen() {
        super(Text.translatable("screen.terranexus.road_marking_editor"));
        types.addAll(MarkingTypes.values().stream().map(MarkingType::id).toList());
        typeIndex = Math.max(0, types.indexOf(RoadMarkingEditorSession.type()));
        offsetPresetIndex = nearestOffsetPreset(RoadMarkingEditorSession.style().lateralOffset());
    }

    @Override
    protected void init() {
        int left = width / 2 - 180;
        int y = height / 2 - 170;
        addDrawableChild(ButtonWidget.builder(workflowText(), this::cycleWorkflow)
                .dimensions(left, y, 115, 20).build());
        addDrawableChild(ButtonWidget.builder(typeText(), this::cycleType)
                .dimensions(left + 120, y, 240, 20).build());

        y += 25;
        numberField(left, y, 112, "screen.terranexus.width", style().width(), this::setWidth);
        numberField(left + 124, y, 112, "screen.terranexus.dash", style().dashLength(), this::setDash);
        numberField(left + 248, y, 112, "screen.terranexus.gap", style().gapLength(), this::setGap);

        y += 25;
        numberField(left, y, 112, "screen.terranexus.opacity", style().opacity(), this::setOpacity);
        numberField(left + 124, y, 112, "screen.terranexus.wear", style().wear(), this::setWear);
        numberField(left + 248, y, 112, "screen.terranexus.dirt", style().dirt(), this::setDirt);

        y += 25;
        numberField(left, y, 112, "screen.terranexus.height", style().heightOffset(), this::setHeight);
        numberField(left + 124, y, 112, "screen.terranexus.offset_custom", style().lateralOffset(), this::setOffset);
        numberField(left + 248, y, 112, "screen.terranexus.corner_radius", style().cornerRadius(), this::setRadius);

        y += 25;
        addDrawableChild(ButtonWidget.builder(offsetText(), this::cycleOffset)
                .dimensions(left, y, 112, 20).build());
        addDrawableChild(ButtonWidget.builder(axisText(), this::cycleAxis)
                .dimensions(left + 124, y, 112, 20).build());
        addDrawableChild(ButtonWidget.builder(angleSnapText(), this::cycleAngleSnap)
                .dimensions(left + 248, y, 112, 20).build());

        y += 25;
        addDrawableChild(ButtonWidget.builder(gridText(), this::cycleGrid)
                .dimensions(left, y, 112, 20).build());
        numberField(left + 124, y, 112, "screen.terranexus.exact_length",
                MarkingPlacementSettings.exactLength(), MarkingPlacementSettings::setExactLength);
        numberField(left + 248, y, 112, "screen.terranexus.exact_angle",
                MarkingPlacementSettings.exactAngleDegrees(), MarkingPlacementSettings::setExactAngleDegrees);

        y += 25;
        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.terranexus.color"), button -> cycleColor())
                .dimensions(left, y, 112, 20).build());
        addDrawableChild(ButtonWidget.builder(materialText(), this::cycleMaterial)
                .dimensions(left + 124, y, 112, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.terranexus.select"), button -> {
            if (RoadMarkingEditorSession.setWorkflowMode(RoadMarkingEditorSession.WorkflowMode.EDIT)) {
                RoadMarkingEditorSession.setMode(RoadMarkingEditorSession.EditMode.SELECT);
                close();
            } else {
                button.setTooltip(Tooltip.of(Text.translatable("message.terranexus.finish_current_marking")));
            }
        }).dimensions(left + 248, y, 112, 20).build());

        y += 25;
        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.terranexus.move"), button -> {
            RoadMarkingEditorSession.setMode(RoadMarkingEditorSession.EditMode.MOVE);
            close();
        }).dimensions(left, y, 112, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.terranexus.insert"), button -> {
            RoadMarkingEditorSession.setMode(RoadMarkingEditorSession.EditMode.INSERT);
            close();
        }).dimensions(left + 124, y, 112, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.terranexus.add"), button -> {
            RoadMarkingEditorSession.setMode(RoadMarkingEditorSession.EditMode.ADD);
            close();
        }).dimensions(left + 248, y, 112, 20).build());

        y += 25;
        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.terranexus.remove_point"),
                button -> RoadMarkingEditorSession.removeSelectedPoint()).dimensions(left, y, 174, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.terranexus.save"), button -> {
            RoadMarkingEditorSession.save();
            close();
        }).dimensions(left + 186, y, 174, 20).build());

        y += 25;
        ButtonWidget delete = ButtonWidget.builder(Text.translatable("screen.terranexus.delete"), button -> {
            RoadMarkingEditorSession.delete();
            close();
        }).dimensions(left, y, 174, 20).build();
        delete.active = RoadMarkingEditorSession.persistedSelection();
        addDrawableChild(delete);
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), button -> close())
                .dimensions(left + 186, y, 174, 20).build());
    }

    private TextFieldWidget numberField(int x, int y, int fieldWidth, String key, double value, DoubleConsumer setter) {
        TextFieldWidget field = new TextFieldWidget(textRenderer, x, y, fieldWidth, 20, Text.translatable(key));
        field.setText(Double.isFinite(value) ? String.format(Locale.ROOT, "%.3f", value) : "");
        field.setTooltip(Tooltip.of(Text.translatable(key)));
        field.setChangedListener(text -> {
            try {
                setter.accept(text.isBlank() ? Double.NaN : Double.parseDouble(text));
            } catch (NumberFormatException ignored) {
            }
        });
        addDrawableChild(field);
        return field;
    }

    private void cycleWorkflow(ButtonWidget button) {
        var next = RoadMarkingEditorSession.workflowMode() == RoadMarkingEditorSession.WorkflowMode.PLACE
                ? RoadMarkingEditorSession.WorkflowMode.EDIT : RoadMarkingEditorSession.WorkflowMode.PLACE;
        if (RoadMarkingEditorSession.setWorkflowMode(next)) {
            button.setMessage(workflowText());
        } else {
            button.setTooltip(Tooltip.of(Text.translatable("message.terranexus.finish_current_marking")));
        }
    }

    private void cycleType(ButtonWidget button) {
        if (types.isEmpty()) return;
        typeIndex = (typeIndex + 1) % types.size();
        Identifier selected = types.get(typeIndex);
        RoadMarkingEditorSession.setType(selected);
        if (selected.equals(MarkingTypes.LANE_DIVIDER)) {
            MarkingStyle s = style();
            setStyle(new MarkingStyle(0.12, s.color(), s.material(), s.opacity(), s.wear(), s.dirt(),
                    3.0, 6.0, s.heightOffset(), s.lateralOffset(), s.cornerRadius(),
                    s.renderOrder(), s.collision()));
        }
        button.setMessage(typeText());
    }

    private void cycleOffset(ButtonWidget button) {
        offsetPresetIndex = (offsetPresetIndex + 1) % OFFSET_PRESETS.length;
        setOffset(OFFSET_PRESETS[offsetPresetIndex]);
        button.setMessage(offsetText());
    }

    private void cycleAxis(ButtonWidget button) {
        MarkingPlacementSettings.cycleAxisLock();
        button.setMessage(axisText());
    }

    private void cycleAngleSnap(ButtonWidget button) {
        MarkingPlacementSettings.cycleAngleSnap();
        button.setMessage(angleSnapText());
    }

    private void cycleGrid(ButtonWidget button) {
        MarkingPlacementSettings.cycleGrid();
        button.setMessage(gridText());
    }

    private void cycleColor() {
        int current = style().color();
        int next = current == 0xFFFFFFFF ? 0xFFFFD21F : current == 0xFFFFD21F ? 0xFF3399FF : 0xFFFFFFFF;
        MarkingStyle s = style();
        setStyle(new MarkingStyle(s.width(), next, s.material(), s.opacity(), s.wear(), s.dirt(),
                s.dashLength(), s.gapLength(), s.heightOffset(), s.lateralOffset(), s.cornerRadius(),
                s.renderOrder(), s.collision()));
    }

    private void cycleMaterial(ButtonWidget button) {
        MarkingStyle s = style();
        String material = switch (s.material()) {
            case "standard" -> "reflective";
            case "reflective" -> "temporary";
            default -> "standard";
        };
        setStyle(new MarkingStyle(s.width(), s.color(), material, s.opacity(), s.wear(), s.dirt(),
                s.dashLength(), s.gapLength(), s.heightOffset(), s.lateralOffset(), s.cornerRadius(),
                s.renderOrder(), s.collision()));
        button.setMessage(materialText());
    }

    private void setWidth(double value) { update(value, 0); }
    private void setDash(double value) { update(value, 1); }
    private void setGap(double value) { update(value, 2); }
    private void setOpacity(double value) { update(value, 3); }
    private void setWear(double value) { update(value, 4); }
    private void setDirt(double value) { update(value, 5); }
    private void setHeight(double value) { update(value, 6); }
    private void setOffset(double value) { update(value, 7); }
    private void setRadius(double value) { update(value, 8); }

    private void update(double value, int property) {
        if (!Double.isFinite(value)) return;
        MarkingStyle s = style();
        setStyle(new MarkingStyle(
                property == 0 ? value : s.width(), s.color(), s.material(),
                property == 3 ? (float) value : s.opacity(),
                property == 4 ? (float) value : s.wear(),
                property == 5 ? (float) value : s.dirt(),
                property == 1 ? value : s.dashLength(),
                property == 2 ? value : s.gapLength(),
                property == 6 ? value : s.heightOffset(),
                property == 7 ? value : s.lateralOffset(),
                property == 8 ? value : s.cornerRadius(),
                s.renderOrder(), s.collision()
        ));
    }

    private MarkingStyle style() { return RoadMarkingEditorSession.style(); }
    private void setStyle(MarkingStyle style) { RoadMarkingEditorSession.setStyle(style); }

    private Text typeText() {
        Identifier type = types.isEmpty() ? MarkingTypes.SOLID : types.get(typeIndex);
        return Text.translatable("marking_type." + type.getNamespace() + "." + type.getPath());
    }

    private Text workflowText() {
        return Text.translatable("screen.terranexus.mode."
                + RoadMarkingEditorSession.workflowMode().name().toLowerCase(Locale.ROOT));
    }

    private Text offsetText() {
        return Text.translatable("screen.terranexus.offset." + OFFSET_NAMES[offsetPresetIndex]);
    }

    private Text axisText() {
        return Text.translatable("screen.terranexus.axis."
                + MarkingPlacementSettings.axisLock().name().toLowerCase(Locale.ROOT));
    }

    private Text angleSnapText() {
        double value = MarkingPlacementSettings.angleSnapDegrees();
        return value == 0.0 ? Text.translatable("screen.terranexus.angle_snap.off")
                : Text.translatable("screen.terranexus.angle_snap", (int) value);
    }

    private Text gridText() {
        double value = MarkingPlacementSettings.gridSize();
        return value == 0.0 ? Text.translatable("screen.terranexus.grid.off")
                : Text.translatable("screen.terranexus.grid", value);
    }

    private Text materialText() {
        return Text.translatable("screen.terranexus.material." + style().material());
    }

    private static int nearestOffsetPreset(double value) {
        int best = 0;
        double distance = Double.MAX_VALUE;
        for (int i = 0; i < OFFSET_PRESETS.length; i++) {
            double candidate = Math.abs(value - OFFSET_PRESETS[i]);
            if (candidate < distance) {
                best = i;
                distance = candidate;
            }
        }
        return best;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Minecraft 1.21.8 already renders and blurs the background in
        // Screen.renderWithTooltip(). A second call crashes the client.
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - 190, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, RoadMarkingEditorSession.statusText(),
                width / 2, height / 2 + 166, 0xC8C8C8);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
