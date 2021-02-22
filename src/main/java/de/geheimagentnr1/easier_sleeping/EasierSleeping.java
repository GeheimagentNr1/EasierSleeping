package de.geheimagentnr1.easier_sleeping;

import de.geheimagentnr1.easier_sleeping.config.ServerConfig;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;


@SuppressWarnings( "UtilityClassWithPublicConstructor" )
@Mod( EasierSleeping.MODID )
public class EasierSleeping {
	
	
	public static final String MODID = "easier_sleeping";
	
	public EasierSleeping() {
		
		ModLoadingContext.get().registerConfig( ModConfig.Type.SERVER, ServerConfig.CONFIG );
		ModLoadingContext.get().registerExtensionPoint(
			ExtensionPoint.DISPLAYTEST,
			() -> Pair.of( () -> FMLNetworkConstants.IGNORESERVERONLY, ( remote, isServer ) -> true )
		);
	}
}
