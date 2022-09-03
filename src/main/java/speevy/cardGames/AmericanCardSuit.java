package speevy.cardGames;

import lombok.*;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Getter
@Accessors(fluent = false)
public enum AmericanCardSuit implements CardSuit {
    CLUBS ("CLUBS", "♣", "BLACK"),
    DIAMONDS ("DIAMONDS", "♦", "RED"),
    HEARTS ("HEARTS", "♥", "RED"),
    SPADES ("SPADES", "♠", "BLACK");
	
	private String name;
	private String symbol;
	private String groupName;
	
}
