package de.geheimagentnr1.easier_sleeping.config;

import de.geheimagentnr1.minecraft_forge_api.AbstractMod;
import de.geheimagentnr1.minecraft_forge_api.config.AbstractConfig;
import lombok.extern.log4j.Log4j2;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import java.util.*;


@Log4j2
public class ServerConfig extends AbstractConfig {
	
	
	@NotNull
	private static final String SLEEP_PERCENT_KEY = "sleep_percent";
	
	@NotNull
	private static final String SLEEP_MESSAGE_KEY = "sleep_message";
	
	@NotNull
	private static final String WAKE_MESSAGE_KEY = "wake_message";
	
	@NotNull
	private static final String MORNING_MESSAGE_KEY = "morning_message";
	
	@NotNull
	private static final String ALL_PLAYERS_REST_KEY = "all_players_rest";
	
	@NotNull
	private static final String DIMENSIONS_KEY = "dimensions";
	
	@NotNull
	private static final String DIMENSION_LIST_TYPE_KEY = "dimension_list_type";
	
	@NotNull
	private static final String BLOCK_BLACKLIST_KEY = "block_blacklist";
	
	@NotNull
	private final TreeSet<ResourceKey<Level>> dimensions =
		new TreeSet<>( Comparator.comparing( ResourceKey::location ) );
	
	@NotNull
	private final TreeSet<ResourceLocation> blockBlacklist = new TreeSet<>();
	
	public ServerConfig( @NotNull AbstractMod _abstractMod ) {
		
		super( _abstractMod );
	}
	
	@NotNull
	@Override
	public ModConfig.Type type() {
		
		return ModConfig.Type.SERVER;
	}
	
	@Override
	public boolean isEarlyLoad() {
		
		return false;
	}
	
	@Override
	protected void registerConfigValues() {
		
		registerConfigValue(
			"Percentage of players required to skip the night.",
			SLEEP_PERCENT_KEY,
			( builder, path ) -> builder.defineInRange( path, 50, 0, 100 )
		);
		registerConfigValue( "Message shown, if a player goes to bed", SLEEP_MESSAGE_KEY, "is now in bed." );
		registerConfigValue( "Message shown, if a player leaves his bed", WAKE_MESSAGE_KEY, "stood up." );
		registerConfigValue( "Message shown, if the night was skipped", MORNING_MESSAGE_KEY, "Good Morning" );
		registerConfigValue(
			"If true, the time since last rest is reset for all players, if enough other players are " +
				"successfully sleeping. So not every player has to sleep to prevent phantom spawning for him.",
			ALL_PLAYERS_REST_KEY,
			false
		);
		registerConfigValue(
			List.of(
				"If dimension_list_type is set to SLEEP_ACTIVE, the list is the list of dimensions in which the " +
					"sleep voting is active.",
				"If dimension_list_type is set to SLEEP_INACTIVE, the list is the list of dimensions in which the " +
					"sleep voting is inactive."
			),
			DIMENSIONS_KEY,
			( builder, path ) -> builder.define(
				path,
				Collections.singletonList( Objects.requireNonNull( Level.OVERWORLD.location() ).toString() ),
				o -> {
					if( o instanceof List<?> list ) {
						return list.isEmpty() || list.get( 0 ) instanceof String;
					}
					return false;
				}
			)
		);
		registerConfigValue(
			List.of(
				"If dimension_list_type is set to SLEEP_ACTIVE, the dimension list is the list of dimensions in " +
					"which the sleep voting is active.",
				"If dimension_list_type is set to SLEEP_INACTIVE, the dimension list is the list of dimensions in " +
					"which the sleep voting is inactive."
			),
			DIMENSION_LIST_TYPE_KEY,
			( builder, path ) -> builder.defineEnum( path, DimensionListType.SLEEP_ACTIVE )
		);
		registerConfigValue(
			"Block names of beds being ignored for sleep percentage.",
			BLOCK_BLACKLIST_KEY,
			( builder, path ) -> builder.define(
				path,
				List.of(),
				o -> {
					if( o instanceof List<?> list ) {
						return list.isEmpty() || list.get( 0 ) instanceof String;
					}
					return false;
				}
			)
		);
	}
	
	@Override
	public void handleServerStartingEvent( @NotNull ServerStartingEvent event ) {
		
		super.handleServerStartingEvent( event );
		checkConfig();
	}
	
	private synchronized void checkConfig() {
		
		boolean areDimensionCorrected = checkCorrectAndReadDimensions();
		boolean areBlocksOfBlacklistCorrected = checkCorrectAndReadBlockBlacklist();
		if( areDimensionCorrected || areBlocksOfBlacklistCorrected ) {
			log.info( "\"{}\" Server Config corrected", abstractMod.getModName() );
			printValues();
		}
	}
	
	private synchronized boolean checkCorrectAndReadDimensions() {
		
		ArrayList<String> read_dimensions = new ArrayList<>( getDimensionsValue() );
		
		dimensions.clear();
		for( String read_dimension : read_dimensions ) {
			ResourceLocation registry_name = ResourceLocation.tryParse( read_dimension );
			if( registry_name != null ) {
				ResourceKey<Level> registrykey = ResourceKey.create( Registries.DIMENSION, registry_name );
				ServerLevel serverLevel = ServerLifecycleHooks.getCurrentServer().getLevel( registrykey );
				if( serverLevel == null ) {
					log.warn( "Removed unknown dimension: {}", read_dimension );
				} else {
					dimensions.add( registrykey );
				}
			} else {
				log.warn( "Removed invalid dimension registry name {}", read_dimension );
			}
		}
		if( getDimensionsValue().size() != dimensions.size() ) {
			setDimensionsValue( dimensionsToRegistryNameList() );
			return true;
		}
		return false;
	}
	
