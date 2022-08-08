package speevy.cardGames.klondike.ui.web;

import java.net.URISyntaxException;
import java.util.Optional;

import org.slf4j.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import lombok.*;
import speevy.cardGames.klondike.Deck.DeckStatus;
import speevy.cardGames.klondike.Klondike.*;
import speevy.cardGames.*;
import speevy.cardGames.klondike.KlondikeService;

@RestController
@CrossOrigin
@RequiredArgsConstructor
public class KlondikeController {

	
	private static final Logger log = LoggerFactory.getLogger(KlondikeController.class);

	private final KlondikeService service;
	
	@Getter
	@NoArgsConstructor
	public static class Action {
		public void setAction(String action) {
			this.action = action;
		}
		public void setFrom(String from) {
			this.from = from;
		}
		public void setTo(String to) {
			this.to = to;
		}
		public void setNumber(Integer number) {
			this.number = Optional.ofNullable(number);
		}
		private String action;
		private String from;
		private String to;
		private Optional<Integer> number;	
	}
	
	@GetMapping("/test")
	DeckStatus test () {
		return new DeckStatus (0, 1, 
				Optional.of(new Card(AmericanCardSuit.CLUBS, AmericanCardRank.ACE)));
	}

	@GetMapping("/test2")
	DeckStatus test2 () {
		return new DeckStatus (0, 1, Optional.empty());
	}

	@GetMapping("/test3")
	Optional<KlondikeStatus> test3 () {
		String id = service.createGame();
		return service.getStatus(id);
	}

	@PostMapping("/game")
	public ResponseEntity<Void> createGame() throws URISyntaxException {
		String id = service.createGame();
		
		return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest().path("/" + id).build().toUri())
				.header("Access-Control-Expose-Headers", "Location")
				.build();
	}
	
	@GetMapping("/game/{id}")
	public ResponseEntity<KlondikeStatus> status(@PathVariable String id) {
		return optionalToResponseEntity(service.getStatus(id));
	}
	
	@PutMapping(value="/game/{id}")
	public ResponseEntity<KlondikeStatus> action (@PathVariable String id, @RequestBody Action action) {
		final Optional<KlondikeStatus> status;
		switch(action.action()) {
		case "take": status = service.take(id); break;
		case "undo": status = service.undo(id); break;
		case "move": status = service.move(id, parseCardHolder(action.from()), parseCardHolder(action.to()), action.number().orElse(1)); break;
		default: throw new IllegalArgumentException("Unexpected value: " + action.action());
		};
		return optionalToResponseEntity(status);
	}
	
	@DeleteMapping(value="/game/{id}") 
	public ResponseEntity<Void> deleteGame(@PathVariable String id) {
		service.delete(id);
		return ResponseEntity.ok().build();
	}
	
	public CardHolder parseCardHolder(String str) {
		if ("d".equals(str)) {
			return new CardHolder(CardHolderType.DECK);
		}
		int index = Integer.parseInt(str.substring(1));
		
		CardHolderType type;
		switch(str.charAt(0)) {
		case 'p': type = CardHolderType.PILE; break;
		case 'f': type = CardHolderType.FOUNDATION; break;
		default: throw new IllegalArgumentException("Unexpected value: " + str.charAt(0));
		}
		
		return new CardHolder(type, index - 1);
	}
	
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<String> illegalArgument(IllegalArgumentException e) {
		return ResponseEntity.badRequest().body(e.getMessage());
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<String> illegalState(IllegalStateException e) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
	}

	<T> ResponseEntity<T> optionalToResponseEntity(Optional<T> optional) {
		return optional.map(o -> ResponseEntity.ok(o)).orElse(ResponseEntity.notFound().build());
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<String> illegalArgument(Exception e) {
		log.error("Unexpected internal error", e);
		return ResponseEntity.internalServerError().body("Unexpected internal error");
	}
	
}
