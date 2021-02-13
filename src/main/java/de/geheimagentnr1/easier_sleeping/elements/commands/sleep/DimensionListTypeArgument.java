package de.geheimagentnr1.easier_sleeping.elements.commands.sleep;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.geheimagentnr1.easier_sleeping.config.DimensionListType;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;


public class DimensionListTypeArgument implements ArgumentType<DimensionListType> {
	
	
	public static final String registry_name = "dimension_list_type";
	
	private static final Collection<String> EXAMPLES = Arrays.asList(
		DimensionListType.SLEEP_ACTIVE.name(),
		DimensionListType.SLEEP_INACTIVE.name()
	);
	
	public static DimensionListTypeArgument dimensionListType() {
		
		return new DimensionListTypeArgument();
	}
	
	//package-private
	@SuppressWarnings( "SameParameterValue" )
	static <S> DimensionListType getDimensionListType( CommandContext<S> context, String name ) {
		
		return context.getArgument( name, DimensionListType.class );
	}
	
	@Override
	public DimensionListType parse( StringReader reader ) throws CommandSyntaxException {
		
		DimensionListTypeParser parser = new DimensionListTypeParser( reader ).parse();
		return parser.getDimensionListType();
	}
	
	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(
		CommandContext<S> context,
		SuggestionsBuilder builder ) {
		
		StringReader reader = new StringReader( builder.getInput() );
		reader.setCursor( builder.getStart() );
		DimensionListTypeParser parser = new DimensionListTypeParser( reader );
		
		try {
			parser.parse();
		} catch( CommandSyntaxException ignored ) {
		}
		return parser.fillSuggestions( builder );
	}
	
	@Override
	public Collection<String> getExamples() {
		
		return EXAMPLES;
	}
}
