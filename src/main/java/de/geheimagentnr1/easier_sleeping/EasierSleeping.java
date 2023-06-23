package de.geheimagentnr1.easier_sleeping;

import de.geheimagentnr1.easier_sleeping.config.ServerConfig;
import de.geheimagentnr1.easier_sleeping.elements.commands.ModArgumentTypesRegisterFactory;
import de.geheimagentnr1.easier_sleeping.elements.commands.ModCommandsRegisterFactory;
import de.geheimagentnr1.easier_sleeping.sleeping.SleepingManager;
import de.geheimagentnr1.minecraft_forge_api.AbstractMod;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;


@Mod( EasierSleeping.MODID )
public class EasierSleeping extends AbstractMod {
	
	
	@NotNull
	static final String MODID = "easier_sleeping";
	
	@NotNull
	@Override
	public String getModId() {
		
		return MODID;
	}
	
	@Override
	protected void initMod() {
		
		ServerConfig serverConfig = registerConfig( ServerConfig::new );
		registerEventHandler( new ModArgumentTypesRegisterFactory() );
		registerEventHandler( new ModCommandsRegisterFactory( serverConfig ) );
		registerEventHandler( new SleepingManager( serverConfig ) );
	}
}
