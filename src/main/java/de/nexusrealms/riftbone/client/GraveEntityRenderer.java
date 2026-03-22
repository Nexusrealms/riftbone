package de.nexusrealms.riftbone.client;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.skull.SkullModel;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import com.mojang.math.Axis;
import org.jetbrains.annotations.Nullable;

public class GraveEntityRenderer extends EntityRenderer<Entity, EntityRenderState> {
    private final SkullModelBase skullBlockEntityModel;
    private final Identifier texture = Identifier.withDefaultNamespace("textures/entity/skeleton/skeleton.png");
    private final RenderType layer = SkullBlockRenderer.getSkullRenderType(SkullBlock.Types.SKELETON, null);
    public GraveEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        skullBlockEntityModel = new SkullModel(ctx.getModelSet().bakeLayer(ModelLayers.SKELETON_SKULL));
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }


    @Override
    public void submit(EntityRenderState renderState, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        super.submit(renderState, matrices, queue, cameraState);
        matrices.pushPose();
        float g = Mth.sin(renderState.ageInTicks / 10.0F) * 0.1F + 0.1F;
        matrices.translate(0.0F, g, 0.0F);
        float h = ItemEntity.getSpin(renderState.ageInTicks, 0);
        matrices.mulPose(Axis.YP.rotation(h));
        matrices.mulPose(Axis.XP.rotationDegrees(180));
        SkullModelBase.State skullModelState = new SkullModelBase.State();
        queue.submitModel(skullBlockEntityModel, skullModelState, matrices, layer, renderState.lightCoords, OverlayTexture.NO_OVERLAY, 0, null);
        matrices.popPose();    }
}
