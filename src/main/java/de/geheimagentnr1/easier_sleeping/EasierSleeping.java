package de.geheimagentnr1.easier_sleeping;

import de.geheimagentnr1.easier_sleeping.config.ServerConfig;
import de.geheimagentnr1.easier_sleeping.elements.commands.sleep.DimensionListTypeArgument;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;


@SuppressWarnings( "UtilityClassWithPublicConstructor" )
@Mod( EasierSleeping.MODID )
public class EasierSleeping {
	
	
	public static final String MODID = "easier_sleeping";
	
	private static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister.create(
		Registries.COMMAND_ARGUMENT_TYPE,
		MODID
	);
	
	private static final RegistryObject<SingletonArgumentInfo<DimensionListTypeArgument>>
		DIMENSION_LIST_TYPE_COMMAND_ARGUMENT_TYPE =
		COMMAND_ARGUMENT_TYPES.register(
			DimensionListTypeArgument.registry_name,
			() -> ArgumentTypeInfos.registerByClass(
				DimensionListTypeArgument.class,
				SingletonArgumentInfo.contextFree( DimensionListTypeArgument::dimensionListType )
			)
		);
	
	public EasierSleeping() {
		
		ModLoadingContext.get().registerConfig( ModConfig.Type.SERVER, ServerConfig.CONFIG );
		COMMAND_ARGUMENT_TYPES.register( FMLJavaModLoadingContext.get().getModEventBus() );
	}
}
