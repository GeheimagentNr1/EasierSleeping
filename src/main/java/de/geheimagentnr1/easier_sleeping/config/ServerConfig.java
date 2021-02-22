package de.geheimagentnr1.easier_sleeping.config;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;


public class ServerConfig {
	
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	private static final String MOD_NAME = ModLoadingContext.get().getActiveContainer().getModInfo().getDisplayName();
	
	private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	
	public static final ForgeConfigSpec CONFIG;
	
	private static final ForgeConfigSpec.IntValue SLEEP_PERCENT;
	
	private static final ForgeConfigSpec.ConfigValue<String> SLEEP_MESSAGE;
	
	private static final ForgeConfigSpec.ConfigValue<String> WAKE_MESSAGE;
	
	private static final ForgeConfigSpec.ConfigValue<String> MORNING_MESSAGE;
	
	private static final ForgeConfigSpec.BooleanValue ALL_PLAYERS_REST;
	
	private static final ForgeConfigSpec.ConfigValue<List<String>> DIMENSIONS;
	
	private static final ForgeConfigSpec.EnumValue<DimensionListType> DIMENSION_LIST_TYPE;
	
	private static final TreeSet<RegistryKey<World>> dimensions =
		new TreeSet<>( Comparator.comparing( RegistryKey::func_240901_a_ ) );
	
	static {
		
		SLEEP_PERCENT = BUILDER.comment( "Percentage of players required to skip the night." ).defineInRange(
			"sleep_percent",
			50,
			0,
			100
		);
		SLEEP_MESSAGE = BUILDER.comment( "Message shown, if a player goes to bed" ).define(
			"sleep_message",
			"is now in bed."
		);
		WAKE_MESSAGE = BUILDER.comment( "Message shown, if a player leaves his bed" ).define(
			"wake_message",
			"stood up."
		);
		MORNING_MESSAGE = BUILDER.comment( "Message shown, if the night was skipped" ).define(
			"morning_message",
			"Good Morning"
		);
		ALL_PLAYERS_REST = BUILDER.comment(
			"If true, the time since last rest is reset for all players, if enough other players are successfully " +
				"sleeping. So not every player has to sleep to prevent phantom spawning for him." )
			.define(
				"all_players_rest",
				false
			);
		DIMENSIONS = BUILDER.comment(
			"If dimension_list_type is set to SLEEP_ACTIVE, the list is the list of dimensions in which the sleep " +
				"voting is active." +
				System.lineSeparator() +
				"If dimension_list_type is set to SLEEP_INACTIVE, the list is the list of dimensions in which the " +
				"sleep voting is inactive." )
			.define( "dimensions", Collections.singletonList(
				Objects.requireNonNull( World.field_234918_g_.func_240901_a_() ).toString() ), o -> {
				if( o instanceof List<?> ) {
					List<?> list = (List<?>)o;
					return list.isEmpty() || list.get( 0 ) instanceof String;
				}
				return false;
			} );
		DIMENSION_LIST_TYPE = BUILDER.comment(
			"If dimension_list_type is set to SLEEP_ACTIVE, the dimension list is the list of dimensions in which " +
				"the sleep voting is active." +
				System.lineSeparator() +
				"If dimension_list_type is set to SLEEP_INACTIVE, the dimension list is the list of dimensions in " +
				"which the sleep voting is inactive." )
			.defineEnum( "dimension_list_type", DimensionListType.SLEEP_ACTIVE );
		
		CONFIG = BUILDER.build();
	}
	
	private static void printConfig() {
		
		LOGGER.info( "{} = {}", SLEEP_PERCENT.getPath(), SLEEP_PERCENT.get() );
		LOGGER.info( "{} = {}", SLEEP_MESSAGE.getPath(), SLEEP_MESSAGE.get() );
		LOGGER.info( "{} = {}", WAKE_MESSAGE.getPath(), WAKE_MESSAGE.get() );
		LOGGER.info( "{} = {}", MORNING_MESSAGE.getPath(), MORNING_MESSAGE.get() );
		LOGGER.info( "{} = {}", DIMENSIONS.getPath(), DIMENSIONS.get() );
		LOGGER.info( "{} = {}", DIMENSION_LIST_TYPE.getPath(), DIMENSION_LIST_TYPE.get() );
	}
	
	public static void printLoadedConfig() {
		
		LOGGER.info( "Loading \"{}\" Config", MOD_NAME );
		printConfig();
		LOGGER.info( "\"{}\" Config loaded", MOD_NAME );
	}
	
