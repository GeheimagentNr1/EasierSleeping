package de.geheimagentnr1.easier_sleeping.elements.commands;

import de.geheimagentnr1.easier_sleeping.config.ServerConfig;
import de.geheimagentnr1.easier_sleeping.elements.commands.sleep.SleepCommand;
import de.geheimagentnr1.minecraft_forge_api.elements.commands.CommandInterface;
import de.geheimagentnr1.minecraft_forge_api.elements.commands.CommandsRegisterFactory;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;


@RequiredArgsConstructor
public class ModCommandsRegisterFactory extends CommandsRegisterFactory {
	
	
	@NotNull
	private final ServerConfig serverConfig;
	
	@NotNull
	@Override
	public List<CommandInterface> commands() {
		
		return List.of(
			new SleepCommand( serverConfig )
		);
	}
}
