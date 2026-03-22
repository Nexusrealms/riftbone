package de.nexusrealms.riftbone.client;

import de.nexusrealms.riftbone.Riftbone;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.entity.EntityRenderers;

public class RiftboneClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRenderers.register(Riftbone.GRAVE, GraveEntityRenderer::new);
    }
}
