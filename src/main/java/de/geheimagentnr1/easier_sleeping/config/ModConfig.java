package de.geheimagentnr1.easier_sleeping.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import de.geheimagentnr1.easier_sleeping.EasierSleeping;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;


public class ModConfig {
	
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	private static final String mod_name = "Easier Sleeping";
	
	private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	
	private static final ForgeConfigSpec CONFIG;
	
	private static final ForgeConfigSpec.IntValue SLEEP_PERCENT;
	
	private static final ForgeConfigSpec.ConfigValue<String> SLEEP_MESSAGE;
	
	private static final ForgeConfigSpec.ConfigValue<String> WAKE_MESSAGE;
	
	private static final ForgeConfigSpec.ConfigValue<String> MORNING_MESSAGE;
	
	private static final ForgeConfigSpec.ConfigValue<List<String>> DIMENSIONS;
	
	private static final TreeSet<DimensionType> dimensions = new TreeSet<>(
		Comparator.comparingInt( DimensionType::getId ) );
	
	static {
		
		SLEEP_PERCENT = BUILDER.comment( "Percentage of players required to skip the night." )
			.defineInRange( "sleep_percent", 50, 0, 100 );
		SLEEP_MESSAGE = BUILDER.comment( "Message shown, if a player goes to bed" )
			.define( "sleep_message", "is now in bed." );
		WAKE_MESSAGE = BUILDER.comment( "Message shown, if a player leaves his bed" )
			.define( "wake_message", "stood up." );
		MORNING_MESSAGE = BUILDER.comment( "Message shown, if the night was skipped" )
			.define( "morning_message", "Good Morning" );
		DIMENSIONS = BUILDER.comment( "Dimensions in which, the sleeping percentage is activ." )
			.define( "dimensions", Collections.singletonList(
				Objects.requireNonNull( DimensionType.OVERWORLD.getRegistryName() ).toString() ), o -> {
				if( o instanceof List<?> ) {
					List<?> list = (List<?>)o;
					return list.isEmpty() || list.get( 0 ) instanceof String;
				}
				return false;
			} );
		
		CONFIG = BUILDER.build();
	}
	
	public static void load() {
		
		CommentedFileConfig configData = CommentedFileConfig.builder( FMLPaths.CONFIGDIR.get().resolve(
			EasierSleeping.MODID + ".toml" ) ).sync().autosave().writingMode( WritingMode.REPLACE ).build();
		
		LOGGER.info( "Loading \"{}\" Config", mod_name );
		configData.load();
		CONFIG.setConfig( configData );
		checkCorrectAndReadDimensions();
		LOGGER.info( "{} = {}", SLEEP_PERCENT.getPath(), SLEEP_PERCENT.get() );
		LOGGER.info( "{} = {}", SLEEP_MESSAGE.getPath(), SLEEP_MESSAGE.get() );
		LOGGER.info( "{} = {}", WAKE_MESSAGE.getPath(), WAKE_MESSAGE.get() );
		LOGGER.info( "{} = {}", MORNING_MESSAGE.getPath(), MORNING_MESSAGE.get() );
		LOGGER.info( "{} = {}", DIMENSIONS.getPath(), DIMENSIONS.get() );
		LOGGER.info( "\"{}\" Config loaded", mod_name );
	}
	
	private static void checkCorrectAndReadDimensions() {
		
		ArrayList<String> read_dimensions = new ArrayList<>( DIMENSIONS.get() );
		
		dimensions.clear();
		for( String read_dimension : read_dimensions ) {
			ResourceLocation registry_name = ResourceLocation.tryCreate( read_dimension );
			if( registry_name != null ) {
				DimensionType dimension = DimensionType.byName( registry_name );
				if( dimension == null ) {
					LOGGER.warn( "Removed unknown dimension: {}", read_dimension );
				} else {
					dimensions.add( dimension );
				}
			} else {
				LOGGER.warn( "Removed invalid dimension registry name {}", read_dimension );
			}
		}
		if( DIMENSIONS.get().size() != dimensions.size() ) {
			DIMENSIONS.set( dimensionsToRegistryNameList() );
		}
	}
	
	private static ArrayList<String> dimensionsToRegistryNameList() {
		
		ArrayList<String> registryNames = new ArrayList<>();
		
		for( DimensionType dimension : dimensions ) {
			registryNames.add( Objects.requireNonNull( dimension.getRegistryName() ).toString() );
		}
		return registryNames;
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
	
	public static TreeSet<DimensionType> getDimensions() {
		
		return dimensions;
	}
	
	public static void addDimension( DimensionType dimension ) {
		
		if( !dimensions.contains( dimension ) ) {
			dimensions.add( dimension );
			DIMENSIONS.set( dimensionsToRegistryNameList() );
		}
	}
	
	public static void removeDimension( DimensionType dimension ) {
		
		if( dimensions.contains( dimension ) ) {
			dimensions.remove( dimension );
			DIMENSIONS.set( dimensionsToRegistryNameList() );
		}
	}
}
