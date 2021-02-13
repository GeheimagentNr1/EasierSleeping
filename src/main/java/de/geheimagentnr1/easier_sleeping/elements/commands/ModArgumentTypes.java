package de.geheimagentnr1.easier_sleeping.elements.commands;

import de.geheimagentnr1.easier_sleeping.EasierSleeping;
import de.geheimagentnr1.easier_sleeping.elements.commands.sleep.DimensionListTypeArgument;
import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;


public class ModArgumentTypes {
	
	
	public static void registerArgumentTypes() {
		
		ArgumentTypes.register(
			EasierSleeping.MODID + ":" + DimensionListTypeArgument.registry_name,
			DimensionListTypeArgument.class,
			new ArgumentSerializer<>( DimensionListTypeArgument::dimensionListType )
		);
	}
}
