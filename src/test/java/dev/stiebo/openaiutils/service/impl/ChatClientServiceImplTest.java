package dev.stiebo.openaiutils.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.stiebo.openaiutils.dto.CVDataOutDto;
import dev.stiebo.openaiutils.dto.Flashcard;
import dev.stiebo.openaiutils.dto.Flashcards;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

//@Import({BeanOutputConverter.class, UserMessage.class, Prompt.class, ChatResponse.class})
class ChatClientServiceImplTest {

    @Mock
    ChatClient.Builder builder;


    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    ChatClient chatClient;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    ChatResponse mockChatResponse;

    @Mock
    ChatResponseMetadata chatResponseMetadata;

    ChatClientServiceImpl chatClientService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(builder.build()).thenReturn(chatClient);
        chatClientService = new ChatClientServiceImpl(builder);

        when(chatClient.prompt(any(Prompt.class)).call().chatResponse()).thenReturn(mockChatResponse);
        when(mockChatResponse.getMetadata().getUsage().getTotalTokens()).thenReturn(100L);
        when(mockChatResponse.getMetadata().getUsage().getPromptTokens()).thenReturn(50L);
        when(mockChatResponse.getMetadata().getUsage().getGenerationTokens()).thenReturn(50L);
    }

    @Test
    void testGetResponseClassFlashcardsWithImageReturnsFlashcards() throws IOException {
        Flashcards mockFlashcards = new Flashcards(
                List.of(
                        new Flashcard("Question1", "Answer1"),
                        new Flashcard("Question2", "Answer2")
                )
        );

        Resource mockUserPromptResource = new ByteArrayResource("userprompt".getBytes());
        Resource mockImageResource = new ByteArrayResource(new byte[]{20, 21, 22});

        ObjectMapper objectMapper = new ObjectMapper();
        when(mockChatResponse.getResult().getOutput().getContent())
                .thenReturn(objectMapper.writeValueAsString(mockFlashcards));

        Flashcards result = chatClientService.getResponse(Flashcards.class, mockUserPromptResource, mockImageResource);

        assertEquals(mockFlashcards, result);
        ArgumentCaptor<Prompt> promptArgumentCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatClient, atLeastOnce()).prompt(promptArgumentCaptor.capture());
        assertTrue(promptArgumentCaptor.getValue().getContents().contains("userprompt"));
    }


    @Test
    void testGetResponseClassCVWithDocumentReturnsCVDataOutDto() throws IOException {

        CVDataOutDto cvDataOutDto = new CVDataOutDto(
                "summary", "first", "last", "1.1.2020",
                "address", new String[] {"first","second"}, new String[] {"education"},
                new String[] {"course1"}, "TDD", new String[] {"english"},
                "programming", "something");
        ObjectMapper objectMapper = new ObjectMapper();
        Resource mockUserPromptResource = new ByteArrayResource("userprompt".getBytes());
        String mockDocument = "mockthisdocument";

        when(mockChatResponse.getResult().getOutput().getContent())
                .thenReturn(objectMapper.writeValueAsString(cvDataOutDto));

        CVDataOutDto result = chatClientService.getResponse(CVDataOutDto.class, mockUserPromptResource, mockDocument);

        assertEquals(cvDataOutDto.shortSummary(), result.shortSummary());
        assertEquals(cvDataOutDto.firstName(), result.firstName());
        assertEquals(cvDataOutDto.lastName(), result.lastName());
        assertEquals(cvDataOutDto.dateOfBirth(), result.dateOfBirth());
        assertEquals(cvDataOutDto.address(), result.address());
        assertArrayEquals(cvDataOutDto.employmentHistory(), result.employmentHistory());
        assertArrayEquals(cvDataOutDto.education(), result.education());
        assertArrayEquals(cvDataOutDto.courses(), result.courses());
        assertEquals(cvDataOutDto.competencies(), result.competencies());
        assertArrayEquals(cvDataOutDto.languages(), result.languages());
        assertEquals(cvDataOutDto.hobbies(), result.hobbies());
        ArgumentCaptor<Prompt> promptArgumentCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatClient, atLeastOnce()).prompt(promptArgumentCaptor.capture());
        assertTrue(promptArgumentCaptor.getValue().getContents().contains("userprompt"));
        assertTrue(promptArgumentCaptor.getValue().getContents().contains("mockthisdocument"));
    }

    @Test
    void testGetResponseWithEmptyUserPromptResourceThrowsException() throws IOException {
        String document = "document";
        Resource mockUserPromptResource = Mockito.mock(Resource.class);
        when(mockUserPromptResource.getContentAsString(any(Charset.class))).thenThrow(IOException.class);
        assertThrows(RuntimeException.class, ()
            -> chatClientService.getResponse(CVDataOutDto.class, mockUserPromptResource, document));
    }
}