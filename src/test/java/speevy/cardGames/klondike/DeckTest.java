package speevy.cardGames.klondike;

import static org.junit.jupiter.api.Assertions.*;
import static speevy.cardGames.AmericanCardRank.*;
import static speevy.cardGames.AmericanCardSuit.*;

import java.util.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import speevy.cardGames.*;
import speevy.cardGames.cardContainers.CardContainersTest;
import speevy.cardGames.klondike.Deck.*;


public class DeckTest {

	@Test
	void deckInit() {
		final Deck deck = new Deck(List.of(
				new Card(DIAMONDS, ACE),
				new Card(DIAMONDS, TWO),
				new Card(DIAMONDS, THREE),
				new Card(CLUBS, ACE),
				new Card(CLUBS, TWO),
				new Card(CLUBS, THREE)
				));
		
		assertEquals(deck.stock.size(), 5);
		assertEquals(deck.waste.size(), 1);
		assertEquals(new Card(CLUBS, THREE), deck.waste.get(0));
	}
	
	@Test
	void deckTake() {
		final Deck deck = createTestDeck();
		
		deck.take();
		assertDeck(deck, 2, 4, DIAMONDS, THREE);
		
		deck.take();
		assertDeck(deck, 1, 5, DIAMONDS, TWO);
		
		deck.take();
		assertDeck(deck, 0, 6, DIAMONDS, ACE);
		
		deck.take();
		assertDeck(deck, 5, 1, CLUBS, ACE);
		
		deck.take();
		assertDeck(deck, 4, 2, CLUBS, TWO);
		
		deck.take();
		assertDeck(deck, 3, 3, CLUBS, THREE);
		
		deck.take();
		assertDeck(deck, 2, 4, DIAMONDS, THREE);
	}
	
	private void assertDeck(Deck deck, int stockSize, int wasteSize, CardSuit suit, CardRank rank) {
		assertEquals(stockSize, deck.stock.size());		
		assertEquals(wasteSize, deck.waste.size());
		assertEquals(new Card(suit, rank), deck.waste.get(wasteSize - 1));
	}

	public static Deck createTestDeck() {
		final Deck deck = new Deck();
		
		deck.stock.add(new Card(DIAMONDS, ACE));
		deck.stock.add(new Card(DIAMONDS, TWO));
		deck.stock.add(new Card(DIAMONDS, THREE));

		deck.waste.add(new Card(CLUBS, ACE));
		deck.waste.add(new Card(CLUBS, TWO));
		deck.waste.add(new Card(CLUBS, THREE));

		return deck;
	}
	
	@Test
	void deckTakeEmpty() {
		final Deck deck = new Deck();
		
		deck.take();
		assertTrue(deck.stock.isEmpty());		
		assertTrue(deck.waste.isEmpty());
	}
	
	@Test
	void deckPeekOne() {
		final Deck deck = createTestDeck();
		
		CardContainersTest.assertPeekOneReturns(deck, new Card(CLUBS, THREE));
		CardContainersTest.assertPeekOneReturns(deck, new Card(CLUBS, TWO));
		CardContainersTest.assertPeekOneReturns(deck, new Card(CLUBS, ACE));
		
		CardContainersTest.assertPeekFails(deck, 1, IllegalStateException.class);
		
	}
	
	@ParameterizedTest
	@ArgumentsSource(Random100Integers.class)
	void deckPeekNotOne(int value) {
		if (value != 1) {
			final Deck deck = createTestDeck();
			CardContainersTest.assertPeekFails(deck, value, TakeMoreThanOneFromWasteException.class);
		}
	}
	
	@Test
	void deckStatus() {
		final Deck deck = createTestDeck();
		checkStatus(deck.getStatus(), 3, 3, CLUBS, THREE);
		
		deck.dryPeek(1);
		checkStatus(deck.getStatus(), 3, 3, CLUBS, THREE);

		deck.peek(1);
		checkStatus(deck.getStatus(), 3, 2, CLUBS, TWO);

		deck.dryPeek(1);
		checkStatus(deck.getStatus(), 3, 2, CLUBS, TWO);

		deck.peek(1);
		checkStatus(deck.getStatus(), 3, 1, CLUBS, ACE);
		
		deck.peek(1);
		checkStatus(deck.getStatus(), 3, 0, null, null);

		deck.take();
		deck.take();
		deck.take();
		checkStatus(deck.getStatus(), 0, 3, DIAMONDS, ACE);

	}
	
	void checkStatus(DeckStatus status, int cardsOnStock, int cardsOnWaste, AmericanCardSuit suit, AmericanCardRank rank) {
		assertEquals(cardsOnStock, status.cardsOnStock());
		assertEquals(cardsOnWaste, status.cardsOnWaste());
		if (suit != null && rank != null) {
			assertEquals(Optional.of(new Card(suit, rank)), status.topCardOnWaste());
		} else {
		}
	}
	
	@Test
	void deckUndoTake() {
		final int numberOfUndos = 10;
		final Deck deck = createTestDeck();
		final ArrayList<DeckStatus> history = new ArrayList<>();
		
		for (int i = 0; i < numberOfUndos; i++) {
			history.add(deck.getStatus());
			deck.take();
		}
		
		for (int i = numberOfUndos - 1; i >= 0; i--) {
			deck.undoTake();
			assertEquals(history.get(i), deck.getStatus());
		}
	}
	
	@Test
	void deckUndoPeek() {
		final int numberOfUndos = 3;
		final Deck deck = createTestDeck();
		final ArrayList<DeckStatus> history = new ArrayList<>();
		final ArrayList<Collection<Card>> historyCards = new ArrayList<>();
		
		for (int i = 0; i < numberOfUndos; i++) {
			history.add(deck.getStatus());
			historyCards.add(deck.peek(1));
		}
		
		for (int i = numberOfUndos - 1; i >= 0; i--) {
			deck.undoPeek(historyCards.get(i));
			assertEquals(history.get(i), deck.getStatus());
		}	
	}
	
	@Test
	void deckUndoTakeWasteEmpty() {
		final Deck deck = createTestDeck();
		deck.peek(1);
		deck.peek(1);
		
		DeckStatus status0 = deck.getStatus();
		final Collection<Card> cards = deck.peek(1);
		DeckStatus status1 = deck.getStatus();
		deck.take();
		DeckStatus status2 = deck.getStatus();
		deck.take();

		deck.undoTake();
		assertEquals(status2, deck.getStatus());
		
		deck.undoTake();
		assertEquals(status1, deck.getStatus());
		
		deck.undoPeek(cards);
		assertEquals(status0, deck.getStatus());		
	}
	
	@Test
	void deckUndoTakeFlip() {
		final Deck deck = createTestDeck();
		final List<DeckStatus> statusHistory = new ArrayList<>();

		do {
			statusHistory.add(deck.getStatus());
			deck.take();
		} while (deck.stock.size() != 4);
		
		Collections.reverse(statusHistory);
		
		for (var status : statusHistory) {
			deck.undoTake();
			assertEquals(status, deck.getStatus());
		}
	}
}
