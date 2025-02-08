package de.nexusrealms.riftbone.client;

import de.nexusrealms.riftbone.GraveEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.UUID;

public class LegacyGraveEntity extends Entity {
    public static final TrackedData<Optional<UUID>> OWNER = DataTracker.registerData(LegacyGraveEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
    public final SimpleInventory graveInventory = new SimpleInventory(54);

    public LegacyGraveEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    public void tick() {
        if(!getWorld().isClient()){
            GraveEntity grave = new GraveEntity(this);
            getWorld().spawnEntity(grave);
            discard();
        }
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(OWNER, Optional.empty());
    }

    protected void readCustomDataFromNbt(NbtCompound nbt) {
        NbtList graveItemList = nbt.getList("graveInventory", 10);
        graveInventory.readNbtList(graveItemList, getRegistryManager());
        if (nbt.contains("ownerUuid")) {
            dataTracker.set(OWNER, Optional.of(nbt.getUuid("ownerUuid")));
        }
    }

    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.put("graveInventory", graveInventory.toNbtList(getRegistryManager()));
        dataTracker.get(OWNER).ifPresent((uuid) -> nbt.putUuid("ownerUuid", uuid));
    }
}
