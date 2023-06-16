package de.geheimagentnr1.easier_sleeping.elements.commands.sleep;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.geheimagentnr1.easier_sleeping.config.DimensionListType;
import de.geheimagentnr1.easier_sleeping.config.ServerConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;


@SuppressWarnings( "SameReturnValue" )
public class SleepCommand {
	
	
	public static void register( CommandDispatcher<CommandSourceStack> dispatcher ) {
		
		LiteralArgumentBuilder<CommandSourceStack> sleep = Commands.literal( "sleep" )
			.requires( source -> source.hasPermission( 2 ) );
		sleep.then( Commands.literal( "sleep_percent" )
			.executes( SleepCommand::showSleepPercent )
			.then( Commands.argument( "sleep_percent", IntegerArgumentType.integer( 0, 100 ) )
				.executes( SleepCommand::changeSleepPercent ) ) );
		sleep.then( Commands.literal( "message" )
			.then( Commands.literal( "wake" )
				.executes( SleepCommand::showWakeMessage )
				.then( Commands.argument( "message", MessageArgument.message() )
					.executes( SleepCommand::changeWakeMessage ) ) )
			.then( Commands.literal( "sleep" )
				.executes( SleepCommand::showSleepMessage )
				.then( Commands.argument( "message", MessageArgument.message() )
					.executes( SleepCommand::changeSleepMessage ) ) )
			.then( Commands.literal( "morning" )
				.executes( SleepCommand::showMorningMessage )
				.then( Commands.argument( "message", MessageArgument.message() )
					.executes( SleepCommand::changeMorningMessage ) ) ) );
		sleep.then( Commands.literal( "all_players_rest" )
			.executes( SleepCommand::showAllPlayersRest )
			.then( Commands.argument( "all_players_rest", BoolArgumentType.bool() )
				.executes( SleepCommand::setAllPlayersRest ) ) );
		sleep.then( Commands.literal( "dimension" )
			.executes( SleepCommand::showDimensions )
			.then( Commands.literal( "add" )
				.then( Commands.argument( "dimension", DimensionArgument.dimension() )
					.executes( SleepCommand::addDimension ) ) )
			.then( Commands.literal( "remove" )
				.then( Commands.argument( "dimension", DimensionArgument.dimension() )
					.executes( SleepCommand::removeDimension ) ) )
			.then( Commands.literal( "list_type" )
				.executes( SleepCommand::showDimensionListType )
				.then( Commands.argument( "list_type", DimensionListTypeArgument.dimensionListType() )
					.then( Commands.argument( "invert_list", BoolArgumentType.bool() )
						.executes( SleepCommand::changeDimensionListType ) ) ) ) );
		
		dispatcher.register( sleep );
	}
	
