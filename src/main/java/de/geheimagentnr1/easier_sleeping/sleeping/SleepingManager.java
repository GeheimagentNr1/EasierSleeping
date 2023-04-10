package de.geheimagentnr1.easier_sleeping.sleeping;

import de.geheimagentnr1.easier_sleeping.config.DimensionListType;
import de.geheimagentnr1.easier_sleeping.config.ServerConfig;
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
import net.minecraftforge.event.ForgeEventFactory;

import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;


public class SleepingManager {
	
	
	private static TreeMap<ResourceKey<Level>, TreeSet<ServerPlayer>> SLEEPING;
	
	private static final Comparator<Player> PLAYER_COMPARATOR = Comparator.comparing( Entity::getUUID );
	
	public static void init() {
		
		SLEEPING = new TreeMap<>( Comparator.comparing( ResourceKey::location ) );
	}
	
	//package-private
	static void updateSleepingPlayers( MinecraftServer server ) {
		
		for( ServerLevel level : server.getAllLevels() ) {
			ResourceKey<Level> registrykey = level.dimension();
			boolean containsDimension = ServerConfig.getDimensions().contains( registrykey );
			if( ServerConfig.getDimensionListType() == DimensionListType.SLEEP_ACTIVE && !containsDimension ||
				ServerConfig.getDimensionListType() == DimensionListType.SLEEP_INACTIVE && containsDimension ) {
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
							ServerConfig.getIgnoredBedBlocks().contains(
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
			if( sleeping_percent >= ServerConfig.getSleepPercent() ||
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
					player.stopSleepInBed(false, false);
				} );
				if( level.getGameRules().getBoolean( GameRules.RULE_WEATHER_CYCLE ) ) {
					level.setWeatherParameters( 0, 0, false, false );
				}
				if( ServerConfig.getAllPlayersRest() ) {
					level_players.forEach( player -> player.resetStat( Stats.CUSTOM.get( Stats.TIME_SINCE_REST ) ) );
				}
				sendMorningMessage( level_players );
				sleeping_players.clear();
			}
		}
	}
	
	private static int countNonSpectatorPlayers( List<? extends Player> players ) {
		
		int count = 0;
		for( Player player : players ) {
			if( !player.isSpectator() ) {
				count++;
			}
		}
		return count;
	}
	
	private static int countSleepingPlayers( TreeSet<ServerPlayer> players ) {
		
		int count = 0;
		for( ServerPlayer player : players ) {
			if( player.isSleepingLongEnough() ) {
				count++;
			}
		}
		return count;
	}
	
	private static void sendWakeMessage(
		List<? extends Player> players,
		int sleep_player_count,
		int non_spectator_player_count,
		Player wake_player ) {
		
		sendMessage(
			players,
			buildWakeSleepMessage(
				wake_player,
				sleep_player_count,
				non_spectator_player_count,
				ServerConfig.getWakeMessage()
			)
		);
	}
	
	private static void sendSleepMessage(
		List<? extends Player> players,
		int sleep_player_count,
		int non_spectator_player_count,
		Player wake_player ) {
		
		sendMessage(
			players,
			buildWakeSleepMessage(
				wake_player,
				sleep_player_count,
				non_spectator_player_count,
				ServerConfig.getSleepMessage()
			)
		);
	}
	
	private static void sendMorningMessage( List<? extends Player> players ) {
		
		sendMessage( players, Component.literal( ServerConfig.getMorningMessage() ) );
	}
	
	private static void sendMessage( List<? extends Player> players, MutableComponent message ) {
		
		for( Player player : players ) {
			player.sendSystemMessage(
				message.setStyle( Style.EMPTY.withColor( TextColor.fromLegacyFormat( ChatFormatting.GRAY ) ) )
			);
		}
	}
	
	private static MutableComponent buildWakeSleepMessage(
		Player player,
		int sleep_player_count,
		int player_count,
		String message ) {
		
		return Component.literal( "" ).append( player.getDisplayName() )
			.append( String.format(
				" %s - %d/%d (%d%%)",
				message,
				sleep_player_count,
				player_count,
				caculateSleepingPercent( sleep_player_count, player_count )
			) );
	}
	
	private static int caculateSleepingPercent( int sleep_player_count, int non_spectator_player_count ) {
		
		return non_spectator_player_count == 0 ? 0 : sleep_player_count * 100 / non_spectator_player_count;
	}
}
