//package de.nexusrealms.riftbone;
//
//import com.mojang.serialization.Codec;
//import dev.emi.trinkets.api.*;
//import dev.emi.trinkets.api.event.TrinketDropCallback;
//import net.minecraft.core.component.DataComponentType;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.entity.player.Inventory;
//import net.minecraft.world.SimpleContainer;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.core.registries.BuiltInRegistries;
//import net.minecraft.core.Registry;
//import net.minecraft.resources.Identifier;
//
//import java.util.List;
//import java.util.Optional;
//
//public class TrinketsCompat {
//
//    public static final DataComponentType<String> SAVED_TRINKET_SLOT = DataComponentType.<String>builder().persistent(Codec.STRING).build();
//    public static void init(){
//        if(Riftbone.isTrinketsLoaded){
//            Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Identifier.fromNamespaceAndPath(Riftbone.MOD_ID, "saved_trinket_slot"), SAVED_TRINKET_SLOT);
//            TrinketDropCallback.EVENT.register((rule, stack, ref, entity) -> TrinketEnums.DropRule.KEEP);
//        }
//    }
//    public static void onGraveSpawn(Player player){
//        if(Riftbone.isTrinketsLoaded){
//            TrinketsApi.getTrinketComponent(player).ifPresent((trinkets) -> trinkets.forEach((ref, stack) -> ref.inventory().setItem(ref.index(), ItemStack.EMPTY)));
//        }
//    }
//    public static void addTrinketsToGrave(SimpleContainer graveInventory, Player player){
//        if(Riftbone.isTrinketsLoaded){
//            TrinketsApi.getTrinketComponent(player).ifPresent(
//                    trinkets -> trinkets.getAllEquipped().forEach(
//                            slotReferenceItemStackPair -> {
//                                slotReferenceItemStackPair.getB().set(SAVED_TRINKET_SLOT, slotReferenceItemStackPair.getA().getId());
//                                graveInventory.addItem(slotReferenceItemStackPair.getB());
//                            }));
//        }
//    }
//    public static boolean handleQuickLoot(ItemStack stack, List<ItemStack> unslotted, Player player){
//        if(Riftbone.isTrinketsLoaded){
//            if(stack.has(SAVED_TRINKET_SLOT)){
//                String savedSlotId = stack.get(SAVED_TRINKET_SLOT);
//                stack.remove(SAVED_TRINKET_SLOT);
//                Optional<TrinketComponent> trinketComponent = TrinketsApi.getTrinketComponent(player);
//                if(trinketComponent.isPresent()){
//                    String[] strings = savedSlotId.split("/");
//                    TrinketComponent component = trinketComponent.get();
//                    try {
//                        TrinketInventory inventory = component.getInventory().get(strings[0]).get(strings[1]);
//                        int slot = Integer.parseInt(strings[2]);
//                        if(inventory.getItem(slot).isEmpty()){
//                            inventory.setItem(slot, stack);
//                        } else {
//                            unslotted.add(stack);
//                        }
//                        return true;
//                    } catch (NullPointerException e){
//                        unslotted.add(stack);
//                        return false;
//                    }
//                }
//                return false;
//            }
//        }
//        return false;
//    }
//
//}
