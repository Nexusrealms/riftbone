package de.nexusrealms.riftbone.mixin;

import com.mojang.authlib.GameProfile;
import de.nexusrealms.riftbone.SoulboundHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {

    public ServerPlayerMixin(Level world, GameProfile profile) {
        super(world, profile);
    }

    @Shadow public abstract boolean canHarmPlayer(Player player);


    @Shadow public abstract ServerLevel level();

    @Inject(method = "restoreFrom", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/server/level/ServerPlayerGameMode;setGameModeForPlayer(Lnet/minecraft/world/level/GameType;Lnet/minecraft/world/level/GameType;)V"))
    public void copySoulbounds(ServerPlayer oldPlayer, boolean alive, CallbackInfo ci){
        if (!(this.level().getGameRules().get(GameRules.KEEP_INVENTORY) || oldPlayer.isSpectator()) && !alive) {
            SoulboundHandler.transferSoulbounds(oldPlayer, (ServerPlayer) (Object) this);
        }
    }
}
