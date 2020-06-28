package de.geheimagentnr1.easier_sleeping.elements.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.geheimagentnr1.easier_sleeping.config.ModConfig;
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
		
		LiteralArgumentBuilder<CommandSource> sleep = Commands.literal( "sleep" ).requires(
			source -> source.hasPermissionLevel( 2 ) );
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
		sleep.then( Commands.literal( "dimension" )
			.executes( SleepCommand::showDimensions )
			.then( Commands.literal( "add" )
				.then( Commands.argument( "dimension", DimensionArgument.getDimension() )
					.executes( SleepCommand::addDimension ) ) )
			.then( Commands.literal( "remove" )
				.then( Commands.argument( "dimension", DimensionArgument.getDimension() )
					.executes( SleepCommand::removeDimension ) ) ) );
		
		dispatcher.register( sleep );
	}
	
	private static int showSleepPercent( CommandContext<CommandSource> context ) {
		
		context.getSource().sendFeedback( new StringTextComponent( "Sleep Percent: " )
			.func_240702_b_( String.valueOf( ModConfig.getSleepPercent() ) ), false );
		return Command.SINGLE_SUCCESS;
	}
	
	private static int changeSleepPercent( CommandContext<CommandSource> context ) {
		
		ModConfig.setSleepPercent( IntegerArgumentType.getInteger( context, "sleep_percent" ) );
		context.getSource().sendFeedback( new StringTextComponent( "Sleep Percent is now: " )
			.func_240702_b_( String.valueOf( ModConfig.getSleepPercent() ) ), true );
		return Command.SINGLE_SUCCESS;
	}
	
	private static int showWakeMessage( CommandContext<CommandSource> context ) {
		
		context.getSource().sendFeedback( new StringTextComponent( "Wake Message: " )
			.func_240702_b_( ModConfig.getWakeMessage() ), false );
		return Command.SINGLE_SUCCESS;
	}
	
	private static int changeWakeMessage( CommandContext<CommandSource> context ) throws CommandSyntaxException {
		
		ModConfig.setWakeMessage( MessageArgument.getMessage( context, "message" ).getUnformattedComponentText() );
		context.getSource().sendFeedback( new StringTextComponent( "Wake Message is now: " )
			.func_240702_b_( ModConfig.getWakeMessage() ), true );
		return Command.SINGLE_SUCCESS;
	}
	
	private static int showSleepMessage( CommandContext<CommandSource> context ) {
		
		context.getSource().sendFeedback( new StringTextComponent( "Sleep Message: " )
			.func_240702_b_( ModConfig.getSleepMessage() ), false );
		return Command.SINGLE_SUCCESS;
	}
	
	private static int changeSleepMessage( CommandContext<CommandSource> context ) throws CommandSyntaxException {
		
		ModConfig.setSleepMessage( MessageArgument.getMessage( context, "message" ).getUnformattedComponentText() );
		context.getSource().sendFeedback( new StringTextComponent( "Sleep Message is now: " )
			.func_240702_b_( ModConfig.getSleepMessage() ), true );
		return Command.SINGLE_SUCCESS;
	}
	
	private static int showMorningMessage( CommandContext<CommandSource> context ) {
		
		context.getSource().sendFeedback( new StringTextComponent( "Morning Message: " )
			.func_240702_b_( ModConfig.getMorningMessage() ), false );
		return Command.SINGLE_SUCCESS;
	}
	
	private static int changeMorningMessage( CommandContext<CommandSource> context ) throws CommandSyntaxException {
		
		ModConfig.setMorningMessage( MessageArgument.getMessage( context, "message" ).getUnformattedComponentText() );
		context.getSource().sendFeedback( new StringTextComponent( "Morning Message is now: " )
			.func_240702_b_( ModConfig.getMorningMessage() ), true );
		return Command.SINGLE_SUCCESS;
	}
	
	private static int showDimensions( CommandContext<CommandSource> context ) {
		
		CommandSource source = context.getSource();
		
		source.sendFeedback( new StringTextComponent( "Dimensions:" ), false );
		for( RegistryKey<World> dimension : ModConfig.getDimensions() ) {
			source.sendFeedback( new StringTextComponent( " - " )
				.func_240702_b_( String.valueOf( dimension.func_240901_a_() ) ), false );
		}
		return Command.SINGLE_SUCCESS;
	}
	
	private static int addDimension( CommandContext<CommandSource> context ) throws CommandSyntaxException {
		
		RegistryKey<World> dimension = DimensionArgument.getDimensionArgument( context, "dimension" ).func_234923_W_();
		ModConfig.addDimension( dimension );
		context.getSource().sendFeedback( new StringTextComponent( "Added Dimension: " )
			.func_240702_b_( String.valueOf( dimension.func_240901_a_() ) ), true );
		return Command.SINGLE_SUCCESS;
	}
	
	private static int removeDimension( CommandContext<CommandSource> context ) throws CommandSyntaxException {
		
		RegistryKey<World> dimension = DimensionArgument.getDimensionArgument( context, "dimension" ).func_234923_W_();
		ModConfig.removeDimension( dimension );
		context.getSource().sendFeedback( new StringTextComponent( "Removed Dimension: " )
			.func_240702_b_( String.valueOf( dimension.func_240901_a_() ) ), true );
		return Command.SINGLE_SUCCESS;
	}
}
