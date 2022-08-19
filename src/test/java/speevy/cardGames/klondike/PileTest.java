package speevy.cardGames.klondike;

import static org.junit.jupiter.api.Assertions.*;
import static speevy.cardGames.AmericanCardRank.*;
import static speevy.cardGames.AmericanCardSuit.*;

import java.util.*;
import java.util.stream.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import speevy.cardGames.*;
import speevy.cardGames.cardContainers.CardContainersTest;
import speevy.cardGames.klondike.Pile.PileStatus;


public class PileTest {

	@Test
	void pilePeekOne() {
		final Pile pile = createTestPile();
		
		CardContainersTest.assertPeekOneReturns(pile, new Card(DIAMONDS, THREE));
		CardContainersTest.assertPeekOneReturns(pile, new Card(DIAMONDS, TWO));
		CardContainersTest.assertPeekOneReturns(pile, new Card(DIAMONDS, ACE));
		
		assertThrows(IllegalStateException.class, () -> pile.peek(1));
		
	}
	
	@SuppressWarnings("deprecation")
	public static Pile createTestPile() {
		final Pile pile = new Pile();
		pile.addCards(List.of(
				new Card (DIAMONDS, ACE),
				new Card (DIAMONDS, TWO),
				new Card (DIAMONDS, THREE)
				));
		
		return pile;
	}
	
	@ParameterizedTest
	@ArgumentsSource(Random100Integers.class)
	void pilePeekNotOne(int value) {
		if (value != 1) {
			final Pile pile = createTestPile();
			assertThrows(IllegalArgumentException.class, () -> pile.peek(value));
		}
	}
	
	@Test
	void pileEmptyPoke() {
		for (var suit: AmericanCardSuit.values()) {
			for (var rank : AmericanCardRank.values()) {
				if (rank.equals(ACE)) {
					pilePokeCard(new Pile(), suit, rank);
				} else {
					pilePokeCardFail(new Pile(), suit, rank);
				}
			}
		}
	}
	
	void pilePokeCard(Pile pile, AmericanCardSuit suit, AmericanCardRank rank) {
		final int beforeSize = pile.getStatus().numCards();
		final Card card = new Card (suit, rank);
		
		pile.poke(List.of(card));
		
		assertEquals(beforeSize + 1, pile.getStatus().numCards());
		assertEquals(card, pile.getStatus().topCard().get());
	}

	void pilePokeCardFail(Pile pile, AmericanCardSuit suit, AmericanCardRank rank) {
		final int beforeSize = pile.getStatus().numCards();
		final Card card = new Card (suit, rank);
		
		assertThrows(IllegalStateException.class, () -> pile.poke(List.of(card)));
		
		assertEquals(beforeSize, pile.getStatus().numCards());
	}
	
	@Test
	void pilePokeNextRank() {
		for (var suit: AmericanCardSuit.values()) {
			if (suit.equals(DIAMONDS)) {
				pilePokeCard(createTestPile(), suit, FOUR);
			} else {
				pilePokeCardFail(createTestPile(), suit, FOUR);
			}
		}
	}
	
	@Test
	void pileStatus() {
		final Pile pile = createTestPile();
		
		assertEquals(new PileStatus(3, Optional.of(new Card(DIAMONDS, THREE))), pile.getStatus());
		
		pile.peek(1);
		assertEquals(new PileStatus(2, Optional.of(new Card(DIAMONDS, TWO))), pile.getStatus());
		
		pile.peek(1);
		assertEquals(new PileStatus(1, Optional.of(new Card(DIAMONDS, ACE))), pile.getStatus());

		pile.peek(1);
		assertEquals(new PileStatus(0, Optional.empty()), pile.getStatus());
	}
	
	@Test
	void pileUndoPeek() {
		final Pile pile = createTestPile();
		final List<PileStatus> historyStatus = new ArrayList<>();
		final List<Collection<Card>> historyCards = new ArrayList<>();
		
		for (int i = 0; i < 3; i++) {
			historyStatus.add(pile.getStatus());
			historyCards.add(pile.peek(1));
		}
		
		for (int i = 2; i >= 0; i--) {
			pile.undoPeek(historyCards.get(i));
			assertEquals(historyStatus.get(i), pile.getStatus());
		}
	}
	
	@Test
	void pileUndoPoke() {
		List<List<Card>> cards = Stream.of(ACE, TWO, THREE)
				.map(rank -> new Card(HEARTS, rank))
				.map(List::of)
				.collect(Collectors.toList());
		
		final Pile pile = new Pile();
		for (var card : cards) {
			pile.poke(card);
		}
		
		for (int i = cards.size() - 1; i >= 0; i--) {
			assertEquals(cards.get(i), pile.undoPoke(1));
			assertEquals(i, pile.getStatus().numCards());
		}
	}
}
