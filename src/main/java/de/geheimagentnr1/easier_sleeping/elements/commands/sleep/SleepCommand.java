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
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.DimensionArgument;
import net.minecraft.command.arguments.MessageArgument;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;


@SuppressWarnings( "SameReturnValue" )
public class SleepCommand {
	
	
	public static void register( CommandDispatcher<CommandSource> dispatcher ) {
		
		LiteralArgumentBuilder<CommandSource> sleep =
			Commands.literal( "sleep" ).requires( source -> source.hasPermissionLevel( 2 ) );
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
				.then( Commands.argument( "dimension", DimensionArgument.getDimension() )
					.executes( SleepCommand::addDimension ) ) )
			.then( Commands.literal( "remove" )
				.then( Commands.argument( "dimension", DimensionArgument.getDimension() )
					.executes( SleepCommand::removeDimension ) ) )
			.then( Commands.literal( "list_type" )
				.executes( SleepCommand::showDimensionListType )
				.then( Commands.argument( "list_type", DimensionListTypeArgument.dimensionListType() )
					.then( Commands.argument( "invert_list", BoolArgumentType.bool() )
						.executes( SleepCommand::changeDimensionListType ) ) ) ) );
		
		dispatcher.register( sleep );
	}
	
	private static int showSleepPercent( CommandContext<CommandSource> context ) {
		
		context.getSource().sendFeedback(
			new StringTextComponent( String.format(
				"Sleep Percent: %d",
				ServerConfig.getSleepPercent()
			) ),
			false
		);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int changeSleepPercent( CommandContext<CommandSource> context ) {
		
		ServerConfig.setSleepPercent( IntegerArgumentType.getInteger( context, "sleep_percent" ) );
		context.getSource().sendFeedback(
			new StringTextComponent( String.format(
				"Sleep Percent is now: %d",
				ServerConfig.getSleepPercent()
			) ),
			true
		);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int showWakeMessage( CommandContext<CommandSource> context ) {
		
		context.getSource().sendFeedback(
			new StringTextComponent( String.format(
				"Wake Message: %s",
				ServerConfig.getWakeMessage()
			) ),
			false
		);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int changeWakeMessage( CommandContext<CommandSource> context ) throws CommandSyntaxException {
		
		ServerConfig.setWakeMessage( MessageArgument.getMessage( context, "message" ).getUnformattedComponentText() );
		context.getSource().sendFeedback(
			new StringTextComponent( String.format(
				"Wake Message is now: %s",
				ServerConfig.getWakeMessage()
			) ),
			true
		);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int showSleepMessage( CommandContext<CommandSource> context ) {
		
		context.getSource().sendFeedback(
			new StringTextComponent( String.format(
				"Sleep Message: %s",
				ServerConfig.getSleepMessage()
			) ),
			false
		);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int changeSleepMessage( CommandContext<CommandSource> context ) throws CommandSyntaxException {
		
		ServerConfig.setSleepMessage( MessageArgument.getMessage( context, "message" ).getUnformattedComponentText() );
		context.getSource().sendFeedback(
			new StringTextComponent( String.format(
				"Sleep Message is now: %s",
				ServerConfig.getSleepMessage()
			) ),
			true
		);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int showMorningMessage( CommandContext<CommandSource> context ) {
		
		context.getSource().sendFeedback(
			new StringTextComponent( String.format(
				"Morning Message: %s",
				ServerConfig.getMorningMessage()
			) ),
			false
		);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int changeMorningMessage( CommandContext<CommandSource> context ) throws CommandSyntaxException {
		
		ServerConfig.setMorningMessage( MessageArgument.getMessage( context, "message" )
			.getUnformattedComponentText() );
		context.getSource().sendFeedback(
			new StringTextComponent( String.format(
				"Morning Message is now: %s",
				ServerConfig.getMorningMessage()
			) ),
			true
		);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int showAllPlayersRest( CommandContext<CommandSource> context ) {
		
		context.getSource().sendFeedback(
			new StringTextComponent( String.format(
				"All players rest is: %s",
				ServerConfig.getAllPlayersRest()
			) ),
			true
		);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int setAllPlayersRest( CommandContext<CommandSource> context ) {
		
		ServerConfig.setAllPlayersRest( BoolArgumentType.getBool( context, "all_players_rest" ) );
		context.getSource().sendFeedback(
			new StringTextComponent( String.format(
				"All players rest is now : %s",
				ServerConfig.getAllPlayersRest()
			) ),
			true
		);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int showDimensions( CommandContext<CommandSource> context ) {
		
		CommandSource source = context.getSource();
		
		source.sendFeedback( new StringTextComponent( "Dimensions:" ), false );
		for( RegistryKey<World> dimension : ServerConfig.getDimensions() ) {
			source.sendFeedback(
				new StringTextComponent( String.format(
					" - %s",
					dimension.getRegistryName()
				) ),
				false
			);
		}
		return Command.SINGLE_SUCCESS;
	}
	
	private static int addDimension( CommandContext<CommandSource> context ) throws CommandSyntaxException {
		
		RegistryKey<World> dimension = DimensionArgument.getDimensionArgument( context, "dimension" ).func_234923_W_();
		ServerConfig.addDimension( dimension );
		context.getSource().sendFeedback(
			new StringTextComponent( String.format(
				"Added Dimension: %s",
				dimension.getRegistryName()
			) ),
			true
		);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int removeDimension( CommandContext<CommandSource> context ) throws CommandSyntaxException {
		
		RegistryKey<World> dimension = DimensionArgument.getDimensionArgument( context, "dimension" ).func_234923_W_();
		ServerConfig.removeDimension( dimension );
		context.getSource().sendFeedback(
			new StringTextComponent( String.format(
				"Removed Dimension: %s",
				dimension.getRegistryName()
			) ),
			true
		);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int showDimensionListType( CommandContext<CommandSource> context ) {
		
		CommandSource source = context.getSource();
		
		source.sendFeedback(
			new StringTextComponent( String.format(
				"Dimension List Type: %s",
				ServerConfig.getDimensionListType().name()
			) ),
			false
		);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int changeDimensionListType( CommandContext<CommandSource> context ) {
		
		CommandSource source = context.getSource();
		DimensionListType dimensionListType = DimensionListTypeArgument.getDimensionListType( context, "list_type" );
		boolean revert = BoolArgumentType.getBool( context, "invert_list" );
		
		ServerConfig.setDimensionListType( dimensionListType );
		if( revert ) {
			ServerConfig.invertDimensions();
		}
		source.sendFeedback(
			new StringTextComponent( String.format(
				"Dimension List Type set to: %s",
				ServerConfig.getDimensionListType().name()
			) ),
			false
		);
		source.sendFeedback( new StringTextComponent( "Dimensions:" ), false );
		for( RegistryKey<World> dimension : ServerConfig.getDimensions() ) {
			source.sendFeedback(
				new StringTextComponent( String.format(
					" - %s",
					dimension.getRegistryName()
				) ),
				false
			);
		}
		return Command.SINGLE_SUCCESS;
	}
}
