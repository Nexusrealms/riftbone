package de.nexusrealms.riftbone.mixin;

import de.nexusrealms.riftbone.Riftbone;
import de.nexusrealms.riftbone.TrinketsCompat;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin {
    @Inject(method = "insertStack(ILnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"))
    public void removeComponentOnInsert(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir){
        if(stack.contains(Riftbone.SAVED_SLOT)) stack.remove(Riftbone.SAVED_SLOT);
        if(stack.contains(TrinketsCompat.SAVED_TRINKET_SLOT)) stack.remove(TrinketsCompat.SAVED_TRINKET_SLOT);
    }
    @Inject(method = "setStack", at = @At("HEAD"))
    public void removeComponentOnSet(int slot, ItemStack stack, CallbackInfo ci){
        if(stack.contains(Riftbone.SAVED_SLOT)) stack.remove(Riftbone.SAVED_SLOT);
        if(stack.contains(TrinketsCompat.SAVED_TRINKET_SLOT)) stack.remove(TrinketsCompat.SAVED_TRINKET_SLOT);
    }
}
