package de.nexusrealms.riftbone;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.MovementEmission;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GraveEntity extends Entity {
    protected static final EntityDataAccessor<Optional<EntityReference<LivingEntity>>> OWNER = SynchedEntityData.defineId(GraveEntity.class, EntityDataSerializers.OPTIONAL_LIVING_ENTITY_REFERENCE);

    public GraveEntity(EntityType<?> type, Level world) {
        super(type, world);
    }
    public GraveEntity(Player entity) {
        super(Riftbone.GRAVE, entity.level());
        entityData.set(OWNER, Optional.of(EntityReference.of(entity)));
        if (entity.level() instanceof ServerLevel && ((ServerLevel) entity.level()).getGameRules().get(Riftbone.ENABLE_GRAVE_SUFFIX)) {
            setCustomName(Component.literal(entity.getName().getString() + "'s grave"));
        } else {
            setCustomName(Component.literal(entity.getName().getString()));
        }
        placeItemsInGrave(entity);
        copyPosition(entity);
        TrinketsCompat.onGraveSpawn(entity);
    }

    private void addStack(Player player, ItemStack stack, int slot) {
        if (!SoulboundHandler.isSoulbound(stack, player)) {
            stack.set(Riftbone.SAVED_SLOT, slot);
            inventory.addItem(stack);
        }
    }
    private void placeItemsInGrave(Player entity) {
        for (int i = 0; i < entity.getInventory().getContainerSize(); i++) {
            addStack(entity, entity.getInventory().getItem(i), i);
        }
        TrinketsCompat.addTrinketsToGrave(inventory, entity);
    }
    private final SimpleContainer inventory = new SimpleContainer(54) {
        public ItemStack removeItemNoUpdate(int slot) {
            ItemStack stack = super.removeItemNoUpdate(slot);
            stack.remove(Riftbone.SAVED_SLOT);
            return stack;
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            ItemStack stack = super.removeItemNoUpdate(slot);
            stack.remove(Riftbone.SAVED_SLOT);
            return stack;
        }
    };

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(OWNER, Optional.empty());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput readView) {
        //Automatic data migration
        readView.read("owner", UUIDUtil.CODEC).ifPresent(uuid1 -> entityData.set(OWNER, Optional.of(EntityReference.of(uuid))));
        ValueInput.TypedInputList<ItemStack> list = readView.listOrEmpty("inventory", ItemStack.CODEC);
        if (!list.isEmpty()) {
            inventory.fromItemList(list);
        }

        for (ItemStackWithSlot stackWithSlot : readView.listOrEmpty("contents", ItemStackWithSlot.CODEC)) {
            if (stackWithSlot.isValidInContainer(inventory.getContainerSize())) {
                inventory.setItem(stackWithSlot.slot(), stackWithSlot.stack());
            }
        }
        EntityReference<LivingEntity> lazyEntityReference = EntityReference.readWithOldOwnerConversion(readView, "Owner", this.level());
        if (lazyEntityReference != null) {
            this.entityData.set(OWNER, Optional.of(lazyEntityReference));
        } else {
            this.entityData.set(OWNER, Optional.empty());
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput writeView) {
        ValueOutput.TypedOutputList<ItemStackWithSlot> listAppender = writeView.list("contents", ItemStackWithSlot.CODEC);
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            ItemStack itemStack = inventory.getItem(i);
            if (!itemStack.isEmpty()) {
                listAppender.add(new ItemStackWithSlot(i, itemStack));
            }
        }
        EntityReference<LivingEntity> lazyEntityReference = this.getOwnerNullable();
        EntityReference.store(lazyEntityReference, writeView, "Owner");
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (level() instanceof ServerLevel world) {
            if (!world.getGameRules().get(Riftbone.OWNER_ONLY_LOOTING) || isOwner(player.getUUID())) {
                if (player.isShiftKeyDown()) {
                    quickLoot(player);
                    return InteractionResult.SUCCESS;
                } else {
                    if (world.getGameRules().get(Riftbone.ENABLE_GRAVE_OPEN_SOUND)) {
                        level().playSound(null, blockPosition(), SoundEvents.BARREL_OPEN, SoundSource.BLOCKS, 1f, 1f);
                    }
                    player.openMenu(new GraveEntity.GraveScreenHandlerFactory(this));
                    return InteractionResult.SUCCESS;
                }
            }
            this.level().playSound(null, blockPosition(), SoundEvents.WOOD_HIT, SoundSource.BLOCKS, 1f, 1f);
            return InteractionResult.FAIL;
        }

        return InteractionResult.PASS;
    }
    public void tick() {
        super.tick();
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();
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
            if(this.level() instanceof ServerLevel && ((ServerLevel) this.level()).getGameRules().get(Riftbone.ENABLE_GRAVE_DESPAWN_SOUND)){
                level().playSound(null, blockPosition(), SoundEvents.ENDER_EYE_DEATH, SoundSource.BLOCKS, 1f, 1f);
            }
            this.discard();
        }
        if (this.level().isClientSide()) {
            this.noPhysics = false;
        } else {
            this.noPhysics = !this.level().noCollision(this, this.getBoundingBox().deflate(1.0E-7));
            if (this.noPhysics) {
                this.moveTowardsClosestSpace(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0, this.getZ());
            }
        }

        if (!this.onGround() || this.getDeltaMovement().horizontalDistanceSqr() > 9.999999747378752E-6 || (this.tickCount + this.getId()) % 4 == 0) {
            this.move(MoverType.SELF, this.getDeltaMovement());
            float g = 0.98F;
            if (this.onGround()) {
                g = this.level().getBlockState(new BlockPos(this.getBlockX(), this.getBlockY() - 1, this.getBlockZ())).getBlock().getFriction() * 0.98F;
            }
            if(getY()<=-64) {
                if (level() instanceof ServerLevel) {
                    if (((ServerLevel) level()).getGameRules().get(Riftbone.VOID_GRAVES_WARP_UP) == true && level().dimension() == Level.END) { //make sure we're in the end
                        setPosRaw(getX(), 64, getZ());
                    } else {
                        setPosRaw(getX(), -64, getZ());
                    }
                    this.setDeltaMovement(0, 0, 0);
                    this.setNoGravity(true);
                }
            }
            this.setDeltaMovement(this.getDeltaMovement().multiply((double) g, 0.98, (double) g));
            if (this.onGround()) {
                Vec3 vec3d2 = this.getDeltaMovement();
                if (vec3d2.y < 0.0) {
                    this.setDeltaMovement(vec3d2.multiply(1.0, -0.5, 1.0));
                }
            }
        }

        boolean bl = Mth.floor(this.xo) != Mth.floor(this.getX()) || Mth.floor(this.yo) != Mth.floor(this.getY()) || Mth.floor(this.zo) != Mth.floor(this.getZ());
        int i = bl ? 2 : 40;

        this.needsSync |= this.updateInWaterStateAndDoFluidPushing();
        if (!this.level().isClientSide()) {
            double d = this.getDeltaMovement().subtract(vec3d).lengthSqr();
            if (d > 0.01) {
                this.needsSync = true;
            }
        }
        if(this.xo != this.getX() || this.zo != this.getZ() && this.isNoGravity()) { //return the grave's gravity on being moved (eg. fishing rod)
            this.setNoGravity(false);
        }
    }
    private boolean isOwner(UUID uuid) {
        if (entityData.get(OWNER).isEmpty()) return false;
        UUID uuid1 = entityData.get(OWNER).get().getUUID();
        return uuid1.equals(uuid);
    }
    @Nullable
    public EntityReference<LivingEntity> getOwnerNullable() {
        return this.entityData.get(OWNER).orElse(null);
    }
    private void quickLoot(Player player) {
        if (!(player.level() instanceof ServerLevel world)) return;
        if (world.getGameRules().get(Riftbone.QUICK_LOOTING_ALLOWED) && (!world.getGameRules().get(Riftbone.OWNER_ONLY_QUICK_LOOTING) || isOwner(player.getUUID()))) {
            List<ItemStack> unslotted = new ArrayList<>();
            Inventory playerInventory = player.getInventory();
            inventory.items.forEach(stack -> {
                if (!TrinketsCompat.handleQuickLoot(stack, unslotted, player)) {
                    if (stack.has(Riftbone.SAVED_SLOT)) {
                        int slot = stack.get(Riftbone.SAVED_SLOT);
                        stack.remove(Riftbone.SAVED_SLOT);
                        if (playerInventory.getItem(slot).isEmpty() || ItemEntity.areMergable(stack, playerInventory.getItem(slot))) {
                            playerInventory.add(slot, stack);
                        } else {
                            unslotted.add(stack);
                        }
                    } else {
                        unslotted.add(stack);
                    }
                }
            });
            unslotted.forEach(stack -> {
                playerInventory.placeItemBackInInventory(stack, false);
            });
            this.discard();
            if(this.level() instanceof ServerLevel && ((ServerLevel) this.level()).getGameRules().get(Riftbone.ENABLE_GRAVE_DESPAWN_SOUND)) {
                level().playSound(null, blockPosition(), SoundEvents.ENDER_EYE_DEATH, SoundSource.BLOCKS, 1f, 1f);
            }
        } else {
            this.level().playSound(null, blockPosition(), SoundEvents.WOOD_HIT, SoundSource.BLOCKS, 1f, 1f);
        }
    }
    public boolean shouldShowName() {
        return true;
    }
    protected Entity.MovementEmission getMovementEmission() {
        return MovementEmission.NONE;
    }

    @Override
    public boolean hurtServer(ServerLevel world, DamageSource source, float amount) {
        return false;
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
            if(this.entity.level() instanceof ServerLevel && ((ServerLevel) this.entity.level()).getGameRules().get(Riftbone.ENABLE_GRAVE_SUFFIX)) {
                return this.entity.getDisplayName();
            } else {
                return this.entity.getDisplayName().copy().append("'s Remains");
            }
        }

        public @NotNull AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
            return ChestMenu.sixRows(syncId, inv, this.entity.inventory);
        }
    }
}
