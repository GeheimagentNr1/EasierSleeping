package de.geheimagentnr1.easier_sleeping;

import de.geheimagentnr1.easier_sleeping.config.MainConfig;
import de.geheimagentnr1.easier_sleeping.elements.commands.ModArgumentTypes;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;


@SuppressWarnings( { "UtilityClassWithPublicConstructor", "unused" } )
@Mod( EasierSleeping.MODID )
public class EasierSleeping {
	
	
	public static final String MODID = "easier_sleeping";
	
	public EasierSleeping() {
		
		ModArgumentTypes.registerArgumentTypes();
		ModLoadingContext.get().registerConfig( ModConfig.Type.COMMON, MainConfig.CONFIG, MODID + ".toml" );
	}
}
