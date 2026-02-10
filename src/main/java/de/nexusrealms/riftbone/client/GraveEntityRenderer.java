package de.nexusrealms.riftbone.client;

import de.nexusrealms.riftbone.GraveEntity;
import net.minecraft.block.SkullBlock;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.SkullBlockEntityModel;
import net.minecraft.client.render.block.entity.SkullBlockEntityRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SkullEntityModel;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;

public class GraveEntityRenderer extends EntityRenderer<Entity, EntityRenderState> {
    private final SkullBlockEntityModel skullBlockEntityModel;
    private final Identifier texture = Identifier.ofVanilla("textures/entity/skeleton/skeleton.png");
    private final RenderLayer layer = SkullBlockEntityRenderer.getCutoutRenderLayer(SkullBlock.Type.SKELETON, null);
    public GraveEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        skullBlockEntityModel = new SkullEntityModel(ctx.getEntityModels().getModelPart(EntityModelLayers.SKELETON_SKULL));
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }


    @Override
    public void render(EntityRenderState renderState, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        super.render(renderState, matrices, queue, cameraState);
        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
        SkullBlockEntityModel.SkullModelState skullModelState = new SkullBlockEntityModel.SkullModelState();
        queue.submitModel(skullBlockEntityModel, skullModelState, matrices, layer, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, 0, null);
        matrices.pop();    }
}
