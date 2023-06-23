package de.geheimagentnr1.easier_sleeping.sleeping;

import lombok.RequiredArgsConstructor;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.WorldWorkerManager;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;


@RequiredArgsConstructor
public class SleepingWorker implements WorldWorkerManager.IWorker {
	
	
	@NotNull
	private final SleepingManager sleepingManager;
	
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
		sleepingManager.updateSleepingPlayers( server );
		return false;
	}
}
