package speevy.cardGames.klondike;

import static org.junit.jupiter.api.Assertions.*;
import static speevy.cardGames.AmericanCardRank.*;
import static speevy.cardGames.AmericanCardSuit.*;
import static org.mockito.Mockito.*;
import static speevy.cardGames.klondike.Klondike.CardHolderType.*;

import java.util.*;
import java.util.function.*;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import lombok.*;
import speevy.cardGames.*;
import speevy.cardGames.cardContainers.*;
import speevy.cardGames.klondike.Klondike.*;

@SuppressWarnings("deprecation")
public class KlondikeTest {

	@Test
	void klondikeNew() {
		Klondike klondike = new Klondike(new AmericanCards());
		KlondikeStatus status = klondike.getStatus();
		
		assertEquals(4, status.piles().size());
		for (var pileStatus : status.piles()) {
			assertEquals(0, pileStatus.numCards());
			assertTrue(pileStatus.topCard().isEmpty());
		}
		
		assertEquals(7, status.foundations().size());
		for (int i = 0; i < status.foundations().size(); i++) {
			var foundationStatus = status.foundations().get(i);
			assertEquals(i, foundationStatus.numHidden());
			assertEquals(1, foundationStatus.visible().size());
		}
		
        // The remaining cards are on the deck
		// 52 - 1 - 2 - 3 - 4 - 5 - 6 - 7 = 24
		assertEquals(24, status.deck().cardsOnStock() + status.deck().cardsOnWaste()); 
		
		//TODO: check cards not repeated and randomized (if possible)
	}
	
	@Test
	void klondikeImpossibleMovements() {
		Klondike klondike = new Klondike(new AmericanCards());

		// Can't move cards to the same cardHolder
		assertThrows(IllegalArgumentException.class, () -> 
			klondike.moveCards(new CardHolder(PILE, 2), new CardHolder(PILE, 2), 1));

		assertThrows(IllegalArgumentException.class, () -> 
			klondike.moveCards(new CardHolder(FOUNDATION, 1), new CardHolder(FOUNDATION, 1), 1));

		// Can't move cards to the deck
		assertThrows(IllegalArgumentException.class, () -> 
			klondike.moveCards(new CardHolder(PILE, 0), new CardHolder(DECK), 1));

		assertThrows(IllegalArgumentException.class, () -> 
		klondike.moveCards(new CardHolder(FOUNDATION, 0), new CardHolder(DECK), 1));

		// Out of bounds card holders
		assertThrows(IllegalArgumentException.class, () -> 
			klondike.moveCards(new CardHolder(PILE, -1), new CardHolder(DECK), 1));

		assertThrows(IllegalArgumentException.class, () -> 
			klondike.moveCards(new CardHolder(PILE, 1), new CardHolder(DECK, 1), 1));

		assertThrows(IllegalArgumentException.class, () -> 
			klondike.moveCards(new CardHolder(PILE, 1), new CardHolder(PILE, 4), 1));

		assertThrows(IllegalArgumentException.class, () -> 
			klondike.moveCards(new CardHolder(PILE, 1), new CardHolder(FOUNDATION, 7), 1));

		assertThrows(IllegalArgumentException.class, () -> 
			klondike.moveCards(new CardHolder(PILE, 1), new CardHolder(FOUNDATION, -1), 1));
	}
	
	@Data
	@AllArgsConstructor
	private static class KlondikeMockCardHolders {
		private final Deck deck;
		private final List<Pile> piles;
		private final List<Foundation> foundations;

		KlondikeMockCardHolders() {
			this(mock(Deck.class), new ArrayList<>(), new ArrayList<>());
			
			for (int i = 0; i < 4; i++) {
				piles.add(mock(Pile.class));
			}
			
			for (int i = 0; i < 7; i++) {
				foundations.add(mock(Foundation.class));
			}
		}
	}

	private void noMoreInteractions(final KlondikeMockCardHolders mocks) {
		verifyNoMoreInteractions(mocks.deck());
		mocks.piles().forEach(Mockito::verifyNoMoreInteractions);
		mocks.foundations().forEach(Mockito::verifyNoMoreInteractions);
	}
	
	@Test
	void klondikeCardMovement() {
		
		klondikeCardMovement(
				mocks -> mocks.foundations().get(0), 
				mocks -> mocks.foundations().get(1),
				new CardHolder(FOUNDATION, 0), new CardHolder(FOUNDATION, 1),
				1
			);

		klondikeCardMovement(
				mocks -> mocks.foundations().get(0), 
				mocks -> mocks.piles().get(1),
				new CardHolder(FOUNDATION, 0), new CardHolder(PILE, 1),
				1
			);

		klondikeCardMovement(
				mocks -> mocks.piles().get(2), mocks -> mocks.piles().get(1),
				new CardHolder(PILE, 2), new CardHolder(PILE, 1),
				5
			);

		klondikeCardMovement(
				mocks -> mocks.piles().get(2), mocks -> mocks.foundations().get(1),
				new CardHolder(PILE, 2), new CardHolder(FOUNDATION, 1),
				5
			);

		klondikeCardMovement(
				mocks -> mocks.deck(), mocks -> mocks.foundations().get(1),
				new CardHolder(DECK), new CardHolder(FOUNDATION, 1),
				5
			);

		klondikeCardMovement(
				mocks -> mocks.deck(), mocks -> mocks.piles().get(1),
				new CardHolder(DECK), new CardHolder(PILE, 1),
				2
			);
	}
	
