package de.geheimagentnr1.easier_sleeping.sleeping;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.WorldWorkerManager;
import net.minecraftforge.server.ServerLifecycleHooks;


public class SleepingWorker implements WorldWorkerManager.IWorker {
	
	
	@Override
	public boolean hasWork() {
		
		return true;
	}
	
	/**
	 * Perform a task, returning true from this will have the manager call this function again this tick if there is
	 * time left.
	 * Returning false will skip calling this worker until next tick.
	 */
	@Override
	public boolean doWork() {
		
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if( server.getTickCount() % 20 != 0 ) {
			return false;
		}
		SleepingManager.updateSleepingPlayers( server );
		return false;
	}
}
