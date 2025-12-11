package de.geheimagentnr1.easier_sleeping.elements.commands;

import de.geheimagentnr1.easier_sleeping.EasierSleeping;
import de.geheimagentnr1.easier_sleeping.elements.commands.sleep.dimension_list_type.DimensionListTypeArgument;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;


public class ModArgumentTypesRegisterFactory {
	
	
	@NotNull
	private static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES =
		DeferredRegister.create( Registries.COMMAND_ARGUMENT_TYPE, EasierSleeping.MODID );
	
	static {
		ARGUMENT_TYPES.register(
			DimensionListTypeArgument.registry_name,
			() -> ArgumentTypeInfos.registerByClass(
				DimensionListTypeArgument.class,
				SingletonArgumentInfo.contextFree( DimensionListTypeArgument::dimensionListType )
			)
		);
	}
	
	public static void register( @NotNull IEventBus modEventBus ) {
		
		ARGUMENT_TYPES.register( modEventBus );
	}
}