	private static int showSleepPercent( CommandContext<CommandSourceStack> context ) {
		
		context.getSource().sendSuccess(
			() -> Component.literal( String.format(
				"Sleep Percent: %d",
				ServerConfig.getSleepPercent()
			) ),
			false
		);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int changeSleepPercent( CommandContext<CommandSourceStack> context ) {
		
		ServerConfig.setSleepPercent( IntegerArgumentType.getInteger( context, "sleep_percent" ) );
		context.getSource().sendSuccess(
			() -> Component.literal( String.format(
				"Sleep Percent is now: %d",
				ServerConfig.getSleepPercent()
			) ),
			true
		);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int showWakeMessage( CommandContext<CommandSourceStack> context ) {
		
		context.getSource().sendSuccess(
			() -> Component.literal( String.format(
				"Wake Message: %s",
				ServerConfig.getWakeMessage()
			) ),
			false
		);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int changeWakeMessage( CommandContext<CommandSourceStack> context ) throws CommandSyntaxException {
		
		ServerConfig.setWakeMessage( MessageArgument.getMessage( context, "message" ).getString() );
		context.getSource().sendSuccess(
			() -> Component.literal( String.format(
				"Wake Message is now: %s",
				ServerConfig.getWakeMessage()
			) ),
			true
		);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int showSleepMessage( CommandContext<CommandSourceStack> context ) {
		
		context.getSource().sendSuccess(
			() -> Component.literal( String.format(
				"Sleep Message: %s",
				ServerConfig.getSleepMessage()
			) ),
			false
		);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int changeSleepMessage( CommandContext<CommandSourceStack> context ) throws CommandSyntaxException {
		
		ServerConfig.setSleepMessage( MessageArgument.getMessage( context, "message" ).getString() );
		context.getSource().sendSuccess(
			() -> Component.literal( String.format(
				"Sleep Message is now: %s",
				ServerConfig.getSleepMessage()
			) ),
			true
		);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int showMorningMessage( CommandContext<CommandSourceStack> context ) {
		
		context.getSource().sendSuccess(
			() -> Component.literal( String.format(
				"Morning Message: %s",
				ServerConfig.getMorningMessage()
			) ),
			false
		);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int changeMorningMessage( CommandContext<CommandSourceStack> context )
		throws CommandSyntaxException {
		
		ServerConfig.setMorningMessage( MessageArgument.getMessage( context, "message" ).getString() );
		context.getSource().sendSuccess(
			() -> Component.literal( String.format(
				"Morning Message is now: %s",
				ServerConfig.getMorningMessage()
			) ),
			true
		);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int showAllPlayersRest( CommandContext<CommandSourceStack> context ) {
		
		context.getSource().sendSuccess(
			() -> Component.literal( String.format(
				"All players rest is: %s",
				ServerConfig.getAllPlayersRest()
			) ),
			true
		);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int setAllPlayersRest( CommandContext<CommandSourceStack> context ) {
		
		ServerConfig.setAllPlayersRest( BoolArgumentType.getBool( context, "all_players_rest" ) );
		context.getSource().sendSuccess(
			() -> Component.literal( String.format(
				"All players rest is now : %s",
				ServerConfig.getAllPlayersRest()
			) ),
			true
		);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int showDimensions( CommandContext<CommandSourceStack> context ) {
		
		CommandSourceStack source = context.getSource();
		
		source.sendSuccess( () -> Component.literal( "Dimensions:" ), false );
		for( ResourceKey<Level> dimension : ServerConfig.getDimensions() ) {
			source.sendSuccess(
				() -> Component.literal( String.format(
					" - %s",
					dimension.location()
				) ),
				false
			);
		}
		return Command.SINGLE_SUCCESS;
	}
	
	private static int addDimension( CommandContext<CommandSourceStack> context ) throws CommandSyntaxException {
		
		ResourceKey<Level> dimension = DimensionArgument.getDimension( context, "dimension" ).dimension();
		ServerConfig.addDimension( dimension );
		context.getSource().sendSuccess(
			() -> Component.literal( String.format(
				"Added Dimension: %s",
				dimension.location()
			) ),
			true
		);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int removeDimension( CommandContext<CommandSourceStack> context ) throws CommandSyntaxException {
		
		ResourceKey<Level> dimension = DimensionArgument.getDimension( context, "dimension" ).dimension();
		ServerConfig.removeDimension( dimension );
		context.getSource().sendSuccess(
			() -> Component.literal( String.format(
				"Removed Dimension: %s",
				dimension.location()
			) ),
			true
		);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int showDimensionListType( CommandContext<CommandSourceStack> context ) {
		
		CommandSourceStack source = context.getSource();
		
		source.sendSuccess(
			() -> Component.literal( String.format(
				"Dimension List Type: %s",
				ServerConfig.getDimensionListType().name()
			) ),
			false
		);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int changeDimensionListType( CommandContext<CommandSourceStack> context ) {
		
		CommandSourceStack source = context.getSource();
		DimensionListType dimensionListType = DimensionListTypeArgument.getDimensionListType( context, "list_type" );
		boolean revert = BoolArgumentType.getBool( context, "invert_list" );
		
		ServerConfig.setDimensionListType( dimensionListType );
		if( revert ) {
			ServerConfig.invertDimensions();
		}
		source.sendSuccess(
			() -> Component.literal( String.format(
				"Dimension List Type set to: %s",
				ServerConfig.getDimensionListType().name()
			) ),
			false
		);
		source.sendSuccess( () -> Component.literal( "Dimensions:" ), false );
		for( ResourceKey<Level> dimension : ServerConfig.getDimensions() ) {
			source.sendSuccess(
				() -> Component.literal( String.format(
					" - %s",
					dimension.location()
				) ),
				false
			);
		}
		return Command.SINGLE_SUCCESS;
	}
}