	void klondikeCardMovement(
			final Function<KlondikeMockCardHolders, CardOrigin> origin, 
			final Function<KlondikeMockCardHolders, CardDestination> destination, 
			final CardHolder from,
			final CardHolder to,
			final int numCards
			) {
		klondikeCardMovementOk(origin, destination, from, to, numCards);
		klondikeCardMovementFail1(origin, destination, from, to, numCards, IllegalArgumentException.class);
		klondikeCardMovementFail2(origin, destination, from, to, numCards, IllegalArgumentException.class);
		klondikeCardMovementFail1(origin, destination, from, to, numCards, IllegalStateException.class);
		klondikeCardMovementFail2(origin, destination, from, to, numCards, IllegalStateException.class);
	}
	
	void klondikeCardMovementOk(
			final Function<KlondikeMockCardHolders, CardOrigin> origin, 
			final Function<KlondikeMockCardHolders, CardDestination> destination, 
			final CardHolder from,
			final CardHolder to,
			final int numCards
			) {
		final KlondikeMockCardHolders mocks = new KlondikeMockCardHolders();
		final Collection<Card> cards = FoundationTest.generateDescendingAlColorStarting(0, 5);
		when(origin.apply(mocks).peek(cards.size())).thenReturn(cards);
		
		Klondike klondike = new Klondike(mocks.deck(), mocks.piles(), mocks.foundations());
		klondike.moveCards(from, to, cards.size());
		
		verify(origin.apply(mocks)).peek(cards.size());
		verify(destination.apply(mocks)).poke(eq(cards));
		noMoreInteractions(mocks);
	}

	<T extends Throwable> void klondikeCardMovementFail1(
			final Function<KlondikeMockCardHolders, CardOrigin> origin, 
			final Function<KlondikeMockCardHolders, CardDestination> destination, 
			final CardHolder from,
			final CardHolder to,
			final int numCards, 
			Class<T> exception
			) {
		final KlondikeMockCardHolders mocks = new KlondikeMockCardHolders();
		final Collection<Card> cards = FoundationTest.generateDescendingAlColorStarting(0, 5);
		when(origin.apply(mocks).peek(cards.size())).thenThrow(exception);
		
		Klondike klondike = new Klondike(mocks.deck(), mocks.piles(), mocks.foundations());
		assertThrows (exception, 
				() -> klondike.moveCards(from, to, cards.size()));
		
		verify(origin.apply(mocks)).peek(cards.size());
		noMoreInteractions(mocks);
	}

	<T extends Throwable> void klondikeCardMovementFail2(
			final Function<KlondikeMockCardHolders, CardOrigin> origin, 
			final Function<KlondikeMockCardHolders, CardDestination> destination, 
			final CardHolder from,
			final CardHolder to,
			final int numCards, 
			Class<T> exception
			) {
		final KlondikeMockCardHolders mocks = new KlondikeMockCardHolders();
		final Collection<Card> cards = FoundationTest.generateDescendingAlColorStarting(0, 5);
		when(origin.apply(mocks).peek(cards.size())).thenReturn(cards);
		doThrow(exception).when(destination.apply(mocks)).poke(any());
		
		Klondike klondike = new Klondike(mocks.deck(), mocks.piles(), mocks.foundations());
		assertThrows (exception, 
				() -> klondike.moveCards(from, to, cards.size()));
		
		verify(origin.apply(mocks)).peek(cards.size());
		verify(origin.apply(mocks)).undoPeek(eq(cards));
		verify(destination.apply(mocks)).poke(eq(cards));
		noMoreInteractions(mocks);
	}

	@Test
	void klondikeTake() {
		final KlondikeMockCardHolders mocks = new KlondikeMockCardHolders();
		Klondike klondike = new Klondike(mocks.deck(), mocks.piles(), mocks.foundations());
		
		klondike.take();
		
		verify(mocks.deck()).take();
		noMoreInteractions(mocks);
	}
	
	@Test
	void klondikeToPile() {
		for (int pile = 0; pile < 5; pile++) {
			klondikeToPile(pile, mocks -> mocks.deck(), new CardHolder(DECK));
			
			klondikeToPile(pile, mocks -> mocks.foundations().get(0),
					new CardHolder(FOUNDATION, 0));
			
			klondikeToPile(pile, mocks -> mocks.foundations().get(1),
					new CardHolder(FOUNDATION, 1));
			
			klondikeToPile(pile, mocks -> mocks.foundations().get(2),
					new CardHolder(FOUNDATION, 2));
		}
	}
	
