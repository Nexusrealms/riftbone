package de.nexusrealms.riftbone.mixin;

import de.nexusrealms.riftbone.GraveEntity;
import de.nexusrealms.riftbone.Riftbone;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }
    @Inject(method = "dropInventory", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;vanishCursedItems()V", shift = At.Shift.AFTER), cancellable = true)
    public void createGrave(CallbackInfo ci){
        GraveEntity graveEntity = new GraveEntity((PlayerEntity) (Object) this);
        if(!getEntityWorld().isClient()&&graveEntity.getY()<-64){
            if(getEntityWorld() instanceof ServerWorld){
                if(((ServerWorld) getEntityWorld()).getGameRules().getValue(Riftbone.VOID_GRAVES_WARP_UP) == true && getEntityWorld().getRegistryKey() == World.END){
                    graveEntity.setPos(graveEntity.getX(),64,graveEntity.getZ());
                }else{
                    graveEntity.setPos(graveEntity.getX(),-64,graveEntity.getZ());
                }
            }
        }
        graveEntity.setNoGravity(true);
        graveEntity.setVelocity(0,0,0);
        getEntityWorld().spawnEntity(graveEntity);
        ci.cancel();
    }
}
