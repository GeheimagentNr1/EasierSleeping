package de.geheimagentnr1.easier_sleeping.config;

import de.geheimagentnr1.easier_sleeping.EasierSleeping;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;


@Log4j2
public class ServerConfig {
	
	
	@Getter
	@NotNull
	private final ModConfigSpec configSpec;
	
	@NotNull
	private final ModConfigSpec.IntValue sleepPercent;
	
	@NotNull
	private final ModConfigSpec.ConfigValue<List<? extends String>> sleepMessages;
	
	@NotNull
	private final ModConfigSpec.ConfigValue<List<? extends String>> wakeMessages;
	
	@NotNull
	private final ModConfigSpec.ConfigValue<List<? extends String>> morningMessages;
	
	@NotNull
	private final ModConfigSpec.BooleanValue allPlayersRest;
	
	@NotNull
	private final ModConfigSpec.ConfigValue<List<? extends String>> dimensionsConfig;
	
	@NotNull
	private final ModConfigSpec.EnumValue<DimensionListType> dimensionListType;
	
	@NotNull
	private final ModConfigSpec.ConfigValue<List<? extends String>> blockBlacklistConfig;
	
	@NotNull
	private final TreeSet<ResourceKey<Level>> dimensions =
		new TreeSet<>( Comparator.comparing( ResourceKey::location ) );
	
	@NotNull
	private final TreeSet<ResourceLocation> blockBlacklist = new TreeSet<>();
	
	public ServerConfig() {
		
		ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
		
		builder.comment( "Percentage of players required to skip the night." );
		sleepPercent = builder.defineInRange( "sleep_percent", 50, 0, 100 );
		
		builder.comment(
			"List of messages, from which one will be shown, when a player goes to bed",
			"(Available parameters: %player% = Player name)"
		);
		sleepMessages = builder.defineListAllowEmpty(
			List.of( "sleep_messages" ),
			() -> defaultSleepingMessages(),
			() -> "",
			obj -> obj instanceof String
		);
		
		builder.comment(
			"List of messages, from which one will be shown, when a player leaves his bed",
			"(Available parameters: %player% = Player name)"
		);
		wakeMessages = builder.defineListAllowEmpty(
			List.of( "wake_messages" ),
			() -> defaultWakeMessages(),
			() -> "",
			obj -> obj instanceof String
		);
		
		builder.comment( "List of messages, from which one will be shown, when the night was skipped" );
		morningMessages = builder.defineListAllowEmpty(
			List.of( "morning_messages" ),
			() -> defaultMorningMessages(),
			() -> "",
			obj -> obj instanceof String
		);
		
		builder.comment(
			"If true, the time since last rest is reset for all players, if enough other players are " +
				"successfully sleeping. So not every player has to sleep to prevent phantom spawning for him."
		);
		allPlayersRest = builder.define( "all_players_rest", false );
		
		builder.comment(
			"If dimension_list_type is set to SLEEP_ACTIVE, the list is the list of dimensions in which the " +
				"sleep voting is active.",
			"If dimension_list_type is set to SLEEP_INACTIVE, the list is the list of dimensions in which the " +
				"sleep voting is inactive."
		);
		dimensionsConfig = builder.defineListAllowEmpty(
			List.of( "dimensions" ),
			() -> Collections.singletonList( Objects.requireNonNull( Level.OVERWORLD.location() ).toString() ),
			() -> "",
			obj -> obj instanceof String
		);
		
		builder.comment(
			"If dimension_list_type is set to SLEEP_ACTIVE, the dimension list is the list of dimensions in " +
				"which the sleep voting is active.",
			"If dimension_list_type is set to SLEEP_INACTIVE, the dimension list is the list of dimensions in " +
				"which the sleep voting is inactive."
		);
		dimensionListType = builder.defineEnum( "dimension_list_type", DimensionListType.SLEEP_ACTIVE );
		
		builder.comment( "Block names of beds being ignored for sleep percentage." );
		blockBlacklistConfig = builder.defineListAllowEmpty(
			List.of( "block_blacklist" ),
			() -> List.of(),
			() -> "",
			obj -> obj instanceof String
		);
		
		configSpec = builder.build();
	}
	
