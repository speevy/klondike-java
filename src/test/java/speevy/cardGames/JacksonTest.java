package speevy.cardGames;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;

import speevy.cardGames.klondike.*;
import speevy.cardGames.klondike.Klondike.*;

public class JacksonTest {
	@Test void testCardJackson() throws JacksonException{
		testJackson(Card.class, new Card(AmericanCardSuit.CLUBS, AmericanCardRank.ACE));
	}
	
	@Test void testPileJackson() throws JacksonException {
		testJackson(Pile.class, PileTest.createTestPile());
	}

	@Test void testDeckJackson() throws JacksonException {
		testJackson(Deck.class, DeckTest.createTestDeck());
	}

	@Test void testFoundationJackson() throws JacksonException {
		testJackson(Foundation.class, FoundationTest.createTestFoundation(3, 0, 5));
	}

	@Test void testCardHolderJackson() throws JacksonException {
		for (CardHolder cardHolder : List.of(
				new Klondike.CardHolder(CardHolderType.DECK),
				new Klondike.CardHolder(CardHolderType.PILE, 0), new Klondike.CardHolder(CardHolderType.PILE, 2),
				new Klondike.CardHolder(CardHolderType.FOUNDATION, 1),
				new Klondike.CardHolder(CardHolderType.FOUNDATION, 3))
			) {

			testJackson(CardHolder.class, cardHolder);

		}
	}

	<T> void testJackson(Class<T> clazz, T obj) throws JacksonException {
		final ObjectMapper mapper = new ObjectMapper();
				
		final String serialized = mapper.writeValueAsString(obj);
		System.out.println(serialized);
		
		assertEquals(obj, mapper.readValue(serialized, clazz));
	}

}