	public static synchronized void checkAndPrintConfig() {
		
		if( checkCorrectAndReadDimensions() ) {
			LOGGER.info( "\"{}\" Config corrected", MOD_NAME );
			printConfig();
		}
	}
	
	private static synchronized boolean checkCorrectAndReadDimensions() {
		
		ArrayList<String> read_dimensions = new ArrayList<>( DIMENSIONS.get() );
		
		dimensions.clear();
		for( String read_dimension : read_dimensions ) {
			ResourceLocation registry_name = ResourceLocation.tryCreate( read_dimension );
			if( registry_name != null ) {
				RegistryKey<World> registrykey = RegistryKey.func_240903_a_(
					Registry.field_239699_ae_,
					registry_name
				);
				ServerWorld serverworld = ServerLifecycleHooks.getCurrentServer().getWorld( registrykey );
				if( serverworld == null ) {
					LOGGER.warn( "Removed unknown dimension: {}", read_dimension );
				} else {
					dimensions.add( registrykey );
				}
			} else {
				LOGGER.warn( "Removed invalid dimension registry name {}", read_dimension );
			}
		}
		if( DIMENSIONS.get().size() != dimensions.size() ) {
			DIMENSIONS.set( dimensionsToRegistryNameList() );
			return true;
		}
		return false;
	}
	
	private static synchronized ArrayList<String> dimensionsToRegistryNameList() {
		
		ArrayList<String> registryNames = new ArrayList<>();
		
		for( RegistryKey<World> dimension : dimensions ) {
			registryNames.add( Objects.requireNonNull( dimension.func_240901_a_() ).toString() );
		}
		return registryNames;
	}
	
	public static synchronized void invertDimensions() {
		
		ArrayList<String> newDimensionRegistryNames = new ArrayList<>();
		
		for( ServerWorld serverworld : ServerLifecycleHooks.getCurrentServer().getWorlds() ) {
			RegistryKey<World> registrykey = serverworld.func_234923_W_();
			if( !dimensions.contains( registrykey ) ) {
				newDimensionRegistryNames.add(
					Objects.requireNonNull( registrykey.func_240901_a_() ).toString() );
			}
		}
		newDimensionRegistryNames.sort( String::compareTo );
		DIMENSIONS.set( newDimensionRegistryNames );
		checkAndPrintConfig();
	}
	
	public static int getSleepPercent() {
		
		return SLEEP_PERCENT.get();
	}
	
	public static void setSleepPercent( int sleep_percent ) {
		
		SLEEP_PERCENT.set( sleep_percent );
	}
	
	public static String getSleepMessage() {
		
		return SLEEP_MESSAGE.get();
	}
	
	public static void setSleepMessage( String message ) {
		
		SLEEP_MESSAGE.set( message );
	}
	
	public static String getWakeMessage() {
		
		return WAKE_MESSAGE.get();
	}
	
	public static void setWakeMessage( String message ) {
		
		WAKE_MESSAGE.set( message );
	}
	
	public static String getMorningMessage() {
		
		return MORNING_MESSAGE.get();
	}
	
	public static void setMorningMessage( String message ) {
		
		MORNING_MESSAGE.set( message );
	}
	
	public static boolean getAllPlayersRest() {
		
		return ALL_PLAYERS_REST.get();
	}
	
	public static void setAllPlayersRest( boolean all_player_rest ) {
		
		ALL_PLAYERS_REST.set( all_player_rest );
	}
	
	public static TreeSet<RegistryKey<World>> getDimensions() {
		
		return dimensions;
	}
	
	public static synchronized void addDimension( RegistryKey<World> dimension ) {
		
		if( !dimensions.contains( dimension ) ) {
			dimensions.add( dimension );
			DIMENSIONS.set( dimensionsToRegistryNameList() );
		}
	}
	
	public static synchronized void removeDimension( RegistryKey<World> dimension ) {
		
		if( dimensions.contains( dimension ) ) {
			dimensions.remove( dimension );
			DIMENSIONS.set( dimensionsToRegistryNameList() );
		}
	}
	
	public static DimensionListType getDimensionListType() {
		
		return DIMENSION_LIST_TYPE.get();
	}
	
	public static void setDimensionListType( DimensionListType dimensionListType ) {
		
		DIMENSION_LIST_TYPE.set( dimensionListType );
	}
}
