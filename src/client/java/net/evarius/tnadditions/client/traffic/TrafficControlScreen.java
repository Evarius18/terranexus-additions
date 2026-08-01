package net.evarius.tnadditions.client.traffic;

import net.evarius.tnadditions.traffic.TrafficControlPayloads;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/** Compact management UI shared by the traffic-control PC and wall screen. */
public final class TrafficControlScreen extends Screen {
    private final List<Device> devices = new ArrayList<>();
    private int selected;
    private int page;
    private TextFieldWidget nameField, groupField, areaField, intersectionField, valueField, textField, scheduleField;

    public TrafficControlScreen(NbtCompound snapshot) {
        super(Text.translatable("screen.terranexus.traffic_control"));
        for (var element : snapshot.getListOrEmpty("devices")) {
            if (element instanceof NbtCompound value) devices.add(Device.read(value));
        }
    }

    @Override protected void init() {
        int left = width / 2 - 210;
        int top = height / 2 - 150;
        int first = page * 8;
        for (int row = 0; row < 8 && first + row < devices.size(); row++) {
            int index = first + row;
            Device device = devices.get(index);
            ButtonWidget button = ButtonWidget.builder(Text.literal(device.label()), ignored -> {
                selected = index;
                clearAndInit();
            }).dimensions(left, top + 24 + row * 23, 195, 20).build();
            button.active = index != selected;
            addDrawableChild(button);
        }
        addDrawableChild(ButtonWidget.builder(Text.literal("<"), ignored -> { page = Math.max(0, page - 1); clearAndInit(); })
                .dimensions(left, top + 212, 45, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal(">"), ignored -> { page = Math.min(maxPage(), page + 1); clearAndInit(); })
                .dimensions(left + 150, top + 212, 45, 20).build());
        if (devices.isEmpty()) return;
        selected = Math.max(0, Math.min(selected, devices.size() - 1));
        Device d = devices.get(selected);
        int x = left + 210;
        nameField = field(x, top + 24, 205, d.name, "screen.terranexus.device_name");
        groupField = field(x, top + 48, 100, d.group, "screen.terranexus.device_group");
        areaField = field(x + 105, top + 48, 100, d.area, "screen.terranexus.device_area");
        intersectionField = field(x, top + 72, 205, d.intersection, "screen.terranexus.intersection");
        valueField = field(x, top + 96, 60, Integer.toString(d.value), "screen.terranexus.display_value");
        textField = field(x + 65, top + 96, 140, d.text, "screen.terranexus.display_text");
        scheduleField = field(x, top + 120, 205, d.scheduleStart + "-" + d.scheduleEnd,
                "screen.terranexus.schedule");
        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.terranexus.save"), ignored -> save())
                .dimensions(x, top + 148, 100, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.terranexus.enabled", d.enabled),
                ignored -> action("toggle_enabled")).dimensions(x + 105, top + 148, 100, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.terranexus.display_mode", d.mode),
                ignored -> action("cycle_mode")).dimensions(x, top + 172, 100, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.terranexus.signal_aspect", d.aspect),
                ignored -> action("cycle_aspect")).dimensions(x + 105, top + 172, 100, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.terranexus.automatic", !d.manual),
                ignored -> action("toggle_automatic")).dimensions(x, top + 196, 100, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.terranexus.apply_group"),
                ignored -> action("apply_group")).dimensions(x + 105, top + 196, 100, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), ignored -> close())
                .dimensions(x, top + 238, 205, 20).build());
    }

    private TextFieldWidget field(int x, int y, int width, String value, String key) {
        TextFieldWidget field = new TextFieldWidget(textRenderer, x, y, width, 20, Text.translatable(key));
        field.setPlaceholder(Text.translatable(key));
        field.setText(value);
        addDrawableChild(field);
        return field;
    }
    private void save() {
        NbtCompound action = base("configure");
        action.putString("name", nameField.getText());
        action.putString("group", groupField.getText());
        action.putString("area", areaField.getText());
        action.putString("intersection", intersectionField.getText());
        action.putString("text", textField.getText());
        try { action.putInt("value", Integer.parseInt(valueField.getText())); } catch (NumberFormatException ignored) {}
        String[] schedule = scheduleField.getText().split("-", 2);
        try {
            action.putInt("schedule_start", schedule.length > 0 ? Integer.parseInt(schedule[0].trim()) : -1);
            action.putInt("schedule_end", schedule.length > 1 ? Integer.parseInt(schedule[1].trim()) : -1);
        } catch (NumberFormatException ignored) { action.putInt("schedule_start", -1); action.putInt("schedule_end", -1); }
        send(action);
    }
    private void action(String actionName) { send(base(actionName)); }
    private NbtCompound base(String actionName) {
        NbtCompound data = new NbtCompound();
        data.putString("key", devices.get(selected).key);
        data.putString("action", actionName);
        return data;
    }
    private void send(NbtCompound data) {
        if (ClientPlayNetworking.canSend(TrafficControlPayloads.Action.ID))
            ClientPlayNetworking.send(new TrafficControlPayloads.Action(data));
    }
    private int maxPage() { return Math.max(0, (devices.size() - 1) / 8); }

    @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int left = width / 2 - 210;
        int top = height / 2 - 150;
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, top + 5, 0xFFFFFF);
        if (devices.isEmpty()) context.drawCenteredTextWithShadow(textRenderer,
                Text.translatable("screen.terranexus.no_devices"), width / 2, height / 2, 0xAAAAAA);
    }

    private record Device(String key, String name, String type, String group, String area, String intersection,
                          String mode, int value, String text, boolean enabled, boolean manual, String aspect,
                          int scheduleStart, int scheduleEnd, String position) {
        static Device read(NbtCompound nbt) {
            return new Device(nbt.getString("key", ""), nbt.getString("name", ""), nbt.getString("type", ""),
                    nbt.getString("group", "default"), nbt.getString("area", ""),
                    nbt.getString("intersection", ""), nbt.getString("mode", "off"), nbt.getInt("value", 0),
                    nbt.getString("text", ""), nbt.getBoolean("enabled", true), nbt.getBoolean("manual", false),
                    nbt.getString("aspect", "red"), nbt.getInt("schedule_start", -1),
                    nbt.getInt("schedule_end", -1), nbt.getString("position", ""));
        }
        String label() { return name + " · " + type + " · " + position; }
    }
}