	// pileAcceptIndex == 4: no pile accepts the card
	void klondikeToPile(int pileAcceptIndex, Function<KlondikeMockCardHolders, CardOrigin> origin, CardHolder from) {
		final KlondikeMockCardHolders mocks = new KlondikeMockCardHolders();
		final Collection<Card> cards = List.of(new Card(HEARTS, FOUR));
		
		final CardOrigin theOrigin = origin.apply(mocks);
		when(theOrigin.peek(cards.size())).thenReturn(cards);
		for (int i = 0; i < mocks.piles().size(); i++) {
			if (i != pileAcceptIndex) {
				doThrow(IllegalStateException.class).when(mocks.piles.get(i)).poke(any());
			}
		}
		
		Klondike klondike = new Klondike(mocks.deck(), mocks.piles(), mocks.foundations());
		if (pileAcceptIndex == 4) {
			assertThrows(IllegalStateException.class, () -> klondike.toPile(from));
			// In this case, the poke function should be called on all the piles
			for (int i = 0; i < pileAcceptIndex; i++) {
				verify(mocks.piles.get(i)).poke(any());
			}			
		} else {
			klondike.toPile(from);
			// The poke function has to be called on the accepting pile
			// and may or may not be called on the others
			for (int i = 0; i < mocks.piles().size(); i++) {
				if (i != pileAcceptIndex) {
					verify(mocks.piles.get(i), atMost(1)).poke(any());
				} else {
					verify(mocks.piles.get(pileAcceptIndex)).poke(any());
				}
			}
		}
		
		// In case NO pile accepts the cards, all the peeks should be undone
		int undoPeeks = (int) mockingDetails(theOrigin).getInvocations().stream()
				.filter(invocation -> invocation.getMethod().getName().equals("peek"))
				.count();
		// In case a pile accepts the cards, all the peeks BUT ONE should be undone
		if (pileAcceptIndex != 4) {
			undoPeeks--;
		}
		
		verify(theOrigin, atLeast(1)).peek(cards.size());
		verify(theOrigin, times(undoPeeks)).undoPeek(eq(cards));
		noMoreInteractions(mocks);
	}

	@Test
	void klondikeUndo() {
		final Random random = new Random();
		final Klondike klondike = new Klondike(new AmericanCards());
		final List<KlondikeStatus> statusHistory = new ArrayList<>();
		statusHistory.add(klondike.getStatus());
		
		for (int i = 0; i < 500; i++) {
			final int from = random.nextInt(12);

			int tmp;
			do {
				tmp = random.nextInt(12);
			} while (tmp == from || tmp == 7); //7 = Deck
			final int to = tmp;

			final int number;
			if (from < 7 && to < 7 ) { // <7 = Foundations
				number = random.nextInt(2) + 1;
			} else {
				number = 1;
			}
			
			execute (klondike, statusHistory, k -> k.moveCards(getCardHolder(from), getCardHolder(to), number));
			
			execute (klondike, statusHistory, k -> k.toPile(getCardHolder(random.nextInt(8))));
			
			execute (klondike, statusHistory, k -> k.take());	
		}
		
		Collections.reverse(statusHistory);
		
		for (var status: statusHistory) {
			assertEquals(status, klondike.getStatus());
			klondike.undo();
		}
		
	}

	private void execute(Klondike klondike, List<KlondikeStatus> statusHistory, 
			Consumer<Klondike> command) {
		try {
			command.accept(klondike);
			statusHistory.add(klondike.getStatus());
		} catch (IllegalStateException e) {
			assertEquals(statusHistory.get(statusHistory.size() -1), klondike.getStatus());
		}
	}
	
	private CardHolder getCardHolder(int to) {
		if (to < 7) return new CardHolder(CardHolderType.FOUNDATION, to);
		if (to == 7) return new CardHolder(CardHolderType.DECK);
		if (to > 7) return new CardHolder(CardHolderType.PILE, to - 8);
		throw new IllegalArgumentException();
	}
	
	public static Klondike createTestKlondike() {
		return new Klondike(
				new Deck(List.of(new Card(HEARTS, ACE), new Card(DIAMONDS, ACE))), 
				List.of(new Pile(), new Pile(), new Pile(), new Pile()),
				List.of (
					new Foundation(List.of(new Card(CLUBS, ACE)), List.of(new Card(CLUBS, TEN), new Card(HEARTS, NINE))),
					new Foundation(List.of(), List.of(new Card(DIAMONDS, JACK))),
					new Foundation(List.of(), List.of(new Card(DIAMONDS, KING))),
					new Foundation(List.of(), List.of(new Card(SPADES, ACE))),
					new Foundation(List.of(), List.of()),
					new Foundation(List.of(), List.of()),
					new Foundation(List.of(), List.of())
				));
	}
}
