package speevy.cardGames;

import java.util.*;
import java.util.stream.Collectors;

public class AmericanCards implements Cards {

	@Override
	public Collection<Card> getAll() {
		return Arrays.stream(AmericanCardSuit.values())
				.flatMap(suit -> Arrays.stream(AmericanCardRank.values())
						.map(rank-> new Card(suit, rank)))
				.collect(Collectors.toList());
	}

}
