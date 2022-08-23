package speevy.cardGames.klondike;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.*;

import com.fasterxml.jackson.annotation.*;

import lombok.*;
import speevy.cardGames.*;
import speevy.cardGames.cardContainers.*;
import speevy.cardGames.klondike.Deck.DeckStatus;
import speevy.cardGames.klondike.Foundation.FoundationStatus;
import speevy.cardGames.klondike.Pile.PileStatus;

@EqualsAndHashCode
public class Klondike {

	@JsonProperty("deck")
	private final Deck deck;
	@JsonProperty("piles")
	private final List<Pile> piles;
	@JsonProperty("foundations")
	private final List<Foundation> foundations;
	@JsonProperty("actionLog")
	private final List<Action> actionLog;
	
	private static final Logger log = LoggerFactory.getLogger(Klondike.class);

	private void debug(Supplier<String> msg) {
		if (log.isErrorEnabled()) {
			log.debug(msg.get());
		}
	}
	
	protected enum ActionType {
		MOVE_CARDS,
		TAKE
	}
	
	@Value
	@AllArgsConstructor
	protected static class Action{
		
		@JsonProperty("type")
		private final ActionType type;
		@JsonProperty("from")
		private final CardHolder from;
		@JsonProperty("to")
		private final CardHolder to;
		@JsonProperty("cards")
		private final int cards;
		
		static Action take() { 
			return new Action(ActionType.TAKE, null, null, 0); 
		}
		
		@SuppressWarnings("unused") // Used by JSON deserialization
		private Action() {
			type = null;
			from = null;
			to = null;
			cards = 0;
		}
	}
	
	public Klondike(final Cards cardDeck) {
		final List<Card> cards = new ArrayList<>(cardDeck.getAll());
		Collections.shuffle(cards);
		
		piles = new ArrayList<>();
		for (int i = 0; i < 4; i++) {
			piles.add(new Pile());
		}
		
		foundations = new ArrayList<>();
		int j = 0;
		for (int i = 0; i < 7; i++, j += i) {
			foundations.add(new Foundation(cards.subList(j, j + i + 1)));
		}
		
		deck = new Deck(cards.subList(j, cards.size()));
		
		actionLog = new ArrayList<>();
	}
	
	@SuppressWarnings("unused") // Used by JSON deserialization
	private Klondike() {
		piles = null;
		foundations = null;
		deck = null;
		actionLog = null;
	}
	
	@JsonIgnore
	public KlondikeStatus getStatus() {
		return new KlondikeStatus(deck.getStatus(), 
				piles.stream().map(Pile::getStatus).collect(Collectors.toList()),
				foundations.stream().map(Foundation::getStatus).collect(Collectors.toList()));
	}
	
	@Data
	public static class KlondikeStatus {
		private final DeckStatus deck;
		private final List<PileStatus> piles;
		private final List<FoundationStatus> foundations;
	}
	
	public enum CardHolderType {
		DECK,
		PILE,
		FOUNDATION
	}
	
	@Value
	public static class CardHolder {
		@JsonProperty("type")
		private final CardHolderType type;
		@JsonProperty("index")
		private final int index;
	
		public CardHolder(CardHolderType type, int index) {
			final int maxIndex;
			switch(type) {
				case DECK: maxIndex = 1; break;
				case FOUNDATION: maxIndex = 7; break;
				case PILE: maxIndex = 4; break;
				default: maxIndex = -1;
			}
			if (type == null || index < 0 || index >= maxIndex) {
				throw new IllegalArgumentException();
			}
			this.type = type;
			this.index = index;
		}
		
		public CardHolder(CardHolderType type) { this(type, 0); }
		
		@SuppressWarnings("unused") // Used by JSON deserialization
		private CardHolder() {
			type = null;
			index = 0;
		}
	}
	
	public void moveCards(final CardHolder from, final CardHolder to, final int number) {
		if (from.equals(to)) {
			throw new IllegalArgumentException("Can't move cards from and to the same cardHolder");
		}
		
		CardOrigin origin = getOrigin(from);
		
		CardDestination destination = getDestination(to); 
		
		Collection<Card> cards = origin.peek(number);
		try {
			destination.poke(cards);
		} catch(RuntimeException e) {
			origin.undoPeek(cards);
			throw e;
		}
		actionLog.add(new Action(ActionType.MOVE_CARDS, from, to, number));
		debug(() -> "Action: move from " + from + " to " + to + " cards " + cards);
	}

	public boolean canMoveCards(final CardHolder from, final CardHolder to, final int number) {
		if (from.equals(to)) {
			return false;
		}
		
		CardOrigin origin = getOrigin(from);
		
		CardDestination destination = getDestination(to); 
		
		Collection<Card> cards = origin.dryPeek(number);
		
		return !cards.isEmpty() && destination.dryPoke(cards);
	}
	
	private CardDestination getDestination(final CardHolder to) {
		switch(to.type()) {
		case FOUNDATION: return foundations.get(to.index());
		case PILE: return piles.get(to.index());
		default: throw new IllegalArgumentException("Can't move cards to deck");
		}
	}

	private CardOrigin getOrigin(final CardHolder from) {
		switch(from.type()) {
		case DECK: return deck;
		case FOUNDATION: return foundations.get(from.index());
		case PILE: return piles.get(from.index());
		default: throw new IllegalArgumentException("Unkown origin " + from);
		}
	}

	/**
	 * Constructor used to prepare test cases, do not use for other purposes.
	 */
	@Deprecated
	Klondike(final Deck deck, final List<Pile> piles, final List<Foundation> foundations) {
		super();
		this.deck = deck;
		this.piles = piles;
		this.foundations = foundations;
		actionLog = new ArrayList<>();
	}

	public void toPile(final CardHolder from) {

		final CardOrigin origin = getOrigin(from);		
		final Collection<Card> cards = origin.peek(1);
		
		for (int i = 0; i < piles.size(); i++) {
			try {
				final Pile destination = piles.get(i);
				destination.poke(cards);
				actionLog.add(new Action(ActionType.MOVE_CARDS, from, new CardHolder(CardHolderType.PILE, i), 1));
				return;
			} catch (IllegalStateException e) {
				// Do nothing
			}
		}
		
		origin.undoPeek(cards);
		throw new IllegalStateException ("No pile accepted this card");
	}

	public void take() {
		deck.take();
		actionLog.add(Action.take());
		debug(() -> "Action: Take");
	}

	public void undo() {
		if (actionLog.isEmpty()) {
			log.warn("Undo called, but no logged actions found");
			return;
		}
		
		Action action  = actionLog.remove(actionLog.size() - 1);
		
		if (action.type().equals(ActionType.TAKE)) {
			deck.undoTake();
			log.debug("Undo take");
		} else {
			final CardOrigin origin = getOrigin(action.from());
			final CardDestination destination = getDestination(action.to());
			
			final Collection<Card> cards = destination.undoPoke(action.cards());
			
			origin.undoPeek(cards);
			debug(() -> "Undo move from " + action.from() + " to " + action.to() + " cards " + cards);
		}
	}

}
