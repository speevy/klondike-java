package speevy.cardGames.klondike;

import java.util.*;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.*;
import speevy.cardGames.Card;
import speevy.cardGames.cardContainers.CardOrigin;

/**
 * The deck of the game, consisting in two piles: the stock and the waste.
 * The waste also acts as a CardOrigin.
 */
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class Deck implements CardOrigin {
	final List<Card> stock = new ArrayList<>();
	final List<Card> waste = new ArrayList<>();
	final List<Boolean> takeCausedFlip = new ArrayList<>();
	
	public Deck(Collection<Card> cards) {
		stock.addAll(cards);
		take();
	}

	void take() {
		if (stock.isEmpty() && !waste.isEmpty()) {
			stock.addAll(waste);
			waste.clear();
			Collections.reverse(stock);
			takeCausedFlip.add(true);
		} else {
			takeCausedFlip.add(false);
		}

		if (!stock.isEmpty()) {
			waste.add(stock.remove(stock.size() - 1));
		}
	}

	@Override
	public Collection<Card> peek(int number) {
		if (number != 1) {
			throw new TakeMoreThanOneFromWasteException(number);
		}
		
		if (waste.isEmpty()) {
			throw new EmptyWasteException();
		}
		
		return List.of(waste.remove(waste.size() - 1));
	}

	class TakeMoreThanOneFromWasteException extends IllegalArgumentException {

		private static final long serialVersionUID = 3789760667895848906L;

		public TakeMoreThanOneFromWasteException(int number) {
			super(String.format(
					"Only one card at a time can be peek from Deck. Requested %d",
					number));
		}		
	}
	
	class EmptyWasteException extends IllegalStateException {

		private static final long serialVersionUID = 1713594105077197577L;

		public EmptyWasteException() {
			super("Cannot peek from empty waste");
		}		
	}
	
	@Override
	public void undoPeek(Collection<Card> cards) {
		waste.addAll(cards);
	}
	
	/**
	 * External view of Deck's status
	 */
	@Data
	public static class DeckStatus {
		private final int cardsOnWaste;
		private final int cardsOnStock;
		private final Optional<Card> topCardOnWaste;	
	};

	public DeckStatus getStatus() {
		final Optional<Card> top;
		
		if (waste.isEmpty()) {
			top = Optional.empty();
		} else {
			top = Optional.of(waste.get(waste.size() - 1));
		}
		
		return new DeckStatus(waste.size(), stock.size(), top);
	}

	public void undoTake() {
		if (!waste.isEmpty()) {
			stock.add(waste.remove(waste.size() - 1));
		}
		
		final boolean flipped = !takeCausedFlip.isEmpty() 
				&& takeCausedFlip.remove(takeCausedFlip.size() - 1);
		
		if (flipped && waste.isEmpty() && !stock.isEmpty()) {
			Collections.reverse(stock);
			waste.addAll(stock);
			stock.clear();
		}
	}
}
