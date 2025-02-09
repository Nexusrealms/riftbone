package de.nexusrealms.riftbone;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class SoulboundHandler {
    public static boolean isSoulbound(ItemStack stack, PlayerEntity player){
        return SoulboundCallback.IS_SOULBOUND.invoker().isSoulbound(player, stack);
    }
    public static void transferSoulbounds(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer){
        List<ItemStack> stacks = getMatching(oldPlayer.getInventory(), stack -> isSoulbound(stack, oldPlayer), -1);
        stacks.forEach(stack -> newPlayer.getInventory().insertStack(stack));
    }
    public static List<ItemStack> getMatching(Inventory inventory, Predicate<ItemStack> shouldRemove, int maxCount){
        int i = 0;
        List<ItemStack> stacks = new ArrayList<>();
        for(int j = 0; j < inventory.size(); ++j) {
            ItemStack itemStack = inventory.getStack(j);
            int k = Inventories.remove(itemStack, shouldRemove, maxCount - i, true);
            if (k > 0) {
                stacks.add(itemStack.copy());
            }
            i += k;
        }
        return stacks;
    }}
