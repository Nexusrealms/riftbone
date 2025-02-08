package de.nexusrealms.riftbone.client;

import de.nexusrealms.riftbone.GraveEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.SkullBlockEntityModel;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SkullEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class GraveEntityRenderer extends EntityRenderer<Entity> {
    private final SkullBlockEntityModel skullBlockEntityModel;
    private final Identifier texture = Identifier.ofVanilla("textures/entity/skeleton/skeleton.png");
    public GraveEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        skullBlockEntityModel = new SkullEntityModel(ctx.getModelLoader().getModelPart(EntityModelLayers.SKELETON_SKULL));
    }

    @Override
    public Identifier getTexture(Entity entity) {
        return texture;
    }

    @Override
    public void render(Entity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCullZOffset(texture));
        skullBlockEntityModel.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV);
        matrices.pop();
    }
}
