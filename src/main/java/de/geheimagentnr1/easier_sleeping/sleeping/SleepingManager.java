package de.geheimagentnr1.easier_sleeping.sleeping;

import de.geheimagentnr1.easier_sleeping.config.DimensionListType;
import de.geheimagentnr1.easier_sleeping.config.ServerConfig;
import de.geheimagentnr1.minecraft_forge_api.events.ForgeEventHandlerInterface;
import lombok.RequiredArgsConstructor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.WorldWorkerManager;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;


@RequiredArgsConstructor
public class SleepingManager implements ForgeEventHandlerInterface {
	
	
	@NotNull
	private static final Comparator<Player> PLAYER_COMPARATOR = Comparator.comparing( Entity::getUUID );
	
	@NotNull
	private final ServerConfig serverConfig;
	
	private TreeMap<ResourceKey<Level>, TreeSet<ServerPlayer>> SLEEPING;
	
	private void init() {
		
		SLEEPING = new TreeMap<>( Comparator.comparing( ResourceKey::location ) );
	}
	
	//package-private
	void updateSleepingPlayers( @NotNull MinecraftServer server ) {
		
		for( ServerLevel level : server.getAllLevels() ) {
			ResourceKey<Level> registrykey = level.dimension();
			boolean containsDimension = serverConfig.getDimensions().contains( registrykey );
			if( serverConfig.getDimensionListType() == DimensionListType.SLEEP_ACTIVE && !containsDimension ||
				serverConfig.getDimensionListType() == DimensionListType.SLEEP_INACTIVE && containsDimension ) {
				continue;
			}
			if( !SLEEPING.containsKey( registrykey ) ) {
				SLEEPING.put( registrykey, new TreeSet<>( PLAYER_COMPARATOR ) );
			}
			TreeSet<ServerPlayer> sleeping_players = SLEEPING.get( registrykey );
			List<ServerPlayer> level_players = level.players();
			int non_spectator_player_count = countNonSpectatorPlayers( level_players );
			for( ServerPlayer player : level_players ) {
				if( player.isSleeping() && !sleeping_players.contains( player ) ) {
					if( player.getSleepingPos()
						.stream()
						.noneMatch( pos ->
							serverConfig.getIgnoredBedBlocks().contains(
								BuiltInRegistries.BLOCK.getKey( level.getBlockState( pos ).getBlock() )
							) ) ) {
						sleeping_players.add( player );
						sendSleepMessage( level_players, sleeping_players.size(), non_spectator_player_count, player );
					}
				} else {
					if( !player.isSleeping() && sleeping_players.contains( player ) ) {
						sleeping_players.remove( player );
						sendWakeMessage( level_players, sleeping_players.size(), non_spectator_player_count, player );
					}
				}
			}
			int sleeping_percent = caculateSleepingPercent(
				countSleepingPlayers( sleeping_players ),
				non_spectator_player_count
			);
			if( sleeping_percent >= serverConfig.getSleepPercent() ||
				non_spectator_player_count > 0 && non_spectator_player_count == sleeping_players.size() ) {
				if( level.getGameRules().getBoolean( GameRules.RULE_DAYLIGHT ) ) {
					long currentDayTime = level.getDayTime();
					long newDayTime = currentDayTime + 24000L - currentDayTime % 24000L;
					newDayTime = ForgeEventFactory.onSleepFinished( level, newDayTime, currentDayTime );
					level.setDayTime( newDayTime );
				}
				sleeping_players.forEach( player -> {
					player.getSleepingPos().ifPresent(
						pos -> player.setRespawnPosition( level.dimension(), pos, player.getYRot(), false, false )
					);
					player.stopSleepInBed( false, false );
				} );
				if( level.getGameRules().getBoolean( GameRules.RULE_WEATHER_CYCLE ) ) {
					level.setWeatherParameters( 0, 0, false, false );
				}
				if( serverConfig.getAllPlayersRest() ) {
					level_players.forEach( player -> player.resetStat( Stats.CUSTOM.get( Stats.TIME_SINCE_REST ) ) );
				}
				sendMorningMessage( level_players );
				sleeping_players.clear();
			}
		}
	}
	
	private int countNonSpectatorPlayers( @NotNull List<? extends Player> players ) {
		
		int count = 0;
		for( Player player : players ) {
			if( !player.isSpectator() ) {
				count++;
			}
		}
		return count;
	}
	
	private int countSleepingPlayers( @NotNull TreeSet<ServerPlayer> players ) {
		
		int count = 0;
		for( ServerPlayer player : players ) {
			if( player.isSleepingLongEnough() ) {
				count++;
			}
		}
		return count;
	}
	
	private void sendWakeMessage(
		@NotNull List<? extends Player> players,
		int sleep_player_count,
		int non_spectator_player_count,
		@NotNull Player wake_player ) {
		
		sendMessage(
			players,
			buildWakeSleepMessage(
				wake_player,
				sleep_player_count,
				non_spectator_player_count,
				serverConfig.getWakeMessage()
			)
		);
	}
	
	private void sendSleepMessage(
		@NotNull List<? extends Player> players,
		int sleep_player_count,
		int non_spectator_player_count,
		@NotNull Player wake_player ) {
		
		sendMessage(
			players,
			buildWakeSleepMessage(
				wake_player,
				sleep_player_count,
				non_spectator_player_count,
				serverConfig.getSleepMessage()
			)
		);
	}
	
	private void sendMorningMessage( @NotNull List<? extends Player> players ) {
		
		sendMessage( players, Component.literal( serverConfig.getMorningMessage() ) );
	}
	
	private void sendMessage( @NotNull List<? extends Player> players, @NotNull MutableComponent message ) {
		
		for( Player player : players ) {
			player.sendSystemMessage(
				message.setStyle( Style.EMPTY.withColor( TextColor.fromLegacyFormat( ChatFormatting.GRAY ) ) )
			);
		}
	}
	
	@NotNull
	private MutableComponent buildWakeSleepMessage(
		@NotNull Player player,
		int sleep_player_count,
		int player_count,
		@NotNull String message ) {
		
		return Component.literal( "" ).append( player.getDisplayName() )
			.append( String.format(
				" %s - %d/%d (%d%%)",
				message,
				sleep_player_count,
				player_count,
				caculateSleepingPercent( sleep_player_count, player_count )
			) );
	}
	
	private int caculateSleepingPercent( int sleep_player_count, int non_spectator_player_count ) {
		
		return non_spectator_player_count == 0 ? 0 : sleep_player_count * 100 / non_spectator_player_count;
	}
	
	@SubscribeEvent
	@Override
	public void handleServerStartingEvent( @NotNull ServerStartingEvent event ) {
		
		init();
		WorldWorkerManager.addWorker( new SleepingWorker( this ) );
	}
}
