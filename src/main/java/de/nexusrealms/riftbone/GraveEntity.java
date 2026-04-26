package de.nexusrealms.riftbone;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GraveEntity extends Entity {
    private static final EntityDataAccessor<Optional<UUID>> OWNER = SynchedEntityData.defineId(GraveEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    public GraveEntity(EntityType<?> type, Level world) {
        super(type, world);
    }
    public GraveEntity(Player entity) {
        super(Riftbone.GRAVE.get(), entity.level());
        entityData.set(OWNER, Optional.of(entity.getUUID()));
        setCustomName(Component.literal(entity.getName().getString() + "'s grave"));
        placeItemsInGrave(entity);
        copyPosition(entity);
    }
    private void addStack(Player player, ItemStack stack, int slot) {
        stack.set(Riftbone.SAVED_SLOT, slot);
        inventory.addItem(stack);
    }
    private void placeItemsInGrave(Player entity) {
        for (int i = 0; i < entity.getInventory().getContainerSize(); i++) {
            addStack(entity, entity.getInventory().getItem(i), i);
        }
    }
    private final SimpleContainer inventory = new SimpleContainer(54) {
        public ItemStack removeStack(int slot) {
            ItemStack stack = super.removeItemNoUpdate(slot);
            stack.remove(Riftbone.SAVED_SLOT);
            return stack;
        }

        @Override
        public @NotNull ItemStack removeItem(int slot, int amount) {
            ItemStack stack = super.removeItem(slot, amount);
            stack.remove(Riftbone.SAVED_SLOT);
            return stack;
        }
    };

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(OWNER, Optional.empty());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        HolderLookup.Provider registries = registryAccess();
        inventory.fromTag(nbt.getList("inventory", Tag.TAG_COMPOUND), registries);
        if(nbt.contains("owner")){
            entityData.set(OWNER, Optional.of(nbt.getUUID("owner")));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        HolderLookup.Provider registries = registryAccess();
        nbt.put("inventory", inventory.createTag(registries));
        if (entityData.get(OWNER).isPresent()) {
            nbt.putUUID("owner", entityData.get(OWNER).get());
        }
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if(!level().isClientSide()){
            if(!level().getGameRules().getBoolean(Riftbone.OWNER_ONLY_LOOTING) || isOwner(player.getUUID())){
                if(player.isShiftKeyDown()){
                    quickLoot(player);
                    return InteractionResult.SUCCESS;
                } else {
                    level().playSound(null, getOnPos(), SoundEvents.BARREL_OPEN, SoundSource.BLOCKS, 1f, 1f);
                    player.openMenu(new GraveEntity.GraveScreenHandlerFactory(this));
                    return InteractionResult.SUCCESS;
                }
            }
            this.level().playSound(null, getOnPos(), SoundEvents.WOOD_HIT, SoundSource.BLOCKS, 1f, 1f);
            return InteractionResult.FAIL;
        }

        return InteractionResult.PASS;
    }
    public void tick() {
        super.tick();
        this.xOld = this.getX();
        this.yOld = this.getY();
        this.zOld = this.getZ();
        Vec3 vec3d = this.getDeltaMovement();
        float f = this.getEyeHeight() - 0.11111111F;
        if (this.isInWater() && this.getFluidHeight(FluidTags.WATER) > (double) f) {
            this.applyWaterBuoyancy();
        } else if (this.isInLava() && this.getFluidHeight(FluidTags.LAVA) > (double) f) {
            this.applyLavaBuoyancy();
        } else if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04, 0.0));
        }
        if (!this.level().isClientSide() && this.tickCount % 100 == 0 && inventory.isEmpty()) {
            level().playSound(null, getOnPos(), SoundEvents.ENDER_EYE_DEATH, SoundSource.BLOCKS, 1f, 1f);
            this.kill();
        }
        /*if (this.level().isClientSide()) {
            this.noPhysics = false;
        } else {
            this.noPhysics = !this.level().noCollision(this, this.getBoundingBox().inflate(1.0E-7));
            if (this.noPhysics) {
                this.moveTowardsClosestSpace(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0, this.getZ());
            }
        }*/

        if (!this.onGround() || this.getDeltaMovement().horizontalDistanceSqr() > 9.999999747378752E-6 || (this.tickCount + this.getId()) % 4 == 0) {
            this.move(MoverType.SELF, this.getDeltaMovement());
            float g = 0.98F;
            if (this.onGround()) {
                g = this.level().getBlockState(new BlockPos(this.getBlockX(), this.getBlockY()- 1, this.getBlockZ())).getBlock().getFriction() * 0.98F;
            }
            if (getY() <= -64) {
                setPos(getX(), -64, getZ());
            }
            this.setDeltaMovement(this.getDeltaMovement().multiply((double) g, 0.98, (double) g));
            if (this.onGround()) {
                Vec3 vec3d2 = this.getDeltaMovement();
                if (vec3d2.y < 0.0) {
                    this.setDeltaMovement(vec3d2.multiply(1.0, -0.5, 1.0));
                }
            }
        }

        boolean bl = Mth.floor(this.xOld) != Mth.floor(this.getX()) || Mth.floor(this.yOld) != Mth.floor(this.getY()) || Mth.floor(this.zOld) != Mth.floor(this.getZ());
        int i = bl ? 2 : 40;

        this.hasImpulse |= this.updateInWaterStateAndDoFluidPushing();
        if (!this.level().isClientSide()) {
            double d = this.getDeltaMovement().subtract(vec3d).lengthSqr();
            if (d > 0.01) {
                this.hasImpulse = true;
            }
        }


    }
    private boolean isOwner(UUID uuid){
        if(entityData.get(OWNER).isEmpty()) return false;
        UUID uuid1 = entityData.get(OWNER).get();
        return uuid1.equals(uuid);
    }
    private void quickLoot(Player player){
        if(level().getGameRules().getBoolean(Riftbone.QUICK_LOOTING_ALLOWED) && (!level().getGameRules().getBoolean(Riftbone.OWNER_ONLY_QUICK_LOOTING) || isOwner(player.getUUID()))){
            List<ItemStack> unslotted = new ArrayList<>();
            Inventory playerInventory = player.getInventory();
            inventory.getItems().forEach(stack -> {
                if(stack.has(Riftbone.SAVED_SLOT)){
                    int slot = stack.get(Riftbone.SAVED_SLOT);
                    stack.remove(Riftbone.SAVED_SLOT);
                    if(playerInventory.getItem(slot).isEmpty() || ItemEntity.areMergable(stack, playerInventory.getItem(slot))){
                        playerInventory.add(slot, stack);
                    } else {
                        unslotted.add(stack);
                    }
                } else {
                    unslotted.add(stack);
                }
            });
            unslotted.forEach(stack -> {
                playerInventory.placeItemBackInInventory(stack, false);
            });
            this.kill();
            level().playSound(null, getOnPos(), SoundEvents.ENDER_EYE_DEATH, SoundSource.BLOCKS, 1f, 1f);
        } else {
            this.level().playSound(null, getOnPos(), SoundEvents.WOOD_HIT, SoundSource.BLOCKS, 1f, 1f);
        }
    }
    public boolean shouldRenderName() {
        return true;
    }
    protected Entity.MovementEmission getMoveEffect() {
        return MovementEmission.NONE;
    }
    private void applyWaterBuoyancy() {
        Vec3 vec3d = this.getDeltaMovement();
        this.setDeltaMovement(vec3d.x * 0.9900000095367432, vec3d.y + (double) (vec3d.y < 0.05999999865889549 ? 5.0E-4F : 0.0F), vec3d.z * 0.9900000095367432);
    }

    private void applyLavaBuoyancy() {
        Vec3 vec3d = this.getDeltaMovement();
        this.setDeltaMovement(vec3d.x * 0.949999988079071, vec3d.y + (double) (vec3d.y < 0.05999999865889549 ? 5.0E-4F : 0.0F), vec3d.z * 0.949999988079071);
    }
    private static class GraveScreenHandlerFactory implements MenuProvider {
        private final GraveEntity entity;

        private GraveScreenHandlerFactory(GraveEntity entity) {
            this.entity = entity;
        }

        public Component getDisplayName() {
            return this.entity.getDisplayName();
        }

        public @NotNull ChestMenu createMenu(int syncId, Inventory inv, Player player) {
            return ChestMenu.sixRows(syncId, inv, this.entity.inventory);
        }

    }
}
