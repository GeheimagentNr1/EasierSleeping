package de.geheimagentnr1.easier_sleeping.handlers;

import de.geheimagentnr1.easier_sleeping.EasierSleeping;
import de.geheimagentnr1.easier_sleeping.elements.commands.ModArgumentTypes;
import de.geheimagentnr1.easier_sleeping.elements.commands.sleep.SleepCommand;
import de.geheimagentnr1.easier_sleeping.sleeping.SleepingManager;
import de.geheimagentnr1.easier_sleeping.sleeping.SleepingWorker;
import net.minecraftforge.common.WorldWorkerManager;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;


@Mod.EventBusSubscriber( modid = EasierSleeping.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE )
public class ForgeEventHandler {
	
	
	@SubscribeEvent
	public static void handlerServerStartingEvent( FMLServerStartingEvent event ) {
		
		ModArgumentTypes.registerArgumentTypes();
		SleepCommand.register( event.getCommandDispatcher() );
		SleepingManager.init();
		WorldWorkerManager.addWorker( new SleepingWorker() );
	}
}
