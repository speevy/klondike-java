package speevy.cardGames.klondike.ui.web;

import static org.mockito.AdditionalMatchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static speevy.cardGames.klondike.KlondikeTest.createTestKlondike;

import java.util.Optional;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.*;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import speevy.cardGames.Cards;
import speevy.cardGames.klondike.*;
import speevy.cardGames.klondike.Klondike.*;
import speevy.cardGames.klondike.storage.KlondikeRepository;

@WebMvcTest(includeFilters = {@Filter(type = FilterType.ASSIGNABLE_TYPE, classes = 
	{KlondikeController.class, Cards.class, KlondikeService.class})})
@Import(JacksonCustomSerializers.class)
public class WebTest {
	@Autowired 
	private MockMvc mockMvc;

	@MockBean 
	private KlondikeRepository mockRepo;
	
	private Klondike spy;
	
	@BeforeEach
	private void prepareMocks() {
		spy = Mockito.spy(createTestKlondike());
		
		when(mockRepo.get("invalid")).thenReturn(Optional.empty());
		when(mockRepo.get(eq("exception"))).thenThrow(RuntimeException.class);

		when(mockRepo.get(not(or(eq("invalid"), eq("exception")))))
			.thenReturn(Optional.of(spy));
	}

	public static final MediaType APPLICATION_JSON = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype());

	@Test
	void create() throws Exception {
		mockMvc.perform(post("/game"))
			.andExpect(status().isCreated())
			.andExpect(header().exists("location"));
		verify(mockRepo).save(any());
	}
	
	@Test
	void take() throws Exception {
		mockMvc.perform(put("/game/valid")
				.contentType(APPLICATION_JSON)
				.content("{\"action\":\"take\"}"))
		.andExpect(status().isOk());
		verify(spy).take();
		verify(mockRepo).update("valid", spy);
	}

	@Test
	void undo() throws Exception {
		mockMvc.perform(put("/game/valid")
				.contentType(APPLICATION_JSON)
				.content("{\"action\":\"undo\"}"))
		.andExpect(status().isOk());
		verify(spy).undo();
		verify(mockRepo).update("valid", spy);
	}

	@Test
	void statusTest() throws Exception {
		mockMvc.perform(get("/game/valid"))
		.andExpect(status().isOk());
	}
	
	@Test
	void move() throws Exception {
		moveCase("d", "p1", new CardHolder(CardHolderType.DECK), 
				new CardHolder(CardHolderType.PILE, 0), 1);
		
		moveCase("f4", "p4", new CardHolder(CardHolderType.FOUNDATION, 3), 
				new CardHolder(CardHolderType.PILE, 3), 1);
		
		moveCase("f1", "f2", new CardHolder(CardHolderType.FOUNDATION, 0), 
				new CardHolder(CardHolderType.FOUNDATION, 1), 2);
		
		moveCase("f3", "f5", new CardHolder(CardHolderType.FOUNDATION, 2), 
				new CardHolder(CardHolderType.FOUNDATION, 4), 1);
		
	}
	
	void moveCase(String fromStr, String toStr, CardHolder from, CardHolder to, int number) 
			throws Exception {
		mockMvc.perform(put("/game/valid")
				.contentType(APPLICATION_JSON)
				.content("{\"action\":\"move\",\"from\":\"" + fromStr +
						"\",\"to\":\"" + toStr + "\",\"number\":" + number + "}"))
		.andExpect(status().isOk());
		verify(spy).moveCards(from, to, number);
		verify(mockRepo, atLeast(1)).update("valid", spy);
	}
	
	@Test
	void deleteTest() throws Exception {
		mockMvc.perform(delete("/game/valid")
				.contentType(APPLICATION_JSON)
				.content("{\"action\":\"undo\"}"))
		.andExpect(status().isOk());
		verify(mockRepo).delete("valid");
	}

	@Test
	void illegalState() throws Exception {
		mockMvc.perform(put("/game/valid")
				.contentType(APPLICATION_JSON)
				.content("{\"action\":\"move\",\"from\":\"f2\",\"to\":\"p1\",\"number\":1}"))
		.andExpect(status().isConflict());
		verify(mockRepo, times(0)).update("valid", spy);
	}
	
	@Test
	void illegalArgument() throws Exception {
		mockMvc.perform(put("/game/valid")
				.contentType(APPLICATION_JSON)
				.content("{\"action\":\"move\",\"from\":\"f4\",\"to\":\"z5\",\"number\":1}"))
		.andExpect(status().isBadRequest());
		verify(mockRepo, times(0)).update("valid", spy);
	}

	@Test
	void notFound() throws Exception {
		mockMvc.perform(get("/game/invalid"))
		.andExpect(status().isNotFound());
	}

	@Test
	void unknownException() throws Exception {
		mockMvc.perform(get("/game/exception"))
			.andExpect(status().isInternalServerError());
	}

}
