package speevy.cardGames;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonTypeInfo.*;

import lombok.*;

@Value
@AllArgsConstructor
public class Card  {
	@JsonTypeInfo(use=Id.CLASS, include=As.EXTERNAL_PROPERTY, property="suitClass")
	@JsonProperty("suit")
	private final CardSuit suit;

	@JsonTypeInfo(use=Id.CLASS, include=As.EXTERNAL_PROPERTY, property="rankClass")
	@JsonProperty("rank")
	private final CardRank rank;

	@SuppressWarnings("unused") // Used by JSON deserialization
	private Card() {
		suit = null;
		rank = null;
	}
}
