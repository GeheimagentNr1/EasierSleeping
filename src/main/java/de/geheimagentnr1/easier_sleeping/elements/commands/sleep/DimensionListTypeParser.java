package de.geheimagentnr1.easier_sleeping.elements.commands.sleep;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.geheimagentnr1.easier_sleeping.config.DimensionListType;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.text.StringTextComponent;

import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;


//package-private
class DimensionListTypeParser {
	
	
	private static final DynamicCommandExceptionType DIMENSION_LIST_TYPE_INVALID =
		new DynamicCommandExceptionType( function -> new StringTextComponent( "Invalid Dimension List Type" ) );
	
	private static final Set<String> DIMENSION_LIST_TYPES = getItemKeySet();
	
	private final StringReader reader;
	
	private DimensionListType dimensionListType;
	
	private Function<SuggestionsBuilder, CompletableFuture<Suggestions>> suggestionsBuilder;
	
	//package-private
	DimensionListTypeParser( StringReader _reader ) {
		
		reader = _reader;
	}
	
	private static Set<String> getItemKeySet() {
		
		Set<String> keySet = new TreeSet<>();
		
		for( DimensionListType dimensionListType : DimensionListType.values() ) {
			keySet.add( dimensionListType.name() );
		}
		return keySet;
	}
	
	//package-private
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
	
	private Optional<DimensionListType> getItemForRegistry( String dimensionListTypeString ) {
		
		for( DimensionListType forDimensionListType : DimensionListType.values() ) {
			if( forDimensionListType.name().equals( dimensionListTypeString ) ) {
				return Optional.of( forDimensionListType );
			}
		}
		return Optional.empty();
	}
	
	//package-private
	@SuppressWarnings( "ReturnOfThis" )
	DimensionListTypeParser parse() throws CommandSyntaxException {
		
		suggestionsBuilder = this::suggestDimensionListType;
		readDimensionListType();
		suggestionsBuilder = this::suggestDimensionListTypeFuture;
		return this;
	}
	
	private CompletableFuture<Suggestions> suggestDimensionListTypeFuture( SuggestionsBuilder builder ) {
		
		return builder.buildFuture();
	}
	
	private CompletableFuture<Suggestions> suggestDimensionListType( SuggestionsBuilder builder ) {
		
		return ISuggestionProvider.suggest( DIMENSION_LIST_TYPES, builder );
	}
	
	//package-private
	CompletableFuture<Suggestions> fillSuggestions( SuggestionsBuilder builder ) {
		
		return suggestionsBuilder.apply( builder.createOffset( reader.getCursor() ) );
	}
}
