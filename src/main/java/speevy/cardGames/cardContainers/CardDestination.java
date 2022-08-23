package speevy.cardGames.cardContainers;

import java.util.Collection;

import speevy.cardGames.Card;

/**
 * Anything where cards can be moved to
 */
public interface CardDestination {

	/**
	 *  Poke an arbitrary number of cards. It should check the
     *  business logic for allowing this poke of cards. If everything
     *  is OK a the cards should be added to the Card Destination.
     *  @throws IllegalStateException if the current state of the game
     *  does not allow the given cards to be poked.
     */
	void poke(Collection<Card> cards) throws IllegalStateException;

	/**
	 *  Check the business logic for allowing this poke of cards. 
     *  
     *  The state of the CardDestination should not be altered.
     *  
     *  No exceptions should be thrown.
     *  
     *  @Returns true if the poke is valid, false if not.
     */
	boolean dryPoke(Collection<Card> cards);

	Collection<Card> undoPoke(int number);

}
