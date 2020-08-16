package de.geheimagentnr1.easier_sleeping.handlers;

import de.geheimagentnr1.easier_sleeping.config.MainConfig;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;


@SuppressWarnings( "unused" )
@Mod.EventBusSubscriber( bus = Mod.EventBusSubscriber.Bus.MOD )
public class ModEventHandler {
	
	
	@SuppressWarnings( "unused" )
	@SubscribeEvent
	public static void onLoad( ModConfig.Loading configEvent ) {
		
		MainConfig.printConfig();
	}
	
	@SuppressWarnings( "unused" )
	@SubscribeEvent
	public static void onFileChange( ModConfig.Reloading configEvent ) {
		
		MainConfig.printConfig();
	}
}
