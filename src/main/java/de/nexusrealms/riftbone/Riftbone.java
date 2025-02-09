package de.nexusrealms.riftbone;

import com.mojang.serialization.Codec;
import de.nexusrealms.riftbone.client.LegacyGraveEntity;
import dev.emi.trinkets.api.SlotReference;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Riftbone implements ModInitializer {
	public static final String MOD_ID = "riftbone";
	public static final String LEGACY_MOD_ID = "riftrealmsutils";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final ComponentType<Integer> SAVED_SLOT = ComponentType.<Integer>builder().codec(Codec.INT).build();

	public static final EntityType<GraveEntity> GRAVE = EntityType.Builder.<GraveEntity>create(GraveEntity::new, SpawnGroup.MISC).dimensions(0.5f, 0.5f).build();
	public static final EntityType<LegacyGraveEntity> LEGACY_GRAVE = EntityType.Builder.<LegacyGraveEntity>create(LegacyGraveEntity::new, SpawnGroup.MISC).dimensions(0.5f, 0.5f).build();

	public static final TagKey<Item> SOULBOUND = TagKey.of(Registries.ITEM.getKey(), Identifier.of(MOD_ID, "soulbound"));

	public static final GameRules.Key<GameRules.BooleanRule> OWNER_ONLY_LOOTING = GameRuleRegistry.register("ownerOnlyLooting", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(false));
	public static final GameRules.Key<GameRules.BooleanRule> OWNER_ONLY_QUICK_LOOTING = GameRuleRegistry.register("ownerOnlyQuickLooting", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));
	public static final GameRules.Key<GameRules.BooleanRule> QUICK_LOOTING_ALLOWED = GameRuleRegistry.register("quickLootingAllowed", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));

	public static boolean isTrinketsLoaded = false;
	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(MOD_ID, "saved_slot"), SAVED_SLOT);
		Registry.register(Registries.ENTITY_TYPE, Identifier.of(MOD_ID, "grave"), GRAVE);
		Registry.register(Registries.ENTITY_TYPE, Identifier.of(LEGACY_MOD_ID, "grave"), LEGACY_GRAVE);
		LOGGER.info("Hello Fabric world!");
		isTrinketsLoaded = FabricLoader.getInstance().isModLoaded("trinkets");
		TrinketsCompat.init();
		SoulboundCallback.IS_SOULBOUND.register((oldPlayer, stack) -> stack.isIn(SOULBOUND));
	}
}