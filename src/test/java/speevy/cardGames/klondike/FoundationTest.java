package speevy.cardGames.klondike;
import static org.junit.jupiter.api.Assertions.*;
import static speevy.cardGames.AmericanCardRank.*;
import static speevy.cardGames.AmericanCardSuit.*;

import java.util.*;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import speevy.cardGames.*;
import speevy.cardGames.klondike.Foundation.FoundationStatus;

public class FoundationTest {

	@Test
	void foundationNew() {

		Foundation foundation = new Foundation(List.of(
				new Card(DIAMONDS, ACE),
				new Card(DIAMONDS, TWO),
				new Card(DIAMONDS, THREE)
				));
		
		assertEquals(new FoundationStatus(2, List.of(new Card(DIAMONDS, THREE))), 
				foundation.getStatus());

		foundation.peek(1);
		assertEquals(new FoundationStatus(1, List.of(new Card(DIAMONDS, TWO))), 
				foundation.getStatus());

		foundation.peek(1);
		assertEquals(new FoundationStatus(0, List.of(new Card(DIAMONDS, ACE))), 
				foundation.getStatus());

		foundation.peek(1);
		assertEquals(new FoundationStatus(0, Collections.emptyList()), 
				foundation.getStatus());
	}
	
	@Test
	void foundationNewOne() {
		Foundation foundation = new Foundation(List.of(new Card(DIAMONDS, ACE)));
		
		assertEquals(new FoundationStatus(0, List.of(new Card(DIAMONDS, ACE))), 
				foundation.getStatus());

		foundation.peek(1);
		assertEquals(new FoundationStatus(0, Collections.emptyList()), 
				foundation.getStatus());
	}
	
	@Test
	void foundationPeekOverflow() {
		Foundation foundation = createTestFoundation(3, 0, 1);
		assertThrows(IllegalStateException.class, () -> foundation.peek(2));

		assertThrows(IllegalArgumentException.class, () -> foundation.peek(0));
		assertThrows(IllegalArgumentException.class, () -> foundation.peek(-1));
	}
	
	@Test
	void foundationPoke() {
		foundationPokeCaseOk(0, 1, 1);
		foundationPokeCaseOk(0, 2, 1);
		foundationPokeCaseOk(0, 1, 2);
		foundationPokeCaseOk(1, 1, 1);
		foundationPokeCaseOk(2, 2, 2);
	}
	
	private void foundationPokeCaseOk(int visibleStart, int visibleNumber, int toAdd) {
		Foundation foundation = createTestFoundation(1, visibleStart, visibleNumber);
		var cards = generateDescendingAlColorStarting(visibleStart + visibleNumber, toAdd);
		
		foundation.poke(cards);
		
		assertEquals(generateDescendingAlColorStarting(visibleStart, visibleNumber + toAdd), 
				foundation.getStatus().visible());
	}

	@Test
	void foundationPokeKo() {
		// Last card in the test foundation is 10 CLUBS
		new AmericanCards().getAll().stream()
			.filter(card -> !card.equals(new Card (HEARTS, NINE)))
			.filter(card -> !card.equals(new Card (DIAMONDS, NINE)))
			.forEach(card -> {
				final Foundation foundation = createTestFoundation(1, 0, 4); //last card is 10 CLUBS
				final FoundationStatus status = foundation.getStatus();
				
				assertThrows(IllegalStateException.class, () -> foundation.poke(List.of(card)));
				assertEquals (status, foundation.getStatus());
			});
	}
	
	@Test
	void foundationStatus() {
		FoundationStatus status = createTestFoundation(5, 0, 2).getStatus();
		assertEquals(5, status.numHidden());
		assertEquals(2, status.visible().size());
	}
	
	@SuppressWarnings("deprecation")
	private Foundation createTestFoundation(int hiddenNumber, int visibleStart, int visibleNumber) {
		return new Foundation(createRandomCardSet(hiddenNumber),
				generateDescendingAlColorStarting(visibleStart, visibleNumber));
	}

