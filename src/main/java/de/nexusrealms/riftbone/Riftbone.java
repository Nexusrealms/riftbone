package de.nexusrealms.riftbone;

import com.mojang.serialization.Codec;
import de.nexusrealms.riftbone.client.RiftboneClient;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Mod(value = "riftbone")
public class Riftbone {
	public static final String MOD_ID = "riftbone";
	public static final String LEGACY_MOD_ID = "riftrealmsutils";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final DeferredRegister<DataComponentType<?>> C = DeferredRegister.create(
			// The registry we want to use.
			// Minecraft's registries can be found in BuiltInRegistries, NeoForge's registries can be found in NeoForgeRegistries.
			// Mods may also add their own registries, refer to the individual mod's documentation or source code for where to find them.
			BuiltInRegistries.DATA_COMPONENT_TYPE,
			// Our mod id.
			MOD_ID
	);
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> SAVED_SLOT = C.register("saved_slot", () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build());
	public static final DeferredRegister<EntityType<?>> E = DeferredRegister.create(
			// The registry we want to use.
			// Minecraft's registries can be found in BuiltInRegistries, NeoForge's registries can be found in NeoForgeRegistries.
			// Mods may also add their own registries, refer to the individual mod's documentation or source code for where to find them.
			BuiltInRegistries.ENTITY_TYPE,
			// Our mod id.
			MOD_ID
	);
	public static final GameRules.Key<GameRules.BooleanValue> OWNER_ONLY_LOOTING = GameRules.register("ownerOnlyLooting", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));
	public static final GameRules.Key<GameRules.BooleanValue> OWNER_ONLY_QUICK_LOOTING = GameRules.register("ownerOnlyQuickLooting", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));
	public static final GameRules.Key<GameRules.BooleanValue> QUICK_LOOTING_ALLOWED = GameRules.register("quickLootingAllowed", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));
	public static final DeferredHolder<EntityType<?>, EntityType<GraveEntity>> GRAVE = E.register("grave", () -> EntityType.Builder.<GraveEntity>of(GraveEntity::new, MobCategory.MISC).sized(0.5f, 0.5f).build("grave"));

	public Riftbone(IEventBus modEventBus, ModContainer modContainer) {
		modEventBus.register(RiftboneClient.class);
		E.register(modEventBus);
		C.register(modEventBus);
		LOGGER.info("Riftbone loaded : wish i was on fabric");
	}
}