package de.nexusrealms.riftbone;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.serialization.Codec;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Riftbone implements ModInitializer {
	public static final String MOD_ID = "riftbone";
	public static final String LEGACY_MOD_ID = "riftrealmsutils";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final DataComponentType<Integer> SAVED_SLOT = DataComponentType.<Integer>builder().persistent(Codec.INT).build();

	public static final EntityType<GraveEntity> GRAVE = EntityType.Builder.<GraveEntity>of(GraveEntity::new, MobCategory.MISC).sized(0.5f, 0.5f).build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, "grave")));

	public static final TagKey<Item> SOULBOUND = TagKey.create(BuiltInRegistries.ITEM.key(), Identifier.fromNamespaceAndPath(MOD_ID, "soulbound"));

    public static final GameRule<Boolean> VOID_GRAVES_WARP_UP = Registry.register(BuiltInRegistries.GAME_RULE, Identifier.fromNamespaceAndPath(MOD_ID, "void_graves_warp_up"), new GameRule<>(GameRuleCategory.PLAYER, GameRuleType.BOOL, BoolArgumentType.bool(), GameRuleTypeVisitor::visitBoolean, Codec.BOOL, (value) -> value ? 1 : 0, false, FeatureFlagSet.of()));
    public static final GameRule<Boolean> ENABLE_GRAVE_SUFFIX = Registry.register(BuiltInRegistries.GAME_RULE, Identifier.fromNamespaceAndPath(MOD_ID, "enable_grave_suffix"), new GameRule<>(GameRuleCategory.PLAYER, GameRuleType.BOOL, BoolArgumentType.bool(), GameRuleTypeVisitor::visitBoolean, Codec.BOOL, (value) -> value ? 1 : 0, false, FeatureFlagSet.of()));
    public static final GameRule<Boolean> ENABLE_GRAVE_OPEN_SOUND = Registry.register(BuiltInRegistries.GAME_RULE, Identifier.fromNamespaceAndPath(MOD_ID, "enable_grave_opening_sound"), new GameRule<>(GameRuleCategory.PLAYER, GameRuleType.BOOL, BoolArgumentType.bool(), GameRuleTypeVisitor::visitBoolean, Codec.BOOL, (value) -> value ? 1 : 0, false, FeatureFlagSet.of())); //set to false by default because barrel sound is annoying
    public static final GameRule<Boolean> OWNER_ONLY_LOOTING = Registry.register(BuiltInRegistries.GAME_RULE, Identifier.fromNamespaceAndPath(MOD_ID, "owner_only_looting"), new GameRule<>(GameRuleCategory.PLAYER, GameRuleType.BOOL, BoolArgumentType.bool(), GameRuleTypeVisitor::visitBoolean, Codec.BOOL, (value) -> value ? 1 : 0, false, FeatureFlagSet.of()));
	public static final GameRule<Boolean> OWNER_ONLY_QUICK_LOOTING = Registry.register(BuiltInRegistries.GAME_RULE, Identifier.fromNamespaceAndPath(MOD_ID, "owner_only_quick_looting"), new GameRule<>(GameRuleCategory.PLAYER, GameRuleType.BOOL, BoolArgumentType.bool(), GameRuleTypeVisitor::visitBoolean, Codec.BOOL, (value) -> value ? 1 : 0, true, FeatureFlagSet.of()));
	public static final GameRule<Boolean> QUICK_LOOTING_ALLOWED = Registry.register(BuiltInRegistries.GAME_RULE, Identifier.fromNamespaceAndPath(MOD_ID, "quick_looting_allowed"), new GameRule<>(GameRuleCategory.PLAYER, GameRuleType.BOOL, BoolArgumentType.bool(), GameRuleTypeVisitor::visitBoolean, Codec.BOOL, (value) -> value ? 1 : 0, true, FeatureFlagSet.of()));

	public static boolean isTrinketsLoaded = false;
	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, "saved_slot"), SAVED_SLOT);
		Registry.register(BuiltInRegistries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, "grave"), GRAVE);
		LOGGER.info("Hello Fabric world!");
		isTrinketsLoaded = FabricLoader.getInstance().isModLoaded("trinkets");
		TrinketsCompat.init();
		SoulboundCallback.IS_SOULBOUND.register((oldPlayer, stack) -> stack.is(SOULBOUND));
	}
}