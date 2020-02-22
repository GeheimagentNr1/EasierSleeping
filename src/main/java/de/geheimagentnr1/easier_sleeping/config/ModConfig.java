package de.geheimagentnr1.easier_sleeping.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import de.geheimagentnr1.easier_sleeping.EasierSleeping;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;


public class ModConfig {
	
	
	private final static Logger LOGGER = LogManager.getLogger();
	
	private final static String mod_name = "Many Ideas Core";
	
	private final static ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	
	private final static ForgeConfigSpec CONFIG;
	
	public final static ForgeConfigSpec.IntValue SLEEP_PERCENT;
	
	public final static ForgeConfigSpec.ConfigValue<String> SLEEP_MESSAGE;
	
	public final static ForgeConfigSpec.ConfigValue<String> WAKE_MESSAGE;
	
	public final static ForgeConfigSpec.ConfigValue<String> MORNING_MESSAGE;
	
	public final static ForgeConfigSpec.ConfigValue<List<Integer>> DIMENSIONS;
	
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
			.define( "dimensions", Collections.singletonList( 0 ), o -> {
				if( o instanceof List<?> ) {
					List<?> list = (List<?>)o;
					return list.isEmpty() || list.get( 0 ) instanceof Integer;
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
		LOGGER.info( "{} = {}", SLEEP_PERCENT.getPath(), SLEEP_PERCENT.get() );
		LOGGER.info( "{} = {}", SLEEP_MESSAGE.getPath(), SLEEP_MESSAGE.get() );
		LOGGER.info( "{} = {}", WAKE_MESSAGE.getPath(), WAKE_MESSAGE.get() );
		LOGGER.info( "{} = {}", MORNING_MESSAGE.getPath(), MORNING_MESSAGE.get() );
		LOGGER.info( "{} = {}", DIMENSIONS.getPath(), DIMENSIONS.get() );
		LOGGER.info( "\"{}\" Config loaded", mod_name );
	}
}
