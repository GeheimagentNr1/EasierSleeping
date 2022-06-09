package de.geheimagentnr1.easier_sleeping.elements.commands;

import de.geheimagentnr1.easier_sleeping.EasierSleeping;
import de.geheimagentnr1.easier_sleeping.elements.commands.sleep.DimensionListTypeArgument;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.Registry;


public class ModArgumentTypes {
	
	
	public static void registerArgumentTypes() {
		
		Registry.register(
			Registry.COMMAND_ARGUMENT_TYPE,
			EasierSleeping.MODID + ":" + DimensionListTypeArgument.registry_name,
			ArgumentTypeInfos.registerByClass(
				DimensionListTypeArgument.class,
				SingletonArgumentInfo.contextFree( DimensionListTypeArgument::new )
			)
		);
	}
}
