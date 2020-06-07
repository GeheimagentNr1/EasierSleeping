package de.geheimagentnr1.easier_sleeping.handlers;

import de.geheimagentnr1.easier_sleeping.config.ModConfig;
import de.geheimagentnr1.easier_sleeping.elements.commands.SleepCommand;
import de.geheimagentnr1.easier_sleeping.sleeping.SleepingManager;
import de.geheimagentnr1.easier_sleeping.sleeping.SleepingWorker;
import net.minecraftforge.common.WorldWorkerManager;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;


@SuppressWarnings( "unused" )
@Mod.EventBusSubscriber( bus = Mod.EventBusSubscriber.Bus.FORGE )
public class ForgeEventHandler {
	
	
	@SubscribeEvent
	public static void handlerServerStartEvent( FMLServerStartingEvent event ) {
		
		SleepCommand.register( event.getCommandDispatcher() );
		ModConfig.load();
		SleepingManager.init();
		WorldWorkerManager.addWorker( new SleepingWorker() );
	}
}
