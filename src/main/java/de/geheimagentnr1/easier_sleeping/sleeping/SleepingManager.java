package de.geheimagentnr1.easier_sleeping.sleeping;

import de.geheimagentnr1.easier_sleeping.config.DimensionListType;
import de.geheimagentnr1.easier_sleeping.config.ServerConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.Stats;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.Util;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;


public class SleepingManager {
	
	
	private static TreeMap<RegistryKey<World>, TreeSet<ServerPlayerEntity>> SLEEPING;
	
	private static final Comparator<PlayerEntity> PLAYER_COMPARATOR = Comparator.comparing( Entity::getUUID );
	
	public static void init() {
		
		SLEEPING = new TreeMap<>( Comparator.comparing( RegistryKey::location ) );
	}
	
	//package-private
	static void updateSleepingPlayers( MinecraftServer server ) {
		
		for( ServerWorld world : server.getAllLevels() ) {
			RegistryKey<World> registrykey = world.dimension();
			boolean containsDimension = ServerConfig.getDimensions().contains( registrykey );
			if( ServerConfig.getDimensionListType() == DimensionListType.SLEEP_ACTIVE && !containsDimension ||
				ServerConfig.getDimensionListType() == DimensionListType.SLEEP_INACTIVE && containsDimension ) {
				continue;
			}
			if( !SLEEPING.containsKey( registrykey ) ) {
				SLEEPING.put( registrykey, new TreeSet<>( PLAYER_COMPARATOR ) );
			}
			TreeSet<ServerPlayerEntity> sleeping_players = SLEEPING.get( registrykey );
			List<ServerPlayerEntity> world_players = world.players();
			int non_spectator_player_count = countNonSpectatorPlayers( world_players );
			for( ServerPlayerEntity player : world_players ) {
				if( player.isSleeping() && !sleeping_players.contains( player ) ) {
					sleeping_players.add( player );
					sendSleepMessage( world_players, sleeping_players.size(), non_spectator_player_count, player );
				} else {
					if( !player.isSleeping() && sleeping_players.contains( player ) ) {
						sleeping_players.remove( player );
						sendWakeMessage( world_players, sleeping_players.size(), non_spectator_player_count, player );
					}
				}
			}
			int sleeping_percent = caculateSleepingPercent(
				countSleepingPlayers( sleeping_players ),
				non_spectator_player_count
			);
			if( sleeping_percent >= ServerConfig.getSleepPercent() ||
				non_spectator_player_count > 0 && non_spectator_player_count == sleeping_players.size() ) {
				if( world.getGameRules().getBoolean( GameRules.RULE_DAYLIGHT ) ) {
					long currentDayTime = world.getDayTime();
					long newDayTime = currentDayTime + 24000L - currentDayTime % 24000L;
					newDayTime = ForgeEventFactory.onSleepFinished( world, newDayTime, currentDayTime );
					world.setDayTime( newDayTime );
				}
				sleeping_players.forEach( player -> {
					player.getSleepingPos().ifPresent(
						pos -> player.setRespawnPosition( world.dimension(), pos, player.yRot, false, false )
					);
					player.stopSleeping();
				} );
				if( world.getGameRules().getBoolean( GameRules.RULE_WEATHER_CYCLE ) ) {
					world.setWeatherParameters( 0, 0, false, false );
				}
				if( ServerConfig.getAllPlayersRest() ) {
					world_players.forEach(
						playerEntity -> playerEntity.resetStat( Stats.CUSTOM.get( Stats.TIME_SINCE_REST ) )
					);
				}
				sendMorningMessage( world_players );
				sleeping_players.clear();
			}
		}
	}
	
	private static int countNonSpectatorPlayers( List<? extends PlayerEntity> players ) {
		
		int count = 0;
		for( PlayerEntity player : players ) {
			if( !player.isSpectator() ) {
				count++;
			}
		}
		return count;
	}
	
	private static int countSleepingPlayers( TreeSet<ServerPlayerEntity> players ) {
		
		int count = 0;
		for( ServerPlayerEntity player : players ) {
			if( player.isSleepingLongEnough() ) {
				count++;
			}
		}
		return count;
	}
	
	private static void sendWakeMessage(
		List<? extends PlayerEntity> players,
		int sleep_player_count,
		int non_spectator_player_count,
		PlayerEntity wake_player ) {
		
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
		List<? extends PlayerEntity> players,
		int sleep_player_count,
		int non_spectator_player_count,
		PlayerEntity wake_player ) {
		
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
	
	private static void sendMorningMessage( List<? extends PlayerEntity> players ) {
		
		sendMessage( players, new StringTextComponent( ServerConfig.getMorningMessage() ) );
	}
	
	private static void sendMessage( List<? extends PlayerEntity> players, IFormattableTextComponent message ) {
		
		for( PlayerEntity player : players ) {
			player.sendMessage(
				message.setStyle( Style.EMPTY.withColor( TextFormatting.YELLOW ) ),
				Util.NIL_UUID
			);
		}
	}
	
	private static IFormattableTextComponent buildWakeSleepMessage(
		PlayerEntity player,
		int sleep_player_count,
		int player_count,
		String message ) {
		
		return new StringTextComponent( "" ).append( player.getDisplayName() )
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
