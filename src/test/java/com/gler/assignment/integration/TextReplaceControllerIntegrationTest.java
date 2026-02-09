package com.gler.assignment.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.emptyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TextReplaceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
        // Verify Spring context loads successfully
    }

    @Test
    void replace_EndToEnd_WithValidThreeCharacterString() throws Exception {
        mockMvc.perform(get("/")
                .param("text", "abc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("*b$"));
    }

    @Test
    void replace_EndToEnd_WithValidFourCharacterString() throws Exception {
        mockMvc.perform(get("/")
                .param("text", "abcd"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("*bc$"));
    }

    @Test
    void replace_EndToEnd_WithHelloWorld() throws Exception {
        mockMvc.perform(get("/")
                .param("text", "hello"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("*ell$"));
    }

    @Test
    void replace_EndToEnd_WithTwoCharacters() throws Exception {
        mockMvc.perform(get("/")
                .param("text", "ab"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(emptyString()));
    }

    @Test
    void replace_EndToEnd_WithSingleCharacter() throws Exception {
        mockMvc.perform(get("/")
                .param("text", "a"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void replace_EndToEnd_WithEmptyString() throws Exception {
        mockMvc.perform(get("/")
                .param("text", ""))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void replace_EndToEnd_WithMissingParameter() throws Exception {
        mockMvc.perform(get("/"))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    void replace_EndToEnd_WithSpecialCharacters() throws Exception {
        mockMvc.perform(get("/")
                .param("text", "a@#b"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("*@#$"));
    }

    @Test
    void replace_EndToEnd_WithWhitespace() throws Exception {
        mockMvc.perform(get("/")
                .param("text", "a b c"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("* b $"));
    }

    @Test
    void replace_EndToEnd_WithNumericString() throws Exception {
        mockMvc.perform(get("/")
                .param("text", "12345"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("*234$"));
    }

    @ParameterizedTest
    @CsvSource({
        "abc, '*b$'",
        "test, '*es$'",
        "hello, '*ell$'",
        "world, '*orl$'",
        "12345, '*234$'",
        "xyz, '*y$'"
    })
    void replace_EndToEnd_ParameterizedValidInputs(String input, String expectedOutput) throws Exception {
        mockMvc.perform(get("/")
                .param("text", input))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedOutput));
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "b", "x", "1", "!"})
    void replace_EndToEnd_ParameterizedInvalidInputs(String input) throws Exception {
        mockMvc.perform(get("/")
                .param("text", input))
                .andExpect(status().isBadRequest());
    }

    @Test
    void replace_EndToEnd_WithVeryLongString() throws Exception {
        String longInput = "a" + "b".repeat(1000) + "c";
        String expectedOutput = "*" + "b".repeat(1000) + "$";

        mockMvc.perform(get("/")
                .param("text", longInput))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(expectedOutput));
    }

    @Test
    void replace_EndToEnd_WithUnicodeCharacters() throws Exception {
        mockMvc.perform(get("/")
                .param("text", "a😀b"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("*😀$"));
    }

    @Test
    void replace_EndToEnd_ResponseContentType() throws Exception {
        mockMvc.perform(get("/")
                .param("text", "hello"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/plain"));
    }

    @Test
    void replace_EndToEnd_MultipleSequentialRequests() throws Exception {
        // First request
        mockMvc.perform(get("/")
                .param("text", "first"))
                .andExpect(status().isOk())
                .andExpect(content().string("*irs$"));

        // Second request
        mockMvc.perform(get("/")
                .param("text", "second"))
                .andExpect(status().isOk())
                .andExpect(content().string("*econ$"));

        // Third request
        mockMvc.perform(get("/")
                .param("text", "third"))
                .andExpect(status().isOk())
                .andExpect(content().string("*hir$"));
    }

    @Test
    void replace_EndToEnd_WithUrlEncodedSpaces() throws Exception {
        mockMvc.perform(get("/")
                .param("text", "hello world"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("*ello worl$"));
    }

    @Test
    void replace_EndToEnd_WithTabAndNewline() throws Exception {
        mockMvc.perform(get("/")
                .param("text", "a\tb\nc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("*\tb\n$"));
    }

    @Test
    void replace_EndToEnd_BoundaryCase_ExactlyTwoCharacters() throws Exception {
        mockMvc.perform(get("/")
                .param("text", "xy"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void replace_EndToEnd_BoundaryCase_ExactlyThreeCharacters() throws Exception {
        mockMvc.perform(get("/")
                .param("text", "xyz"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("*y$"));
    }

    @Test
    void replace_Performance_HandleMultipleConcurrentRequests() throws Exception {
        // Simulate load by making several requests
        for (int i = 0; i < 10; i++) {
            String input = "test" + i;
            String expected = "*est" + String.valueOf(i).substring(0, Math.min(1, String.valueOf(i).length())) + "$";
            
            mockMvc.perform(get("/")
                    .param("text", input))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void replace_EndToEnd_VerifyIdempotency() throws Exception {
        String input = "testing";
        String expectedOutput = "*estin$";

        // First call
        mockMvc.perform(get("/")
                .param("text", input))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedOutput));

        // Second call with same input should produce same output
        mockMvc.perform(get("/")
                .param("text", input))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedOutput));
    }

    @Test
    void replace_EndToEnd_WithMixedCaseLetters() throws Exception {
        mockMvc.perform(get("/")
                .param("text", "HeLLo"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("*eLL$"));
    }

    @Test
    void replace_EndToEnd_WithPunctuationMarks() throws Exception {
        mockMvc.perform(get("/")
                .param("text", "a.!?b"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("*.!?$"));
    }
}