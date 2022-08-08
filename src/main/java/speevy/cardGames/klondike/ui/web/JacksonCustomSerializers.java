package speevy.cardGames.klondike.ui.web;

import java.io.IOException;
import java.util.Optional;

import org.springframework.context.annotation.*;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import speevy.cardGames.Card;
import speevy.cardGames.klondike.Deck.DeckStatus;
import speevy.cardGames.klondike.Foundation.FoundationStatus;
import speevy.cardGames.klondike.Klondike.KlondikeStatus;
import speevy.cardGames.klondike.Pile.PileStatus;

@Configuration
public class JacksonCustomSerializers {
	@SuppressWarnings("rawtypes")
	@Bean
	@Primary
	public ObjectMapper objectMapper() {
		SimpleModule module = new SimpleModule();
	    
		module.addSerializer(new StdSerializer<Card>(Card.class) {
			private static final long serialVersionUID = -4130760262915139044L;

			@Override
			public void serialize(Card value, JsonGenerator gen, SerializerProvider provider) throws IOException {
				gen.writeStartObject();
				gen.writeStringField("suit", value.suit().getName());
				gen.writeStringField("rank", value.rank().getName());
				gen.writeEndObject();
				
			}
			
		});

		module.addSerializer(new StdSerializer<DeckStatus>(DeckStatus.class) {

			private static final long serialVersionUID = 2642294643667613929L;

			@Override
			public void serialize(DeckStatus value, JsonGenerator gen, SerializerProvider provider) throws IOException {
				gen.writeStartObject();
				gen.writeNumberField("cardsOnWaste", value.cardsOnWaste());
				gen.writeNumberField("cardsOnStock", value.cardsOnStock());
				gen.writePOJOField("topCardOnWaste", value.topCardOnWaste());
				gen.writeEndObject();
				
			}
			
		});

		module.addSerializer(new StdSerializer<PileStatus>(PileStatus.class) {

			private static final long serialVersionUID = -3126676206194648333L;

			@Override
			public void serialize(PileStatus value, JsonGenerator gen, SerializerProvider provider) throws IOException {
				gen.writeStartObject();
				gen.writeNumberField("numCards", value.numCards());
				gen.writePOJOField("topCard", value.topCard());
				gen.writeEndObject();			
			}
			
		});

		module.addSerializer(new StdSerializer<KlondikeStatus>(KlondikeStatus.class) {

			private static final long serialVersionUID = -57461909847291788L;

			@Override
			public void serialize(KlondikeStatus value, JsonGenerator gen, SerializerProvider provider) throws IOException {
				gen.writeStartObject();
				gen.writePOJOField("deck", value.deck());
				gen.writePOJOField("piles", value.piles());
				gen.writePOJOField("foundations", value.foundations());
				gen.writeEndObject();			
			}
			
		});


		module.addSerializer(new StdSerializer<FoundationStatus>(FoundationStatus.class) {

			private static final long serialVersionUID = -3126676206194648333L;

			@Override
			public void serialize(FoundationStatus value, JsonGenerator gen, SerializerProvider provider) throws IOException {
				gen.writeStartObject();
				gen.writeNumberField("numHidden", value.numHidden());
				gen.writePOJOField("visible", value.visible());
				gen.writeEndObject();			
			}
			
		});


	    module.addSerializer(new StdSerializer<Optional>(Optional.class) {

			private static final long serialVersionUID = 1L;

			@Override
			public void serialize(Optional value, JsonGenerator gen, SerializerProvider provider) throws IOException {
				if(value.isPresent()) {
					gen.writePOJO(value.get());
				} else {
					gen.writeNull();
				}
			}
			
		});

	    return new ObjectMapper()
	      .registerModule(module);
	}
}
