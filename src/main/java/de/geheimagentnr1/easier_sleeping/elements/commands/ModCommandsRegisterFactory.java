package de.geheimagentnr1.easier_sleeping.elements.commands;

import de.geheimagentnr1.easier_sleeping.config.ServerConfig;
import de.geheimagentnr1.easier_sleeping.elements.commands.sleep.SleepCommand;
import lombok.RequiredArgsConstructor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.jetbrains.annotations.NotNull;


@RequiredArgsConstructor
public class ModCommandsRegisterFactory {
	
	
	@NotNull
	private final ServerConfig serverConfig;
	
	@SubscribeEvent
	public void onRegisterCommands( @NotNull RegisterCommandsEvent event ) {
		
		event.getDispatcher().register( new SleepCommand( serverConfig ).build() );
	}
}
