package de.geheimagentnr1.easier_sleeping.elements.commands.sleep;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.geheimagentnr1.easier_sleeping.config.DimensionListType;
import de.geheimagentnr1.easier_sleeping.config.ServerConfig;
import de.geheimagentnr1.easier_sleeping.elements.commands.sleep.dimension_list_type.DimensionListTypeArgument;
import de.geheimagentnr1.minecraft_forge_api.elements.commands.CommandInterface;
import lombok.RequiredArgsConstructor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;


@SuppressWarnings( "SameReturnValue" )
@RequiredArgsConstructor
public class SleepCommand implements CommandInterface {
	
	
	@NotNull
	private final ServerConfig serverConfig;
	
	@NotNull
	@Override
	public LiteralArgumentBuilder<CommandSourceStack> build() {
		
		LiteralArgumentBuilder<CommandSourceStack> sleep = Commands.literal( "sleep" )
			.requires( source -> source.hasPermission( 2 ) );
		sleep.then( Commands.literal( "sleep_percent" )
			.executes( this::showSleepPercent )
			.then( Commands.argument( "sleep_percent", IntegerArgumentType.integer( 0, 100 ) )
				.executes( this::changeSleepPercent ) ) );
		sleep.then( Commands.literal( "messages" )
			.then( Commands.literal( "wake" )
				.executes( this::showWakeMessage )
				.then( Commands.literal( "add" )
					.then( Commands.argument( "message", MessageArgument.message() )
						.executes( this::addWakeMessage ) ) )
				.then( Commands.literal( "remove" )
					.then( Commands.argument( "message", MessageArgument.message() )
						.executes( this::removeWakeMessage ) ) ) )
			.then( Commands.literal( "sleep" )
				.executes( this::showSleepMessage )
				.then( Commands.literal( "add" )
					.then( Commands.argument( "message", MessageArgument.message() )
						.executes( this::addSleepMessage ) ) )
				.then( Commands.literal( "remove" )
					.then( Commands.argument( "message", MessageArgument.message() )
						.executes( this::removeSleepMessage ) ) ) )
			.then( Commands.literal( "morning" )
				.executes( this::showMorningMessage )
				.then( Commands.literal( "add" )
					.then( Commands.argument( "message", MessageArgument.message() )
						.executes( this::addMorningMessage ) ) )
				.then( Commands.literal( "remove" )
					.then( Commands.argument( "message", MessageArgument.message() )
						.executes( this::removeMorningMessage ) ) ) ) );
		sleep.then( Commands.literal( "all_players_rest" )
			.executes( this::showAllPlayersRest )
			.then( Commands.argument( "all_players_rest", BoolArgumentType.bool() )
				.executes( this::setAllPlayersRest ) ) );
		sleep.then( Commands.literal( "dimension" )
			.executes( this::showDimensions )
			.then( Commands.literal( "add" )
				.then( Commands.argument( "dimension", DimensionArgument.dimension() )
					.executes( this::addDimension ) ) )
			.then( Commands.literal( "remove" )
				.then( Commands.argument( "dimension", DimensionArgument.dimension() )
					.executes( this::removeDimension ) ) )
			.then( Commands.literal( "list_type" )
				.executes( this::showDimensionListType )
				.then( Commands.argument( "list_type", DimensionListTypeArgument.dimensionListType() )
					.then( Commands.argument( "invert_list", BoolArgumentType.bool() )
						.executes( this::changeDimensionListType ) ) ) ) );
		
		return sleep;
	}
	
	private int showSleepPercent( @NotNull CommandContext<CommandSourceStack> context ) {
		
		context.getSource().sendSuccess(
			() -> Component.literal( String.format(
				"Sleep Percent: %d",
				serverConfig.getSleepPercent()
			) ),
			false
		);
		return Command.SINGLE_SUCCESS;
	}
	
