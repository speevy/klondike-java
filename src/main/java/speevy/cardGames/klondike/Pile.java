package speevy.cardGames.klondike;

import java.util.*;

import speevy.cardGames.*;
import speevy.cardGames.cardContainers.*;

/**
 * Each of the piles of the game. Acts as a Card Origin an Card Destination.
 * Only one card can be peek or poke at a time.
 * When poking a card, it has to be the same suit and the next rank of the 
 * card at top of the pile. If the pile is empty an ACE of any suit is allowed.
 */
public class Pile implements CardOrigin, CardDestination {
	
	private final ArrayList<Card> cards = new ArrayList<>();

	@Override
	public void poke(Collection<Card> cardsToPoke) throws IllegalStateException {
		if (cardsToPoke.size() != 1) {
			throw new PeekOrPokeMoreThanOnePileException(cardsToPoke.size());
		}
		final Card card = cardsToPoke.stream().findFirst().get();
		
		if (cards.isEmpty()) {
			if (card.rank().isFirst()) {
				cards.add(card);
			} else {
				throw new WrongCardPileException();
			}
		} else {
			final Card topCard = cards.get(cards.size() - 1);
			if (	card.suit().equals(topCard.suit())
					&& card.rank().isImmediateNextOf(topCard.rank())) {
				cards.add(card);
			} else {
				throw new WrongCardPileException();
			}
		}

	}

	@Override
	public Collection<Card> undoPoke(int number) {
		if (number != 1) {
			throw new PeekOrPokeMoreThanOnePileException(number);
		}
		
		return List.of(cards.remove(cards.size() -1));
	}

	@Override
	public Collection<Card> peek(int number) throws IllegalStateException, IllegalArgumentException {
		if (number != 1) {
			throw new PeekOrPokeMoreThanOnePileException(number);
		}
		
		if (cards.isEmpty()) {
			throw new EmptyPileException();
		}
		
		return List.of(cards.remove(cards.size() - 1));
	}

	@Override
	public void undoPeek(Collection<Card> cards) {
		this.cards.addAll(cards);
	}
	
	public PileStatus getStatus() {
		if (cards.isEmpty()) {
			return new PileStatus(0, Optional.empty());
		}
		return new PileStatus(cards.size(), Optional.of(cards.get(cards.size() - 1)));
	}

	/**
	 * External view of Pile status
	 */
	public record PileStatus (
		int numCards, Optional<Card> topCard	
	) {}

	/**
	 * Method used to prepare test cases, do not use for other purposes.
	 */
	@Deprecated
	void addCards(Collection<Card> toAdd) {
		cards.addAll(toAdd);
	}
	
	class EmptyPileException extends IllegalStateException {

		private static final long serialVersionUID = 1713594105077197577L;

		public EmptyPileException() {
			super("Cannot peek from empty pile");
		}		
	}
	
	class WrongCardPileException extends IllegalStateException {

		private static final long serialVersionUID = 1713594105077197577L;

		public WrongCardPileException() {
			super("Cannot poke the given card to this pile");
		}		
	}
	
	class PeekOrPokeMoreThanOnePileException extends IllegalArgumentException {

		private static final long serialVersionUID = 3789760667895848906L;

		public PeekOrPokeMoreThanOnePileException(int number) {
			super(String.format(
					"Only one card at a time can be peek or poke from or to the Pile. Requested %d",
					number));
		}		
	}
}
