package de.nexusrealms.riftbone;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public interface SoulboundCallback {

    Event<SoulboundCallback> IS_SOULBOUND = EventFactory.createArrayBacked(SoulboundCallback.class, (soulboundCallbacks ->
            (player, newplayer) -> {
                for(SoulboundCallback callback : soulboundCallbacks){
                    if(callback.isSoulbound(player, newplayer)) return true;
                }
                return false;
            }));

    boolean isSoulbound(PlayerEntity oldPlayer, ItemStack stack);

}
