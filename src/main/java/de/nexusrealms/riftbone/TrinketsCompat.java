package de.nexusrealms.riftbone;

import com.mojang.serialization.Codec;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;

public class TrinketsCompat {

    public static final ComponentType<String> SAVED_TRINKET_SLOT = ComponentType.<String>builder().codec(Codec.STRING).build();
    public static void init(){
        if(Riftbone.isTrinketsLoaded){
            Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(Riftbone.MOD_ID, "saved_trinket_slot"), SAVED_TRINKET_SLOT);
        }
    }
    public static void onGraveSpawn(PlayerEntity player){
        if(Riftbone.isTrinketsLoaded){
            TrinketsApi.getTrinketComponent(player).ifPresent((trinkets) -> trinkets.forEach((ref, stack) -> ref.inventory().setStack(ref.index(), ItemStack.EMPTY)));
        }
    }
    public static void addTrinketsToGrave(SimpleInventory graveInventory, PlayerEntity player){
        if(Riftbone.isTrinketsLoaded){
            TrinketsApi.getTrinketComponent(player).ifPresent(
                    trinkets -> trinkets.getAllEquipped().forEach(
                            slotReferenceItemStackPair -> {
                                slotReferenceItemStackPair.getRight().set(SAVED_TRINKET_SLOT, slotReferenceItemStackPair.getLeft().getId());
                                graveInventory.addStack(slotReferenceItemStackPair.getRight());
                            }));
        }
    }
    public static boolean handleQuickLoot(ItemStack stack, List<ItemStack> unslotted, PlayerEntity player){
        if(Riftbone.isTrinketsLoaded){
            if(stack.contains(SAVED_TRINKET_SLOT)){
                String savedSlotId = stack.get(SAVED_TRINKET_SLOT);
                stack.remove(SAVED_TRINKET_SLOT);
                Optional<TrinketComponent> trinketComponent = TrinketsApi.getTrinketComponent(player);
                if(trinketComponent.isPresent()){
                    String[] strings = savedSlotId.split("/");
                    TrinketComponent component = trinketComponent.get();
                    try {
                        TrinketInventory inventory = component.getInventory().get(strings[0]).get(strings[1]);
                        int slot = Integer.parseInt(strings[2]);
                        if(inventory.getStack(slot).isEmpty()){
                            inventory.setStack(slot, stack);
                        } else {
                            unslotted.add(stack);
                        }
                        return true;
                    } catch (NullPointerException e){
                        unslotted.add(stack);
                        return false;
                    }
                }
                return false;
            }
        }
        return false;
    }

}
