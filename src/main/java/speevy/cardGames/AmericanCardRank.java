package speevy.cardGames;

import lombok.*;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Getter
@Accessors(fluent = false)
public enum AmericanCardRank implements CardRank {
    ACE ("A", 1),
    TWO ("2", 2),
    THREE ("3", 3),
    FOUR ("4", 4),
    FIVE ("5", 5),
    SIX ("6", 6),
    SEVEN ("7", 7),
    EIGHT ("8", 8),
    NINE ("9", 9),
    TEN ("10", 10),
    JACK ("J", 11),
    QUEEN ("Q", 12),
    KING ("K", 13);
    
    private String name;
    private int index;

    @Override
	public boolean isImmediateNextOf(CardRank other) {

		return (other instanceof AmericanCardRank) 
				&& (((AmericanCardRank)other).getIndex() + 1 == index);
	}

    @Override
	public boolean isFirst() {
		return equals(ACE);
	}

    @Override
	public boolean isLast() {
		return equals(KING);
	}
}