	private int changeSleepPercent( @NotNull CommandContext<CommandSourceStack> context ) {
		
		serverConfig.setSleepPercent( IntegerArgumentType.getInteger( context, "sleep_percent" ) );
		context.getSource().sendSuccess(
			() -> Component.literal( String.format(
				"Sleep Percent is now: %d",
				serverConfig.getSleepPercent()
			) ),
			true
		);
		return Command.SINGLE_SUCCESS;
	}
	
	private void sendMessages(
		@NotNull CommandContext<CommandSourceStack> context,
		@NotNull String messagePrefix,
		@NotNull Supplier<List<String>> configMessagesGetter ) {
		
		context.getSource().sendSuccess(
			() -> Component.literal( String.format(
				"%s: %s",
				messagePrefix,
				String.join( ", ", configMessagesGetter.get() )
			) ),
			true
		);
	}
	
	private int showMessages(
		@NotNull CommandContext<CommandSourceStack> context,
		@NotNull String resultMessagePrefix,
		@NotNull Supplier<List<String>> configMessagesGetter ) {
		
		sendMessages(
			context,
			String.format(
				"%s Messages",
				resultMessagePrefix
			),
			configMessagesGetter
		);
		return Command.SINGLE_SUCCESS;
	}
	
	private int showWakeMessage( @NotNull CommandContext<CommandSourceStack> context ) {
		
		return showMessages( context, "Wake", serverConfig::getWakeMessages );
	}
	
	private int changeMessages(
		@NotNull CommandContext<CommandSourceStack> context,
		@NotNull MessageActionType messageActionType,
		@NotNull Consumer<List<String>> configMessagesSetter,
		@NotNull Supplier<List<String>> configMessagesGetter,
		@NotNull String resultMessagePrefix )
		throws CommandSyntaxException {
		
		String message = MessageArgument.getMessage( context, "message" ).getString();
		List<String> messages = new ArrayList<>( configMessagesGetter.get() );
		
		if( messageActionType == MessageActionType.ADD ) {
			messages.add( message );
		} else {
			messages.remove( message );
		}
		configMessagesSetter.accept( messages );
		sendMessages(
			context,
			String.format(
				"%s Messages are now",
				resultMessagePrefix
			),
			configMessagesGetter
		);
		return Command.SINGLE_SUCCESS;
	}
	
	private int changeWakeMessage(
		@NotNull CommandContext<CommandSourceStack> context,
		@NotNull MessageActionType messageActionType ) throws CommandSyntaxException {
		
		return changeMessages(
			context,
			messageActionType,
			serverConfig::setWakeMessages,
			serverConfig::getWakeMessages,
			"Wake"
		);
	}
	
	private int addWakeMessage( @NotNull CommandContext<CommandSourceStack> context ) throws CommandSyntaxException {
		
		return changeWakeMessage( context, MessageActionType.ADD );
	}
	
	private int removeWakeMessage( @NotNull CommandContext<CommandSourceStack> context ) throws CommandSyntaxException {
		
		return changeWakeMessage( context, MessageActionType.REMOVE );
	}
	
	private int showSleepMessage( @NotNull CommandContext<CommandSourceStack> context ) {
		
		return showMessages( context, "Sleep", serverConfig::getSleepMessages );
	}
	
	private int changeSleepMessage(
		@NotNull CommandContext<CommandSourceStack> context,
		@NotNull MessageActionType messageActionType ) throws CommandSyntaxException {
		
		return changeMessages(
			context,
			messageActionType,
			serverConfig::setSleepMessages,
			serverConfig::getSleepMessages,
			"Sleep"
		);
	}
	
	private int addSleepMessage( @NotNull CommandContext<CommandSourceStack> context ) throws CommandSyntaxException {
		
		return changeSleepMessage( context, MessageActionType.ADD );
	}
	
	private int removeSleepMessage( @NotNull CommandContext<CommandSourceStack> context )
		throws CommandSyntaxException {
		
		return changeSleepMessage( context, MessageActionType.REMOVE );
	}
	
	private int showMorningMessage( @NotNull CommandContext<CommandSourceStack> context ) {
		
		return showMessages( context, "Morning", serverConfig::getMorningMessages );
	}
	
