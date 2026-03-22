package de.nexusrealms.riftbone;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.serialization.Codec;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.Identifier;
import net.minecraft.world.rule.GameRule;
import net.minecraft.world.rule.GameRuleCategory;
import net.minecraft.world.rule.GameRuleType;
import net.minecraft.world.rule.GameRuleVisitor;
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

	public static final EntityType<GraveEntity> GRAVE = EntityType.Builder.<GraveEntity>create(GraveEntity::new, SpawnGroup.MISC).dimensions(0.5f, 0.5f).build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MOD_ID, "grave")));

	public static final TagKey<Item> SOULBOUND = TagKey.of(Registries.ITEM.getKey(), Identifier.of(MOD_ID, "soulbound"));

    public static final GameRule<Boolean> VOID_GRAVES_WARP_UP = Registry.register(Registries.GAME_RULE, Identifier.of(MOD_ID, "void_graves_warp_up"), new GameRule<>(GameRuleCategory.PLAYER, GameRuleType.BOOL, BoolArgumentType.bool(), GameRuleVisitor::visitBoolean, Codec.BOOL, (value) -> value ? 1 : 0, false, FeatureSet.empty()));
    public static final GameRule<Boolean> ENABLE_GRAVE_SUFFIX = Registry.register(Registries.GAME_RULE, Identifier.of(MOD_ID, "enable_grave_suffix"), new GameRule<>(GameRuleCategory.PLAYER, GameRuleType.BOOL, BoolArgumentType.bool(), GameRuleVisitor::visitBoolean, Codec.BOOL, (value) -> value ? 1 : 0, false, FeatureSet.empty()));
    public static final GameRule<Boolean> ENABLE_GRAVE_OPEN_SOUND = Registry.register(Registries.GAME_RULE, Identifier.of(MOD_ID, "enable_grave_opening_sound"), new GameRule<>(GameRuleCategory.PLAYER, GameRuleType.BOOL, BoolArgumentType.bool(), GameRuleVisitor::visitBoolean, Codec.BOOL, (value) -> value ? 1 : 0, false, FeatureSet.empty())); //set to false by default because barrel sound is annoying
    public static final GameRule<Boolean> OWNER_ONLY_LOOTING = Registry.register(Registries.GAME_RULE, Identifier.of(MOD_ID, "owner_only_looting"), new GameRule<>(GameRuleCategory.PLAYER, GameRuleType.BOOL, BoolArgumentType.bool(), GameRuleVisitor::visitBoolean, Codec.BOOL, (value) -> value ? 1 : 0, false, FeatureSet.empty()));
	public static final GameRule<Boolean> OWNER_ONLY_QUICK_LOOTING = Registry.register(Registries.GAME_RULE, Identifier.of(MOD_ID, "owner_onlyy_quick_looting"), new GameRule<>(GameRuleCategory.PLAYER, GameRuleType.BOOL, BoolArgumentType.bool(), GameRuleVisitor::visitBoolean, Codec.BOOL, (value) -> value ? 1 : 0, true, FeatureSet.empty()));
	public static final GameRule<Boolean> QUICK_LOOTING_ALLOWED = Registry.register(Registries.GAME_RULE, Identifier.of(MOD_ID, "quick_looting_allowed"), new GameRule<>(GameRuleCategory.PLAYER, GameRuleType.BOOL, BoolArgumentType.bool(), GameRuleVisitor::visitBoolean, Codec.BOOL, (value) -> value ? 1 : 0, true, FeatureSet.empty()));

	public static boolean isTrinketsLoaded = false;
	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(MOD_ID, "saved_slot"), SAVED_SLOT);
		Registry.register(Registries.ENTITY_TYPE, Identifier.of(MOD_ID, "grave"), GRAVE);
		LOGGER.info("Hello Fabric world!");
		isTrinketsLoaded = FabricLoader.getInstance().isModLoaded("trinkets");
		TrinketsCompat.init();
		SoulboundCallback.IS_SOULBOUND.register((oldPlayer, stack) -> stack.isIn(SOULBOUND));
	}
}