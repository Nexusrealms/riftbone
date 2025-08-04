package de.nexusrealms.riftbone.client;

import de.nexusrealms.riftbone.Riftbone;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class RiftboneClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(Riftbone.GRAVE, GraveEntityRenderer::new);
    }
}
