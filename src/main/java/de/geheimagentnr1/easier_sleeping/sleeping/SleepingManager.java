package de.geheimagentnr1.easier_sleeping.sleeping;

import de.geheimagentnr1.easier_sleeping.config.MainConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;


public class SleepingManager {
	
	
	private static TreeMap<RegistryKey<World>, TreeSet<ServerPlayerEntity>> SLEEPING;
	
	public static void init() {
		
		SLEEPING = new TreeMap<>( Comparator.comparing( RegistryKey::func_240901_a_ ) );
		for( World world : ServerLifecycleHooks.getCurrentServer().getWorlds() ) {
			SLEEPING.put( world.func_234923_W_(), new TreeSet<>( Comparator.comparing( Entity::getUniqueID ) ) );
		}
	}
	
	//package-private
	static void updateSleepingPlayers( MinecraftServer server ) {
		
		for( ServerWorld world : server.getWorlds() ) {
			if( !MainConfig.getDimensions().contains( world.func_234923_W_() ) ) {
				continue;
			}
			TreeSet<ServerPlayerEntity> sleeping_players = SLEEPING.get( world.func_234923_W_() );
			List<ServerPlayerEntity> world_players = world.getPlayers();
			int non_spectator_player_count = countNonSpectatorPlayers( world_players );
			for( ServerPlayerEntity player : world_players ) {
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
			if( sleeping_percent >= MainConfig.getSleepPercent() ||
				non_spectator_player_count > 0 &&
					non_spectator_player_count == sleeping_players.size() ) {
				if( world.getGameRules().getBoolean( GameRules.DO_DAYLIGHT_CYCLE ) ) {
					world.func_241114_a_( 0 );
				}
				sleeping_players.forEach( player -> {
					player.getBedPosition().ifPresent( pos ->
						player.func_242111_a( world.func_234923_W_(), pos, player.rotationYaw, false, false ) );
					player.wakeUp();
				} );
				if( world.getGameRules().getBoolean( GameRules.DO_WEATHER_CYCLE ) ) {
					world.func_241113_a_( 6000, 0, false, false );
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
	
	private static int countSleepingPlayers( TreeSet<ServerPlayerEntity> players ) {
		
		int count = 0;
		for( ServerPlayerEntity player : players ) {
			if( player.isPlayerFullyAsleep() ) {
				count++;
			}
		}
		return count;
	}
	
	private static void sendWakeMessage( ServerWorld world, List<? extends PlayerEntity> players,
		int sleep_player_count, int non_spectator_player_count, PlayerEntity wake_player ) {
		
		sendMessage( world, players, buildWakeSleepMessage( wake_player, sleep_player_count,
			non_spectator_player_count, MainConfig.getWakeMessage() ) );
	}
	
	private static void sendSleepMessage( ServerWorld world, List<? extends PlayerEntity> players,
		int sleep_player_count, int non_spectator_player_count, PlayerEntity wake_player ) {
		
		sendMessage( world, players, buildWakeSleepMessage( wake_player, sleep_player_count,
			non_spectator_player_count, MainConfig.getSleepMessage() ) );
	}
	
	private static void sendMorningMessage( ServerWorld world, List<? extends PlayerEntity> players ) {
		
		sendMessage( world, players, new StringTextComponent( MainConfig.getMorningMessage() ) );
	}
	
	private static void sendMessage( ServerWorld world, List<? extends PlayerEntity> players,
		ITextComponent message ) {
		
		for( PlayerEntity player : players ) {
			player.sendMessage( message, Util.field_240973_b_ );
		}
		world.func_241113_a_( 6000, 0, false, false );
	}
	
	private static ITextComponent buildWakeSleepMessage( PlayerEntity player, int sleep_player_count,
		int player_count, String message ) {
		
		return new StringTextComponent( "" ).func_230529_a_( player.getDisplayName() )
			.func_240702_b_( " " ).func_240702_b_( message ).func_240702_b_( " - " )
			.func_240702_b_( String.valueOf( sleep_player_count ) ).func_240702_b_( "/" )
			.func_240702_b_( String.valueOf( player_count ) ).func_240702_b_( " (" )
			.func_240702_b_( String.valueOf( caculateSleepingPercent( sleep_player_count, player_count ) ) )
			.func_240702_b_( "%)" );
	}
	
	private static int caculateSleepingPercent( int sleep_player_count, int non_spectator_player_count ) {
		
		return non_spectator_player_count == 0 ? 0 : sleep_player_count * 100 / non_spectator_player_count;
	}
}
