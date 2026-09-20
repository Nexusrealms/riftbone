package de.nexusrealms.riftbone.itemlogger;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import de.nexusrealms.riftbone.GraveEntity;
import de.nexusrealms.riftbone.Riftbone;
import eu.pb4.playerdata.api.PlayerDataApi;
import eu.pb4.playerdata.api.storage.NbtCodecDataStorage;
import eu.pb4.playerdata.api.storage.PlayerDataStorage;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionCheck;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.Vec3;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ItemLogger {
    public static Config config;
    private static Duration duration;
    public static final PlayerDataStorage<List<ItemLog>> ITEM_LOG_STORAGE = new NbtCodecDataStorage<>("riftbone_item_log", ItemLog.CODEC.listOf());

    public static void init(){
        config = Config.load();
        duration = Duration.of(config.duration, config.unit);
        PlayerDataApi.register(ITEM_LOG_STORAGE);
        ServerPlayerEvents.JOIN.register(player -> {
            if(player.getInventory().isEmpty()) return;
            List<ItemLog> logs = PlayerDataApi.getCustomDataFor(player, ITEM_LOG_STORAGE);
            if(logs == null){
                save(player, Optional.empty());
            } else {
                getMostRecent(logs).ifPresentOrElse(itemLog -> {
                    Instant now = Instant.now();
                    Duration duration = Duration.between(itemLog.date().toInstant(), now);
                    if(duration.compareTo(ItemLogger.duration) > 0){
                        save(player, Optional.of(logs));
                    }
                }, () -> save(player, Optional.empty()));
            }
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(Commands.literal("itemlog")
                        .requires(s -> s.isPlayer() && Commands.hasPermission(new PermissionCheck.Require(Permissions.COMMANDS_MODERATOR)).test(s))
                                .then(Commands.literal("save")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(context -> {
                                                    ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                                    save(player, Optional.empty());
                                                    return 1;
                                                })))
                                .then(Commands.literal("killallgraves")
                                        .executes(context -> {
                                            List<? extends GraveEntity> graves = context.getSource().getPlayer().level().getEntities(EntityTypeTest.forClass(GraveEntity.class), GraveEntity::wasItemLog);
                                            graves.forEach(GraveEntity::discard);
                                            context.getSource().sendSuccess(() -> graves.isEmpty() ? Component.literal("No graves to remove") : Component.literal("Removed " + graves.size() + " item log graves"), true);
                                            return 1;
                                        }))
                        .then(Commands.literal("spawn")
                                .then(Commands.argument("log", IntegerArgumentType.integer(0, config.limit - 1))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> {
                                            ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                            int log = IntegerArgumentType.getInteger(context, "log");
                                            if(PlayerDataApi.getCustomDataFor(player, ITEM_LOG_STORAGE) instanceof List<ItemLog> list){
                                                if(list.size() <= log){
                                                    context.getSource().sendFailure(Component.literal("Player does not have this many logs"));
                                                    return 2;
                                                }
                                                ItemLog opt = list.stream().sorted(Comparator.comparing(ItemLog::date).reversed()).toList().get(log);
                                                spawn(context.getSource().getLevel(), context.getSource().getPlayer().position(), opt);
                                                context.getSource().sendSuccess(() -> Component.literal("Spawned grave with logged items"), true);
                                                return 1;
                                            } else {
                                                context.getSource().sendFailure(Component.literal("Player has no Item log"));
                                                return 3;
                                            }
                                        }))
                                .then(Commands.argument("uuid", UuidArgument.uuid())
                                        .executes(context -> {
                                            UUID uuid = UuidArgument.getUuid(context, "uuid");
                                            int log = IntegerArgumentType.getInteger(context, "log");
                                            if(PlayerDataApi.getCustomDataFor(context.getSource().getServer(), uuid, ITEM_LOG_STORAGE) instanceof List<ItemLog> list){
                                                if(list.size() >= log){
                                                    context.getSource().sendFailure(Component.literal("Player does not have this many logs"));
                                                    return 2;
                                                }
                                                ItemLog opt = list.stream().sorted(Comparator.comparing(ItemLog::date).reversed()).toList().get(log);
                                                spawn(context.getSource().getLevel(), context.getSource().getPlayer().position(), opt);
                                                context.getSource().sendSuccess(() -> Component.literal("Spawned grave with logged items"), true);
                                                return 1;
                                            } else {
                                                context.getSource().sendFailure(Component.literal("Player has no Item log, or this is not a valid player UUID"));
                                                return 3;
                                            }
                                        }))))));
    }
    private static void spawn(ServerLevel level, Vec3 position, ItemLog log){
        GraveEntity grave = new GraveEntity(position, log, level);
        level.addFreshEntity(grave);
    }
    private static void save(ServerPlayer player, Optional<List<ItemLog>> prev){
        ItemLog log = ItemLog.createFromPlayer(player);
        Riftbone.LOGGER.info("Saving item log for {} : {}", player.getScoreboardName(), player.getUUID());
        prev.ifPresentOrElse(logs -> {
            Riftbone.LOGGER.info("Merging logs");
            List<ItemLog> nw = new java.util.ArrayList<>(logs.stream().sorted(Comparator.comparing(ItemLog::date)).toList());
            if(nw.size() >= config.limit){ nw.set(0, log);} else nw.add(log);
            PlayerDataApi.setCustomDataFor(player, ITEM_LOG_STORAGE, nw);
        }, () -> PlayerDataApi.setCustomDataFor(player,ITEM_LOG_STORAGE, List.of(log)));
    }
    private static Optional<ItemLog> getMostRecent(List<ItemLog> list){
        return list.stream().sorted(Comparator.comparing(ItemLog::date).reversed()).findFirst();
    }
}