	private final List<Card> alternating = List.of(
			new Card(HEARTS, KING),
			new Card(SPADES, QUEEN),
			new Card(DIAMONDS, JACK),
			new Card(CLUBS, TEN),
			new Card(DIAMONDS, NINE),
			new Card(CLUBS, EIGHT),
			new Card(HEARTS, SEVEN),
			new Card(SPADES, SIX),
			new Card(HEARTS, FIVE)
			);  

	Collection<Card> generateDescendingAlColorStarting(int start, int number) {
		return alternating.subList(start, start + number);
	}

	private Collection<Card> createRandomCardSet(int hiddenNumber) {
		List<Card> cards = new ArrayList<>(new AmericanCards().getAll());
		Collections.shuffle(cards);
		
		return cards.stream().limit(hiddenNumber).collect(Collectors.toList());
	}
	
	@Test
	void isDescendingAndAlternatingColors() {
		assertFalse(false);
		
		assertTrue(DescendingAndALternatingCase(DIAMONDS, FIVE, CLUBS, FOUR));
		assertFalse(DescendingAndALternatingCase(DIAMONDS, FIVE, HEARTS, FOUR));
		assertTrue(DescendingAndALternatingCase(DIAMONDS, FIVE, SPADES, FOUR));
		assertFalse(DescendingAndALternatingCase(DIAMONDS, FIVE, DIAMONDS, FOUR));
		assertFalse(DescendingAndALternatingCase(DIAMONDS, FIVE, CLUBS, THREE));
		assertFalse(DescendingAndALternatingCase(DIAMONDS, FIVE, HEARTS, THREE));
		assertFalse(DescendingAndALternatingCase(DIAMONDS, FIVE, SPADES, THREE));
		assertFalse(DescendingAndALternatingCase(DIAMONDS, FIVE, DIAMONDS, THREE));
	}

	private boolean DescendingAndALternatingCase(CardSuit suit1, CardRank rank1, CardSuit suit2, CardRank rank2) {
		return createTestFoundation(1, 1, 1)
				.isDescendingAndAlternatingColors(new Card(suit1, rank1), new Card (suit2, rank2));
	}

	@Test
	void foundationUndoPeek() {
		foundationUndoPeekCase(3, 1, 1);
		foundationUndoPeekCase(3, 2, 1);
		foundationUndoPeekCase(3, 2, 2);
		foundationUndoPeekCase(1, 1, 1);
		foundationUndoPeekCase(0, 1, 1);
		foundationUndoPeekCase(0, 2, 1);
		foundationUndoPeekCase(0, 2, 2);
	}

	private void foundationUndoPeekCase(int hidden, int visible, int peek) {
		final Foundation foundation = createTestFoundation(hidden, 0, visible);
		final FoundationStatus status = foundation.getStatus();
		final Collection<Card> cards = foundation.peek(peek);
		
		foundation.undoPeek(cards);
		
		assertEquals(status, foundation.getStatus());
	}
	
	@Test
	void foundationUndoPoke() {
		foundationUndoPokeCase(0, 1, 1);
        foundationUndoPokeCase(0, 2, 1);
        foundationUndoPokeCase(0, 1, 2);
        foundationUndoPokeCase(1, 1, 1);
        foundationUndoPokeCase(2, 2, 2);
	}

	private void foundationUndoPokeCase(int visibleStart, int visibleSize, int toAdd) {
		final Foundation foundation = createTestFoundation(1, visibleStart, visibleSize);
		final Collection<Card> cards = generateDescendingAlColorStarting(visibleStart + visibleSize, toAdd);
		final FoundationStatus status = foundation.getStatus();
		
		foundation.poke(cards);
		
		assertEquals(cards, foundation.undoPoke(toAdd));
		assertEquals(status, foundation.getStatus());
	}
}