	private List<String> defaultSleepingMessages() {
		
		return List.of( "%player% is now in bed." );
	}
	
	private List<String> defaultWakeMessages() {
		
		return List.of( "%player% stood up." );
	}
	
	private List<String> defaultMorningMessages() {
		
		return List.of( "Good Morning" );
	}
	
	public void onServerStarting() {
		
		checkConfig();
	}
	
	private synchronized void checkConfig() {
		
		boolean areDimensionCorrected = checkCorrectAndReadDimensions();
		boolean areBlocksOfBlacklistCorrected = checkCorrectAndReadBlockBlacklist();
		if( areDimensionCorrected || areBlocksOfBlacklistCorrected ) {
			log.info( "\"{}\" Server Config corrected", EasierSleeping.MODID );
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
		
		return sleepPercent.get();
	}
	
	public void setSleepPercent( int sleep_percent ) {
		
		sleepPercent.set( sleep_percent );
	}
	
	@NotNull
	@SuppressWarnings( "unchecked" )
	public List<String> getSleepMessages() {
		
		return new ArrayList<>( (List<String>) (List<?>) sleepMessages.get() );
	}
	
	@NotNull
	public List<String> getSleepMessagesOrDefault() {
		
		List<String> messages = getSleepMessages();
		if( messages.isEmpty() ) {
			return defaultSleepingMessages();
		}
		return messages;
	}
	
	private List<String> distinctMessages( List<String> messages ) {
		
		return messages.stream().distinct().collect( Collectors.toList() );
	}
	
	public void setSleepMessages( @NotNull List<String> messages ) {
		
		sleepMessages.set( distinctMessages( messages ) );
	}
	
	@NotNull
	@SuppressWarnings( "unchecked" )
	public List<String> getWakeMessages() {
		
		return new ArrayList<>( (List<String>) (List<?>) wakeMessages.get() );
	}
	
	@NotNull
	public List<String> getWakeMessagesOrDefault() {
		
		List<String> messages = getWakeMessages();
		if( messages.isEmpty() ) {
			return defaultWakeMessages();
		}
		return messages;
	}
	
	public void setWakeMessages( @NotNull List<String> messages ) {
		
		wakeMessages.set( distinctMessages( messages ) );
	}
	
	@NotNull
	@SuppressWarnings( "unchecked" )
	public List<String> getMorningMessages() {
		
		return new ArrayList<>( (List<String>) (List<?>) morningMessages.get() );
	}
	
	@NotNull
	public List<String> getMorningMessagesOrDefault() {
		
		List<String> messages = getMorningMessages();
		if( messages.isEmpty() ) {
			return defaultMorningMessages();
		}
		return messages;
	}
	
	public void setMorningMessages( @NotNull List<String> messages ) {
		
		morningMessages.set( distinctMessages( messages ) );
	}
	
	public boolean getAllPlayersRest() {
		
		return allPlayersRest.get();
	}
	
	public void setAllPlayersRest( boolean all_player_rest ) {
		
		allPlayersRest.set( all_player_rest );
	}
	
	@NotNull
	@SuppressWarnings( "unchecked" )
	private List<String> getDimensionsValue() {
		
		return new ArrayList<>( (List<String>) (List<?>) dimensionsConfig.get() );
	}
	
	private void setDimensionsValue( @NotNull List<String> dimensionsValue ) {
		
		dimensionsConfig.set( dimensionsValue );
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
		
		return dimensionListType.get();
	}
	
	public void setDimensionListType( @NotNull DimensionListType _dimensionListType ) {
		
		dimensionListType.set( _dimensionListType );
	}
	
	@NotNull
	@SuppressWarnings( "unchecked" )
	private List<String> getBlockBlacklist() {
		
		return new ArrayList<>( (List<String>) (List<?>) blockBlacklistConfig.get() );
	}
	
	private void setBlockBlacklist( @NotNull List<String> _blockBlacklist ) {
		
		blockBlacklistConfig.set( _blockBlacklist );
	}
	
	@NotNull
	public TreeSet<ResourceLocation> getIgnoredBedBlocks() {
		
		return blockBlacklist;
	}
}