	private int changeMorningMessage(
		@NotNull CommandContext<CommandSourceStack> context,
		@NotNull MessageActionType messageActionType ) throws CommandSyntaxException {
		
		return changeMessages(
			context,
			messageActionType,
			serverConfig::setMorningMessages,
			serverConfig::getMorningMessages,
			"Morning"
		);
	}
	
	private int addMorningMessage( @NotNull CommandContext<CommandSourceStack> context ) throws CommandSyntaxException {
		
		return changeMorningMessage( context, MessageActionType.ADD );
	}
	
	private int removeMorningMessage( @NotNull CommandContext<CommandSourceStack> context )
		throws CommandSyntaxException {
		
		return changeMorningMessage( context, MessageActionType.REMOVE );
	}
	
	private int showAllPlayersRest( @NotNull CommandContext<CommandSourceStack> context ) {
		
		context.getSource().sendSuccess(
			() -> Component.literal( String.format(
				"All players rest is: %s",
				serverConfig.getAllPlayersRest()
			) ),
			true
		);
		return Command.SINGLE_SUCCESS;
	}
	
	private int setAllPlayersRest( @NotNull CommandContext<CommandSourceStack> context ) {
		
		serverConfig.setAllPlayersRest( BoolArgumentType.getBool( context, "all_players_rest" ) );
		context.getSource().sendSuccess(
			() -> Component.literal( String.format(
				"All players rest is now : %s",
				serverConfig.getAllPlayersRest()
			) ),
			true
		);
		return Command.SINGLE_SUCCESS;
	}
	
	private int showDimensions( @NotNull CommandContext<CommandSourceStack> context ) {
		
		CommandSourceStack source = context.getSource();
		
		source.sendSuccess( () -> Component.literal( "Dimensions:" ), false );
		for( ResourceKey<Level> dimension : serverConfig.getDimensions() ) {
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
	
	private int addDimension( @NotNull CommandContext<CommandSourceStack> context ) throws CommandSyntaxException {
		
		ResourceKey<Level> dimension = DimensionArgument.getDimension( context, "dimension" ).dimension();
		serverConfig.addDimension( dimension );
		context.getSource().sendSuccess(
			() -> Component.literal( String.format(
				"Added Dimension: %s",
				dimension.location()
			) ),
			true
		);
		return Command.SINGLE_SUCCESS;
	}
	
	private int removeDimension( @NotNull CommandContext<CommandSourceStack> context ) throws CommandSyntaxException {
		
		ResourceKey<Level> dimension = DimensionArgument.getDimension( context, "dimension" ).dimension();
		serverConfig.removeDimension( dimension );
		context.getSource().sendSuccess(
			() -> Component.literal( String.format(
				"Removed Dimension: %s",
				dimension.location()
			) ),
			true
		);
		return Command.SINGLE_SUCCESS;
	}
	
	private int showDimensionListType( @NotNull CommandContext<CommandSourceStack> context ) {
		
		CommandSourceStack source = context.getSource();
		
		source.sendSuccess(
			() -> Component.literal( String.format(
				"Dimension List Type: %s",
				serverConfig.getDimensionListType().name()
			) ),
			false
		);
		return Command.SINGLE_SUCCESS;
	}
	
	private int changeDimensionListType( @NotNull CommandContext<CommandSourceStack> context ) {
		
		CommandSourceStack source = context.getSource();
		DimensionListType dimensionListType = DimensionListTypeArgument.getDimensionListType( context, "list_type" );
		boolean revert = BoolArgumentType.getBool( context, "invert_list" );
		
		serverConfig.setDimensionListType( dimensionListType );
		if( revert ) {
			serverConfig.invertDimensions();
		}
		source.sendSuccess(
			() -> Component.literal( String.format(
				"Dimension List Type set to: %s",
				serverConfig.getDimensionListType().name()
			) ),
			false
		);
		source.sendSuccess( () -> Component.literal( "Dimensions:" ), false );
		for( ResourceKey<Level> dimension : serverConfig.getDimensions() ) {
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
