package de.nexusrealms.riftbone.client;

import de.nexusrealms.riftbone.Riftbone;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class RiftboneClient  {
    @SubscribeEvent // on the mod event bus only on the physical client
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(Riftbone.GRAVE.get(), GraveEntityRenderer::new);
    }
}
