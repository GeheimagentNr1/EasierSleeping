package de.geheimagentnr1.easier_sleeping.handlers;

import de.geheimagentnr1.easier_sleeping.config.ModConfig;
import de.geheimagentnr1.easier_sleeping.sleeping.SleepingManager;
import de.geheimagentnr1.easier_sleeping.sleeping.SleepingWorker;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.WorldWorkerManager;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;


@SuppressWarnings( "unused" )
@Mod.EventBusSubscriber( bus = Mod.EventBusSubscriber.Bus.FORGE )
public class ForgeEventHandler {
	
	
	@SubscribeEvent
	public static void handlerServerStartEvent( FMLServerStartingEvent event ) {
		
		ModConfig.load();
		if( !ModConfig.DIMENSIONS.get().isEmpty() ) {
			SleepingManager.init();
			WorldWorkerManager.addWorker( new SleepingWorker() );
		}
	}
	
	@SubscribeEvent
	public static void handleRightClickBlockEvent( PlayerInteractEvent.RightClickBlock event ) {
		
		World world = event.getWorld();
		PlayerEntity player = event.getPlayer();
		
		if( !world.isRemote() && world.isDaytime() && !player.isSpectator() ) {
			BlockPos pos = event.getPos();
			BlockState state = world.getBlockState( pos );
			if( state.getBlock() instanceof BedBlock ) {
				player.setSpawnPoint( pos, false, event.getWorld().getDimension().getType() );
				player.sendMessage( new StringTextComponent( "Set new spawnpoint" ) );
				event.setCanceled( true );
			}
		}
	}
}
