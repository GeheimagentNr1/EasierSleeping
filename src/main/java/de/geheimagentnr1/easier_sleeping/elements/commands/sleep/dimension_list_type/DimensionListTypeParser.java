package de.geheimagentnr1.easier_sleeping.elements.commands.sleep.dimension_list_type;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.geheimagentnr1.easier_sleeping.config.DimensionListType;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;


//package-private
class DimensionListTypeParser {
	
	
	@NotNull
	private static final DynamicCommandExceptionType DIMENSION_LIST_TYPE_INVALID = new DynamicCommandExceptionType(
		function -> Component.literal( "Invalid Dimension List Type" )
	);
	
	@NotNull
	private static final Set<String> DIMENSION_LIST_TYPES = getItemKeySet();
	
	@NotNull
	private final StringReader reader;
	
	private DimensionListType dimensionListType;
	
	private Function<SuggestionsBuilder, CompletableFuture<Suggestions>> suggestionsBuilder;
	
	//package-private
	DimensionListTypeParser( @NotNull StringReader _reader ) {
		
		reader = _reader;
	}
	
	@NotNull
	private static Set<String> getItemKeySet() {
		
		Set<String> keySet = new TreeSet<>();
		
		for( DimensionListType dimensionListType : DimensionListType.values() ) {
			keySet.add( dimensionListType.name() );
		}
		return keySet;
	}
	
	//package-private
	@NotNull
	DimensionListType getDimensionListType() {
		
		return dimensionListType;
	}
	
	private void readDimensionListType() throws CommandSyntaxException {
		
		int cursor = reader.getCursor();
		
		while( reader.canRead() && reader.peek() != ' ' ) {
			reader.skip();
		}
		String dimensionListTypeString = reader.getString().substring( cursor, reader.getCursor() );
		dimensionListType = getItemForRegistry( dimensionListTypeString ).orElseThrow( () -> {
			reader.setCursor( cursor );
			return DIMENSION_LIST_TYPE_INVALID.createWithContext( reader, dimensionListTypeString );
		} );
	}
	
	@NotNull
	private Optional<DimensionListType> getItemForRegistry( @NotNull String dimensionListTypeString ) {
		
		for( DimensionListType forDimensionListType : DimensionListType.values() ) {
			if( forDimensionListType.name().equals( dimensionListTypeString ) ) {
				return Optional.of( forDimensionListType );
			}
		}
		return Optional.empty();
	}
	
	//package-private
	@SuppressWarnings( "ReturnOfThis" )
	@NotNull
	DimensionListTypeParser parse() throws CommandSyntaxException {
		
		suggestionsBuilder = this::suggestDimensionListType;
		readDimensionListType();
		suggestionsBuilder = this::suggestDimensionListTypeFuture;
		return this;
	}
	
	@NotNull
	private CompletableFuture<Suggestions> suggestDimensionListTypeFuture( @NotNull SuggestionsBuilder builder ) {
		
		return builder.buildFuture();
	}
	
	@NotNull
	private CompletableFuture<Suggestions> suggestDimensionListType( @NotNull SuggestionsBuilder builder ) {
		
		return SharedSuggestionProvider.suggest( DIMENSION_LIST_TYPES, builder );
	}
	
	//package-private
	@NotNull
	CompletableFuture<Suggestions> fillSuggestions( @NotNull SuggestionsBuilder builder ) {
		
		return suggestionsBuilder.apply( builder.createOffset( reader.getCursor() ) );
	}
}
