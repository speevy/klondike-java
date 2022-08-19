package speevy.cardGames.klondike;

import java.util.*;

import com.fasterxml.jackson.annotation.*;

import lombok.*;
import speevy.cardGames.*;
import speevy.cardGames.cardContainers.*;

/**
 * Each of the foundations of the game. Acts as a Card Origin an Card
 * Destination. Multiple cards can be peek or poke at a time. When poking one or
 * various cards, it has to alternate suit colors and the next rank has to be
 * the immediate previous value of the rank of the preceding card. If the
 * foundation is empty a KING of any suit is allowed.
 */
@EqualsAndHashCode
public class Foundation implements CardOrigin, CardDestination {
	@JsonProperty("hidden")
	private final List<Card> hidden;
	@JsonProperty("visible")
	private final ArrayList<Card> visible;
	@JsonProperty("peekCausedFlip")
	private final List<Boolean> peekCausedFlip;

	public Foundation() {
		super();
		hidden = new ArrayList<>();
		visible = new ArrayList<>();
		peekCausedFlip = new ArrayList<>();
	}
	
	public Foundation(List<Card> initialCards) {
		this();
		hidden.addAll(initialCards.subList(0, initialCards.size() - 1));
		visible.add(initialCards.get(initialCards.size() - 1));
	}

	@Override
	public void poke(Collection<Card> cards) throws IllegalStateException {
		if (cards.isEmpty()) {
			throw new PokeEmptyException();
		}
		Card firstCard = cards.stream().findFirst().get();
		
		if ((visible.isEmpty() && !firstCard.rank().isLast())
				|| (!visible.isEmpty() && !isDescendingAndAlternatingColors(
						visible.get(visible.size() -1), firstCard))) {
				throw new NotAlternatingDescendingException();
		}
		
		visible.addAll(cards);
	}
	
	boolean isDescendingAndAlternatingColors(Card a, Card b) {
		return !a.suit().getGroupName().equals(b.suit().getGroupName())
				&& a.rank().isImmediateNextOf(b.rank());
	}
	
	@Override
	public Collection<Card> undoPoke(int number) {
		List<Card> pokedCards = new ArrayList<>();
		for (int i = visible.size() - number; i < visible.size(); ) {
			pokedCards.add(visible.remove(i));
		}
		return pokedCards;
	}

	@Override
	public Collection<Card> peek(int number) throws IllegalStateException, IllegalArgumentException {
		if (number <= 0) {
			throw new InvalidPeekNumberException(number);
		}
		
		if (visible.size() < number) {
			throw new NotEnoughCardsToPeekException(visible.size(), number);
		}
		
		List<Card> peekedCards = new ArrayList<>();
		for (int i = visible.size() - number; i < visible.size(); ) {
			peekedCards.add(visible.remove(i));
		}
		
		if (visible.isEmpty() && !hidden.isEmpty()) {
			visible.add(hidden.remove(hidden.size() -1));
			peekCausedFlip.add(true);
		} else {
			peekCausedFlip.add(false);
		}
		
		return peekedCards;
	}

	@Override
	public void undoPeek(Collection<Card> cards) {
		if (!peekCausedFlip.isEmpty() 
				&& peekCausedFlip.remove(peekCausedFlip.size() - 1)
				&& visible.size() == 1) {
			hidden.add(visible.remove(0));
		}
		
		visible.addAll(cards);
	}
	
	@SuppressWarnings("unchecked")
	@JsonIgnore
	public FoundationStatus getStatus() {
		return new FoundationStatus(hidden.size(), 
				Collections.unmodifiableList((List<Card>) visible.clone()));
	}

	/**
	 * External view of Foundation status
	 */
	@Data
	@AllArgsConstructor
	public static class FoundationStatus{
		private final int numHidden;
		Collection<Card> visible; 
	}

	/**
	 * Constructor used to prepare test cases, do not use for other purposes.
	 */
	@Deprecated
	Foundation(Collection<Card> hidden, Collection<Card> visible) {
		this();
		this.hidden.addAll(hidden);
		this.visible.addAll(visible);
	}
	
	class NotAlternatingDescendingException extends IllegalStateException {

		private static final long serialVersionUID = -8286626283624013715L;

		public NotAlternatingDescendingException() {
			super("The cards on the foundation should be descending and alternating colors");
		}		
	}

	class InvalidPeekNumberException extends IllegalArgumentException {

		private static final long serialVersionUID = 4196497264580372951L;

		public InvalidPeekNumberException(int number) {
			super(String.format("Invalid number of cards to peek: %d", number));
		}
	}
	
	class PokeEmptyException extends IllegalArgumentException {

		private static final long serialVersionUID = 8724271928172394991L;

		public PokeEmptyException() {
			super("At least one card should be poke");
		}		
	}

	class NotEnoughCardsToPeekException extends IllegalStateException {

		private static final long serialVersionUID = 4383961994475229051L;

		public NotEnoughCardsToPeekException(int size, int number) {
			super(String.format("Invalid number of cards to peek: requested: %d, visible: %d", number, size));
		}		
	}

}