package de.geheimagentnr1.easier_sleeping.sleeping;

import de.geheimagentnr1.easier_sleeping.config.ModConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;


public class SleepingManager {
	
	
	private static TreeMap<DimensionType, TreeSet<PlayerEntity>> SLEEPING;
	
	public static void init() {
		
		SLEEPING = new TreeMap<>( Comparator.comparingInt( DimensionType::getId ) );
		for( World world : ServerLifecycleHooks.getCurrentServer().getWorlds() ) {
			SLEEPING.put( world.getDimension().getType(),
				new TreeSet<>( Comparator.comparing( Entity::getUniqueID ) ) );
		}
	}
	
	//package-private
	static void updateSleepingPlayers( MinecraftServer server ) {
		
		for( World world : server.getWorlds() ) {
			if( !ModConfig.getDimensions().contains( world.getDimension().getType() ) ) {
				continue;
			}
			TreeSet<PlayerEntity> sleeping_players = SLEEPING.get( world.getDimension().getType() );
			List<? extends PlayerEntity> world_players = world.getPlayers();
			int non_spectator_player_count = countNonSpectatorPlayers( world_players );
			for( PlayerEntity player : world_players ) {
				if( player.isSleeping() && !sleeping_players.contains( player ) ) {
					sleeping_players.add( player );
					sendSleepMessage( world, world_players, sleeping_players.size(), non_spectator_player_count,
						player );
				} else {
					if( !player.isSleeping() && sleeping_players.contains( player ) ) {
						sleeping_players.remove( player );
						sendWakeMessage( world, world_players, sleeping_players.size(), non_spectator_player_count,
							player );
					}
				}
			}
			int sleeping_percent = caculateSleepingPercent( countSleepingPlayers( sleeping_players ),
				non_spectator_player_count );
			if( sleeping_percent >= ModConfig.getSleepPercent() ||
				non_spectator_player_count > 0 &&
				non_spectator_player_count == sleeping_players.size() ) {
				if( world.getGameRules().getBoolean( GameRules.DO_DAYLIGHT_CYCLE ) ) {
					world.setDayTime( 0 );
				}
				sleeping_players.forEach( player -> {
					player.setSpawnPoint( player.getBedLocation( player.dimension ), false, false, player.dimension );
					player.wakeUp();
				} );
				if( world.getGameRules().getBoolean( GameRules.DO_WEATHER_CYCLE ) ) {
					world.getDimension().resetRainAndThunder();
				}
				sendMorningMessage( world, world_players );
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
	
	private static int countSleepingPlayers( TreeSet<PlayerEntity> players ) {
		
		int count = 0;
		for( PlayerEntity player : players ) {
			if( player.isPlayerFullyAsleep() ) {
				count++;
			}
		}
		return count;
	}
	
	private static void sendWakeMessage( World world, List<? extends PlayerEntity> players,
		int sleep_player_count, int non_spectator_player_count, PlayerEntity wake_player ) {
		
		sendMessage( world, players, buildWakeSleepMessage( wake_player, sleep_player_count,
			non_spectator_player_count, ModConfig.getWakeMessage() ) );
	}
	
	private static void sendSleepMessage( World world, List<? extends PlayerEntity> players,
		int sleep_player_count, int non_spectator_player_count, PlayerEntity wake_player ) {
		
		sendMessage( world, players, buildWakeSleepMessage( wake_player, sleep_player_count,
			non_spectator_player_count, ModConfig.getSleepMessage() ) );
	}
	
	private static void sendMorningMessage( World world, List<? extends PlayerEntity> players ) {
		
		sendMessage( world, players, new StringTextComponent( ModConfig.getMorningMessage() ) );
	}
	
	private static void sendMessage( World world, List<? extends PlayerEntity> players,
		ITextComponent message ) {
		
		for( PlayerEntity player : players ) {
			player.sendMessage( message );
		}
		world.getDimension().resetRainAndThunder();
	}
	
	private static ITextComponent buildWakeSleepMessage( PlayerEntity player, int sleep_player_count,
		int player_count, String message ) {
		
		return player.getDisplayName().appendText( " " ).appendText( message ).appendText( " - " )
			.appendText( String.valueOf( sleep_player_count ) ).appendText( "/" )
			.appendText( String.valueOf( player_count ) ).appendText( " (" )
			.appendText( String.valueOf( caculateSleepingPercent( sleep_player_count, player_count ) ) )
			.appendText( "%)" );
	}
	
	private static int caculateSleepingPercent( int sleep_player_count, int non_spectator_player_count ) {
		
		return non_spectator_player_count == 0 ? 0 : sleep_player_count * 100 / non_spectator_player_count;
	}
}
