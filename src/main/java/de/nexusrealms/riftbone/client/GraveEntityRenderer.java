package de.nexusrealms.riftbone.client;

import de.nexusrealms.riftbone.GraveEntity;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.RenderType;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.SkullModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;
import com.mojang.math.Axis;

public class GraveEntityRenderer extends EntityRenderer<Entity> {
    private final SkullModelBase skullBlockEntityModel;
    private final ResourceLocation texture = ResourceLocation.withDefaultNamespace("textures/entity/skeleton/skeleton.png");
    public GraveEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        skullBlockEntityModel = new SkullModel(ctx.getModelSet().bakeLayer(ModelLayers.SKELETON_SKULL));
    }

    @Override
    public ResourceLocation getTextureLocation(Entity entity) {
        return texture;
    }

    @Override
    public void render(Entity entity, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light) {
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
        matrices.pushPose();
        matrices.mulPose(Axis.XP.rotationDegrees(180));
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderType.entityCutoutNoCullZOffset(texture));
        skullBlockEntityModel.renderToBuffer(matrices, vertexConsumer, light, OverlayTexture.NO_OVERLAY);
        matrices.popPose();
    }
}