	@NotNull
	private synchronized ArrayList<String> dimensionsToRegistryNameList() {
		
		ArrayList<String> registryNames = new ArrayList<>();
		
		for( ResourceKey<Level> dimension : dimensions ) {
			registryNames.add( Objects.requireNonNull( dimension.location() ).toString() );
		}
		return registryNames;
	}
	
	public synchronized void invertDimensions() {
		
		ArrayList<String> newDimensionRegistryNames = new ArrayList<>();
		
		for( ServerLevel serverLevel : ServerLifecycleHooks.getCurrentServer().getAllLevels() ) {
			ResourceKey<Level> registrykey = serverLevel.dimension();
			if( !dimensions.contains( registrykey ) ) {
				newDimensionRegistryNames.add( Objects.requireNonNull( registrykey.location() ).toString() );
				
			}
		}
		newDimensionRegistryNames.sort( String::compareTo );
		setDimensionsValue( newDimensionRegistryNames );
		checkConfig();
	}
	
	private synchronized boolean checkCorrectAndReadBlockBlacklist() {
		
		ArrayList<String> block_blacklist = new ArrayList<>( getBlockBlacklist() );
		
		blockBlacklist.clear();
		for( String block : block_blacklist ) {
			ResourceLocation registry_name = ResourceLocation.tryParse( block );
			if( registry_name != null ) {
				if( BuiltInRegistries.BLOCK.getOptional( registry_name ).isPresent() ) {
					blockBlacklist.add( registry_name );
				} else {
					log.warn( "Removed unknown block: {}", block );
				}
			} else {
				log.warn( "Removed invalid block registry name {}", block );
			}
		}
		if( getBlockBlacklist().size() != blockBlacklist.size() ) {
			setBlockBlacklist( blockBlacklistToRegistryNameList() );
			return true;
		}
		return false;
	}
	
	@NotNull
	private synchronized ArrayList<String> blockBlacklistToRegistryNameList() {
		
		ArrayList<String> registryNames = new ArrayList<>();
		
		for( ResourceLocation block : blockBlacklist ) {
			registryNames.add( Objects.requireNonNull( block ).toString() );
		}
		return registryNames;
	}
	
	public int getSleepPercent() {
		
		return getValue( Integer.class, SLEEP_PERCENT_KEY );
	}
	
	public void setSleepPercent( int sleep_percent ) {
		
		setValue( Integer.class, SLEEP_PERCENT_KEY, sleep_percent );
	}
	
	@NotNull
	public String getSleepMessage() {
		
		return getValue( String.class, SLEEP_MESSAGE_KEY );
	}
	
	public void setSleepMessage( @NotNull String message ) {
		
		setValue( String.class, SLEEP_MESSAGE_KEY, message );
	}
	
	@NotNull
	public String getWakeMessage() {
		
		return getValue( String.class, WAKE_MESSAGE_KEY );
	}
	
	public void setWakeMessage( @NotNull String message ) {
		
		setValue( String.class, WAKE_MESSAGE_KEY, message );
	}
	
	@NotNull
	public String getMorningMessage() {
		
		return getValue( String.class, MORNING_MESSAGE_KEY );
	}
	
	public void setMorningMessage( @NotNull String message ) {
		
		setValue( String.class, MORNING_MESSAGE_KEY, message );
	}
	
	public boolean getAllPlayersRest() {
		
		return getValue( Boolean.class, ALL_PLAYERS_REST_KEY );
	}
	
	public void setAllPlayersRest( boolean all_player_rest ) {
		
		setValue( Boolean.class, ALL_PLAYERS_REST_KEY, all_player_rest );
	}
	
	@NotNull
	private List<String> getDimensionsValue() {
		
		return getListValue( String.class, DIMENSIONS_KEY );
	}
	
	private void setDimensionsValue( @NotNull List<String> dimensionsValue ) {
		
		setValue( List.class, BLOCK_BLACKLIST_KEY, dimensionsValue );
	}
	
	@NotNull
	public TreeSet<ResourceKey<Level>> getDimensions() {
		
		return dimensions;
	}
	
	public synchronized void addDimension( @NotNull ResourceKey<Level> dimension ) {
		
		if( !dimensions.contains( dimension ) ) {
			dimensions.add( dimension );
			setDimensionsValue( dimensionsToRegistryNameList() );
		}
	}
	
	public synchronized void removeDimension( @NotNull ResourceKey<Level> dimension ) {
		
		if( dimensions.contains( dimension ) ) {
			dimensions.remove( dimension );
			setDimensionsValue( dimensionsToRegistryNameList() );
		}
	}
	
	@NotNull
	public DimensionListType getDimensionListType() {
		
		return getValue( DimensionListType.class, DIMENSION_LIST_TYPE_KEY );
	}
	
	public void setDimensionListType( @NotNull DimensionListType dimensionListType ) {
		
		setValue( DimensionListType.class, DIMENSION_LIST_TYPE_KEY, dimensionListType );
	}
	
	@NotNull
	private List<String> getBlockBlacklist() {
		
		return getListValue( String.class, BLOCK_BLACKLIST_KEY );
	}
	
	private void setBlockBlacklist( @NotNull List<String> _blockBlacklist ) {
		
		setValue( List.class, BLOCK_BLACKLIST_KEY, _blockBlacklist );
	}
	
	@NotNull
	public TreeSet<ResourceLocation> getIgnoredBedBlocks() {
		
		return blockBlacklist;
	}
}
