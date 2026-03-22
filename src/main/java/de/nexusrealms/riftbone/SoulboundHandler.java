package de.nexusrealms.riftbone;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class SoulboundHandler {
    public static boolean isSoulbound(ItemStack stack, Player player){
        return SoulboundCallback.IS_SOULBOUND.invoker().isSoulbound(player, stack);
    }
    public static void transferSoulbounds(ServerPlayer oldPlayer, ServerPlayer newPlayer){
        List<ItemStack> stacks = getMatching(oldPlayer.getInventory(), stack -> isSoulbound(stack, oldPlayer), -1);
        stacks.forEach(stack -> newPlayer.getInventory().add(stack));
    }
    public static List<ItemStack> getMatching(Container inventory, Predicate<ItemStack> shouldRemove, int maxCount){
        int i = 0;
        List<ItemStack> stacks = new ArrayList<>();
        for(int j = 0; j < inventory.getContainerSize(); ++j) {
            ItemStack itemStack = inventory.getItem(j);
            int k = ContainerHelper.clearOrCountMatchingItems(itemStack, shouldRemove, maxCount - i, true);
            if (k > 0) {
                stacks.add(itemStack.copy());
            }
            i += k;
        }
        return stacks;
    }}
