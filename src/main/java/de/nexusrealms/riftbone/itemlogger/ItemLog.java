package de.nexusrealms.riftbone.itemlogger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.nexusrealms.riftbone.Riftbone;
import de.nexusrealms.riftbone.TrinketsCompat;
import de.nexusrealms.riftbone.mixin.EquipmentAccessor;
import de.nexusrealms.riftbone.mixin.EquipmentItemsAccessor;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.*;

import java.time.Instant;
import java.util.*;

public record ItemLog(ListTag items, Date date, UUID player, Map<EquipmentSlot, ItemStack> equipment, List<ItemStack> trinkets) {
    private static final Codec<ListTag> LIST_TAG_CODEC = Codec.PASSTHROUGH.comapFlatMap(
            (dynamic) -> {
                Tag tag = dynamic.convert(NbtOps.INSTANCE).getValue();
                if (tag instanceof ListTag listTag) {
                    return DataResult.success(listTag == dynamic.getValue() ? listTag.copy() : listTag);
                } else {
                    return DataResult.error(() -> "Not a list tag: " + tag);
                }
            }, (compoundTag) -> new Dynamic<>(NbtOps.INSTANCE, compoundTag.copy())
    );
    private static final Codec<Date> DATE_CODEC = Codec.LONG.xmap(Date::new, Date::getTime);
    public static final Codec<ItemLog> CODEC = RecordCodecBuilder.create(itemLogInstance -> itemLogInstance.group(
            LIST_TAG_CODEC.fieldOf("items").forGetter(ItemLog::items),
            DATE_CODEC.fieldOf("date").forGetter(ItemLog::date),
            UUIDUtil.CODEC.fieldOf("player").forGetter(ItemLog::player),
            Codec.unboundedMap(EquipmentSlot.CODEC, ItemStack.CODEC).fieldOf("equipment").forGetter(ItemLog::equipment),
            ItemStack.CODEC.listOf().fieldOf("trinkets").forGetter(ItemLog::trinkets)
    ).apply(itemLogInstance, ItemLog::new));
    public static ItemLog createFromPlayer(ServerPlayer player){
        Date date = Date.from(Instant.now());
        ProblemReporter.ScopedCollector rp = new ProblemReporter.ScopedCollector(Riftbone.LOGGER);
        ListTag list = new ListTag();
        ValueOutput.TypedOutputList<ItemStackWithSlot> tol = new TagValueOutput.TypedListWrapper<>(rp, "item log", player.registryAccess().createSerializationContext(NbtOps.INSTANCE), ItemStackWithSlot.CODEC, list);
        player.getInventory().save(tol);
        rp.close();
        Map<EquipmentSlot, ItemStack> map = new HashMap<>(5);
        ((EquipmentItemsAccessor) ((EquipmentAccessor) player.getInventory()).getEquipment()).getItems().forEach((equipmentSlot, stack) -> {
            if(!stack.isEmpty()){
                map.put(equipmentSlot, stack.copy());
            }
        });

        return new ItemLog(list, date, player.getUUID(), map, TrinketsCompat.collectTrinketsForItemLog(player));
    }
    public ValueInput.TypedInputList<ItemStackWithSlot> createInputList(HolderLookup.Provider registries){
        ProblemReporter.ScopedCollector rp = new ProblemReporter.ScopedCollector(Riftbone.LOGGER);
        return new TagValueInput.TypedListWrapper<>(rp, "item log", new ValueInputContextHelper(registries, NbtOps.INSTANCE), ItemStackWithSlot.CODEC, items);
    }

}
