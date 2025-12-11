package de.geheimagentnr1.easier_sleeping;

import de.geheimagentnr1.easier_sleeping.config.ServerConfig;
import de.geheimagentnr1.easier_sleeping.elements.commands.ModArgumentTypesRegisterFactory;
import de.geheimagentnr1.easier_sleeping.elements.commands.ModCommandsRegisterFactory;
import de.geheimagentnr1.easier_sleeping.sleeping.SleepingManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;


@Mod( EasierSleeping.MODID )
public class EasierSleeping {
	
	
	@NotNull
	public static final String MODID = "easier_sleeping";
	
	public EasierSleeping( @NotNull IEventBus modEventBus, @NotNull ModContainer modContainer ) {
		
		ServerConfig serverConfig = new ServerConfig();
		modContainer.registerConfig( ModConfig.Type.SERVER, serverConfig.getConfigSpec() );
		
		ModArgumentTypesRegisterFactory.register( modEventBus );
		
		NeoForge.EVENT_BUS.register( new ModCommandsRegisterFactory( serverConfig ) );
		NeoForge.EVENT_BUS.register( new SleepingManager( serverConfig ) );
	}
}
