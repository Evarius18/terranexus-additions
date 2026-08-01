package net.evarius.tnadditions.client.traffic;

import net.evarius.tnadditions.traffic.TrafficControlPayloads;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class TrafficControlClient {
    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(TrafficControlPayloads.Open.ID, (payload, context) ->
                context.client().execute(() -> context.client().setScreen(new TrafficControlScreen(payload.data()))));
    }
    private TrafficControlClient() {}
}
