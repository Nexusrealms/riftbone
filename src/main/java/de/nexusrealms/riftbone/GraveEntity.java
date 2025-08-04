package de.nexusrealms.riftbone;

import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.inventory.StackWithSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GraveEntity extends Entity {
    protected static final TrackedData<Optional<LazyEntityReference<LivingEntity>>> OWNER = DataTracker.registerData(GraveEntity.class, TrackedDataHandlerRegistry.LAZY_ENTITY_REFERENCE);

    public GraveEntity(EntityType<?> type, World world) {
        super(type, world);
    }
    public GraveEntity(PlayerEntity entity) {
        super(Riftbone.GRAVE, entity.getWorld());
        dataTracker.set(OWNER, Optional.of(new LazyEntityReference<>(entity.getUuid())));
        setCustomName(Text.literal(entity.getName().getString() + "'s grave"));
        placeItemsInGrave(entity);
        copyPositionAndRotation(entity);
        //TrinketsCompat.onGraveSpawn(entity);
    }

    private void addStack(PlayerEntity player, ItemStack stack, int slot) {
        if(!SoulboundHandler.isSoulbound(stack, player)) {
            stack.set(Riftbone.SAVED_SLOT, slot);
            inventory.addStack(stack);
        }
    }
    private void placeItemsInGrave(PlayerEntity entity) {
        for (int i = 0; i < entity.getInventory().size(); i++) {
            addStack(entity, entity.getInventory().getStack(i), i);
        }
        //TrinketsCompat.addTrinketsToGrave(inventory, entity);
    }
    private final SimpleInventory inventory = new SimpleInventory(54) {
        public ItemStack removeStack(int slot) {
            ItemStack stack = super.removeStack(slot);
            stack.remove(Riftbone.SAVED_SLOT);
            return stack;
        }

        @Override
        public ItemStack removeStack(int slot, int amount) {
            ItemStack stack = super.removeStack(slot);
            stack.remove(Riftbone.SAVED_SLOT);
            return stack;
        }
    };

    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(OWNER, Optional.empty());
    }

    @Override
    protected void readCustomData(ReadView readView) {
        //Automatic data migration
        readView.read("owner", Uuids.INT_STREAM_CODEC).ifPresent(uuid1 -> dataTracker.set(OWNER, Optional.of(new LazyEntityReference<>(uuid1))));
        ReadView.TypedListReadView<ItemStack> list = readView.getTypedListView("inventory", ItemStack.CODEC);
        if(!list.isEmpty()){
            inventory.readDataList(list);
        }

        for(StackWithSlot stackWithSlot : readView.getTypedListView("contents", StackWithSlot.CODEC)) {
            if (stackWithSlot.isValidSlot(inventory.size())) {
                inventory.setStack(stackWithSlot.slot(), stackWithSlot.stack());
            }
        }
        LazyEntityReference<LivingEntity> lazyEntityReference = LazyEntityReference.fromDataOrPlayerName(readView, "Owner", this.getWorld());
        if (lazyEntityReference != null) {
            this.dataTracker.set(OWNER, Optional.of(lazyEntityReference));
        } else {
            this.dataTracker.set(OWNER, Optional.empty());
        }
    }

    @Override
    protected void writeCustomData(WriteView writeView) {
        WriteView.ListAppender<StackWithSlot> listAppender = writeView.getListAppender("contents", StackWithSlot.CODEC);
        for(int i = 0; i < inventory.size(); ++i) {
            ItemStack itemStack = inventory.getStack(i);
            if (!itemStack.isEmpty()) {
                listAppender.add(new StackWithSlot(i, itemStack));
            }
        }
        LazyEntityReference<LivingEntity> lazyEntityReference = this.getOwnerNullable();
        LazyEntityReference.writeData(lazyEntityReference, writeView, "Owner");
    }

    @Override
    public boolean canHit() {
        return true;
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if(!getWorld().isClient()){
            if(!getWorld().getServer().getGameRules().getBoolean(Riftbone.OWNER_ONLY_LOOTING) || isOwner(player.getUuid())){
                if(player.isSneaking()){
                    quickLoot(player);
                    return ActionResult.SUCCESS;
                } else {
                    getWorld().playSound(null, getBlockPos(), SoundEvents.BLOCK_BARREL_OPEN, SoundCategory.BLOCKS, 1f, 1f);
                    player.openHandledScreen(new GraveEntity.GraveScreenHandlerFactory(this));
                    return ActionResult.SUCCESS;
                }
            }
            this.getWorld().playSound(null, getBlockPos(), SoundEvents.BLOCK_WOOD_HIT, SoundCategory.BLOCKS, 1f, 1f);
            return ActionResult.FAIL;
        }

        return ActionResult.PASS;
    }
    public void tick() {
        super.tick();
        this.lastX = this.getX();
        this.lastY = this.getY();
        this.lastZ = this.getZ();
        Vec3d vec3d = this.getVelocity();
        float f = this.getStandingEyeHeight() - 0.11111111F;
        if (this.isTouchingWater() && this.getFluidHeight(FluidTags.WATER) > (double) f) {
            this.applyWaterBuoyancy();
        } else if (this.isInLava() && this.getFluidHeight(FluidTags.LAVA) > (double) f) {
            this.applyLavaBuoyancy();
        } else if (!this.hasNoGravity()) {
            this.setVelocity(this.getVelocity().add(0.0, -0.04, 0.0));
        }
        if (!this.getWorld().isClient && this.age % 100 == 0 && inventory.isEmpty()) {
            getWorld().playSound(null, getBlockPos(), SoundEvents.ENTITY_ENDER_EYE_DEATH, SoundCategory.BLOCKS, 1f, 1f);
            this.discard();
        }
        if (this.getWorld().isClient) {
            this.noClip = false;
        } else {
            this.noClip = !this.getWorld().isSpaceEmpty(this, this.getBoundingBox().contract(1.0E-7));
            if (this.noClip) {
                this.pushOutOfBlocks(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0, this.getZ());
            }
        }

        if (!this.isOnGround() || this.getVelocity().horizontalLengthSquared() > 9.999999747378752E-6 || (this.age + this.getId()) % 4 == 0) {
            this.move(MovementType.SELF, this.getVelocity());
            float g = 0.98F;
            if (this.isOnGround()) {
                g = this.getWorld().getBlockState(new BlockPos(this.getBlockX(), this.getBlockY()- 1, this.getBlockZ())).getBlock().getSlipperiness() * 0.98F;
            }
            if (getY() <= -64) {
                setPos(getX(), -64, getZ());
            }
            this.setVelocity(this.getVelocity().multiply((double) g, 0.98, (double) g));
            if (this.isOnGround()) {
                Vec3d vec3d2 = this.getVelocity();
                if (vec3d2.y < 0.0) {
                    this.setVelocity(vec3d2.multiply(1.0, -0.5, 1.0));
                }
            }
        }

        boolean bl = MathHelper.floor(this.lastX) != MathHelper.floor(this.getX()) || MathHelper.floor(this.lastY) != MathHelper.floor(this.getY()) || MathHelper.floor(this.lastZ) != MathHelper.floor(this.getZ());
        int i = bl ? 2 : 40;

        this.velocityDirty |= this.updateWaterState();
        if (!this.getWorld().isClient) {
            double d = this.getVelocity().subtract(vec3d).lengthSquared();
            if (d > 0.01) {
                this.velocityDirty = true;
            }
        }


    }
    private boolean isOwner(UUID uuid){
        if(dataTracker.get(OWNER).isEmpty()) return false;
        UUID uuid1 = dataTracker.get(OWNER).get().getUuid();
        return uuid1.equals(uuid);
    }
    @Nullable
    public LazyEntityReference<LivingEntity> getOwnerNullable() {
        return this.dataTracker.get(OWNER).orElse(null);
    }
    private void quickLoot(PlayerEntity player){
        if(getWorld().getServer().getGameRules().getBoolean(Riftbone.QUICK_LOOTING_ALLOWED) && (!getWorld().getServer().getGameRules().getBoolean(Riftbone.OWNER_ONLY_QUICK_LOOTING) || isOwner(player.getUuid()))){
            List<ItemStack> unslotted = new ArrayList<>();
            PlayerInventory playerInventory = player.getInventory();
            inventory.heldStacks.forEach(stack -> {
                //if(!TrinketsCompat.handleQuickLoot(stack, unslotted, player)){
                if(true){
                    if(stack.contains(Riftbone.SAVED_SLOT)){
                        int slot = stack.get(Riftbone.SAVED_SLOT);
                        stack.remove(Riftbone.SAVED_SLOT);
                        if(playerInventory.getStack(slot).isEmpty() || ItemEntity.canMerge(stack, playerInventory.getStack(slot))){
                            playerInventory.insertStack(slot, stack);
                        } else {
                            unslotted.add(stack);
                        }
                    } else {
                        unslotted.add(stack);
                    }
                }
            });
            unslotted.forEach(stack -> {
                playerInventory.offer(stack, false);
            });
            this.discard();
            getWorld().playSound(null, getBlockPos(), SoundEvents.ENTITY_ENDER_EYE_DEATH, SoundCategory.BLOCKS, 1f, 1f);
        } else {
            this.getWorld().playSound(null, getBlockPos(), SoundEvents.BLOCK_WOOD_HIT, SoundCategory.BLOCKS, 1f, 1f);
        }
    }
    public boolean shouldRenderName() {
        return true;
    }
    protected Entity.MoveEffect getMoveEffect() {
        return MoveEffect.NONE;
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        return false;
    }

    private void applyWaterBuoyancy() {
        Vec3d vec3d = this.getVelocity();
        this.setVelocity(vec3d.x * 0.9900000095367432, vec3d.y + (double) (vec3d.y < 0.05999999865889549 ? 5.0E-4F : 0.0F), vec3d.z * 0.9900000095367432);
    }

    private void applyLavaBuoyancy() {
        Vec3d vec3d = this.getVelocity();
        this.setVelocity(vec3d.x * 0.949999988079071, vec3d.y + (double) (vec3d.y < 0.05999999865889549 ? 5.0E-4F : 0.0F), vec3d.z * 0.949999988079071);
    }
    private static class GraveScreenHandlerFactory implements NamedScreenHandlerFactory {
        private final GraveEntity entity;

        private GraveScreenHandlerFactory(GraveEntity entity) {
            this.entity = entity;
        }

        public Text getDisplayName() {
            return this.entity.getDisplayName();
        }

        public @NotNull ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
            return GenericContainerScreenHandler.createGeneric9x6(syncId, inv, this.entity.inventory);
        }

    }
}
