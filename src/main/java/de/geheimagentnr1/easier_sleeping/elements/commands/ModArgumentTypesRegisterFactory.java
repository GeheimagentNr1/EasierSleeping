package de.geheimagentnr1.easier_sleeping.elements.commands;

import de.geheimagentnr1.easier_sleeping.elements.commands.sleep.DimensionListTypeArgument;
import de.geheimagentnr1.minecraft_forge_api.registry.ElementsRegisterFactory;
import de.geheimagentnr1.minecraft_forge_api.registry.RegistryEntry;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class ModArgumentTypesRegisterFactory extends ElementsRegisterFactory<ArgumentTypeInfo<?, ?>> {
	
	
	@NotNull
	@Override
	protected ResourceKey<Registry<ArgumentTypeInfo<?, ?>>> registryKey() {
		
		return Registries.COMMAND_ARGUMENT_TYPE;
	}
	
	@NotNull
	@Override
	protected List<RegistryEntry<ArgumentTypeInfo<?, ?>>> elements() {
		
		return List.of(
			RegistryEntry.create(
				DimensionListTypeArgument.registry_name,
				ArgumentTypeInfos.registerByClass(
					DimensionListTypeArgument.class,
					SingletonArgumentInfo.contextFree( DimensionListTypeArgument::dimensionListType )
				)
			)
		);
	}
}
