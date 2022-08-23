package speevy.cardGames.cardContainers;

import java.util.*;

import speevy.cardGames.Card;

/** 
 * Anything where cards can be taken of
 */
public interface CardOrigin {

    /** Peek an arbitrary number of cards. It should check the
     *  business logic for allowing this peek of cards. If everything
     *  is OK a vector containing the requested cards is returned. 
     *  The returned cards should be removed from the Card Origin.
     *  
     *  @throws IllegalStateException if the current game state does not 
     *  allow the requested number of cards to be peek.
     *  @throws IllegalArgumentException if the number of requested cards
     *  is not valid
     */
    Collection<Card> peek(int number) throws IllegalStateException, IllegalArgumentException;

    /** Peek an arbitrary number of cards. It should check the
     *  business logic for allowing this peek of cards. If everything
     *  is OK a vector containing the requested cards is returned, otherwise, an empty collection. 
     *  The returned cards should NOT be removed from the Card Origin.
     */
    Collection<Card> dryPeek(int number);

    void undoPeek(Collection<Card> cards);
}
