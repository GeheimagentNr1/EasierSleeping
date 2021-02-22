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
				.then( Commands.argument(
					"message",
					MessageArgument.message()
				).executes( SleepCommand::changeWakeMessage ) ) )
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
				.then( Commands.argument(
					"list_type",
					DimensionListTypeArgument.dimensionListType()
				)
					.then( Commands.argument( "invert_list", BoolArgumentType.bool() )
						.executes( SleepCommand::changeDimensionListType ) ) ) ) );
		
		dispatcher.register( sleep );
	}
	
	private static int showSleepPercent( CommandContext<CommandSource> context ) {
		
		context.getSource().sendFeedback( new StringTextComponent( "Sleep Percent: " ).appendString( String.valueOf(
			ServerConfig.getSleepPercent() ) ), false );
		return Command.SINGLE_SUCCESS;
	}
	
	private static int changeSleepPercent( CommandContext<CommandSource> context ) {
		
		ServerConfig.setSleepPercent( IntegerArgumentType.getInteger( context, "sleep_percent" ) );
		context.getSource()
			.sendFeedback(
				new StringTextComponent( "Sleep Percent is now: " )
					.appendString( String.valueOf( ServerConfig.getSleepPercent() ) ),
				true
			);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int showWakeMessage( CommandContext<CommandSource> context ) {
		
		context.getSource()
			.sendFeedback(
				new StringTextComponent( "Wake Message: " ).appendString( ServerConfig.getWakeMessage() ),
				false
			);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int changeWakeMessage( CommandContext<CommandSource> context ) throws CommandSyntaxException {
		
		ServerConfig.setWakeMessage( MessageArgument.getMessage( context, "message" ).getUnformattedComponentText() );
		context.getSource()
			.sendFeedback(
				new StringTextComponent( "Wake Message is now: " ).appendString( ServerConfig.getWakeMessage() ),
				true
			);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int showSleepMessage( CommandContext<CommandSource> context ) {
		
		context.getSource()
			.sendFeedback(
				new StringTextComponent( "Sleep Message: " ).appendString( ServerConfig.getSleepMessage() ),
				false
			);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int changeSleepMessage( CommandContext<CommandSource> context ) throws CommandSyntaxException {
		
		ServerConfig.setSleepMessage( MessageArgument.getMessage( context, "message" ).getUnformattedComponentText() );
		context.getSource()
			.sendFeedback(
				new StringTextComponent( "Sleep Message is now: " ).appendString( ServerConfig.getSleepMessage() ),
				true
			);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int showMorningMessage( CommandContext<CommandSource> context ) {
		
		context.getSource()
			.sendFeedback(
				new StringTextComponent( "Morning Message: " ).appendString( ServerConfig.getMorningMessage() ),
				false
			);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int changeMorningMessage( CommandContext<CommandSource> context ) throws CommandSyntaxException {
		
		ServerConfig.setMorningMessage( MessageArgument.getMessage( context, "message" )
			.getUnformattedComponentText() );
		context.getSource().sendFeedback(
			new StringTextComponent( "Morning Message is now: " ).appendString( ServerConfig.getMorningMessage() ),
			true
		);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int showAllPlayersRest( CommandContext<CommandSource> context ) {
		
		context.getSource().sendFeedback(
			new StringTextComponent( "All players rest is: " )
				.appendString( String.valueOf( ServerConfig.getAllPlayersRest() ) ),
			true
		);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int setAllPlayersRest( CommandContext<CommandSource> context ) {
		
		ServerConfig.setAllPlayersRest( BoolArgumentType.getBool( context, "all_players_rest" ) );
		context.getSource().sendFeedback(
			new StringTextComponent( "All players rest is now : " )
				.appendString( String.valueOf( ServerConfig.getAllPlayersRest() ) ),
			true
		);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int showDimensions( CommandContext<CommandSource> context ) {
		
		CommandSource source = context.getSource();
		
		source.sendFeedback( new StringTextComponent( "Dimensions:" ), false );
		for( RegistryKey<World> dimension : ServerConfig.getDimensions() ) {
			source.sendFeedback(
				new StringTextComponent( " - " ).appendString( String.valueOf( dimension.getLocation() ) ),
				false
			);
		}
		return Command.SINGLE_SUCCESS;
	}
	
	private static int addDimension( CommandContext<CommandSource> context ) throws CommandSyntaxException {
		
		RegistryKey<World> dimension = DimensionArgument.getDimensionArgument( context, "dimension" ).getDimensionKey();
		ServerConfig.addDimension( dimension );
		context.getSource().sendFeedback(
			new StringTextComponent( "Added Dimension: " )
				.appendString( String.valueOf( dimension.getLocation() ) ),
			true
		);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int removeDimension( CommandContext<CommandSource> context ) throws CommandSyntaxException {
		
		RegistryKey<World> dimension = DimensionArgument.getDimensionArgument( context, "dimension" ).getDimensionKey();
		ServerConfig.removeDimension( dimension );
		context.getSource()
			.sendFeedback(
				new StringTextComponent( "Removed Dimension: " )
					.appendString( String.valueOf( dimension.getLocation() ) ),
				true
			);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int showDimensionListType( CommandContext<CommandSource> context ) {
		
		CommandSource source = context.getSource();
		
		source.sendFeedback( new StringTextComponent( "Dimension List Type: " )
			.appendString( ServerConfig.getDimensionListType().name() ), false );
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
			new StringTextComponent( "Dimension List Type set to: " )
				.appendString( ServerConfig.getDimensionListType().name() ),
			false
		);
		source.sendFeedback( new StringTextComponent( "Dimensions:" ), false );
		for( RegistryKey<World> dimension : ServerConfig.getDimensions() ) {
			source.sendFeedback(
				new StringTextComponent( " - " ).appendString( String.valueOf( dimension.getLocation() ) ),
				false
			);
		}
		return Command.SINGLE_SUCCESS;
	}
}
