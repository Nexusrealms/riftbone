package de.nexusrealms.riftbone.mixin;

import de.nexusrealms.riftbone.Riftbone;
import net.minecraft.world.Container;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public class InventoryMixin {
    @Inject(method = "add(ILnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), remap = false)
    public void removeComponentOnInsert(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir){
        if(stack.has(Riftbone.SAVED_SLOT)) stack.remove(Riftbone.SAVED_SLOT);
//        if(stack.has(TrinketsCompat.SAVED_TRINKET_SLOT)) stack.remove(TrinketsCompat.SAVED_TRINKET_SLOT);
    }
    @Inject(method = "Lnet/minecraft/world/entity/player/Inventory;setItem(ILnet/minecraft/world/item/ItemStack;)V", at = @At("HEAD"), remap = false)
    public void removeComponentOnSet(int slot, ItemStack stack, CallbackInfo ci){
        if(stack.has(Riftbone.SAVED_SLOT)) stack.remove(Riftbone.SAVED_SLOT);
//        if(stack.has(TrinketsCompat.SAVED_TRINKET_SLOT)) stack.remove(TrinketsCompat.SAVED_TRINKET_SLOT);
    }
}
