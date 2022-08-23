package speevy.cardGames.cardContainers;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;

import lombok.*;
import speevy.cardGames.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CardContainersTest {
	
	public static void assertPeekOneReturns(CardOrigin origin, Card card) {
		assertOneResult(card, origin.dryPeek(1));
		assertOneResult(card, origin.peek(1));
	}

	private static void assertOneResult(Card card, Collection<Card> result) {
		assertEquals(1, result.size());
		assertEquals(card, result.stream().findFirst().get());
	}
	
	public static <T extends Exception> void assertPeekFails(CardOrigin origin, int numberOfCards, Class<T> exception) {
		assertTrue(origin.dryPeek(numberOfCards).isEmpty());
		assertThrows(exception, () -> origin.peek(numberOfCards));
	}
}
