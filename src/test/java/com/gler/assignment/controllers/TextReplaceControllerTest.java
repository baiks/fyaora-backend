package com.gler.assignment.controllers;

import com.gler.assignment.services.TextReplaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TextReplaceControllerTest {

    @Mock
    private TextReplaceService textReplaceService;

    @InjectMocks
    private TextReplaceController textReplaceController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(textReplaceController).build();
    }

    @Test
    void replace_WithValidText_ShouldReturnOkResponse() throws Exception {
        // Arrange
        String inputText = "hello";
        String expectedOutput = "*ell$";
        when(textReplaceService.replace(inputText))
            .thenReturn(ResponseEntity.ok(expectedOutput));

        // Act & Assert
        mockMvc.perform(get("/")
                .param("text", inputText))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedOutput));

        verify(textReplaceService, times(1)).replace(inputText);
    }

    @Test
    void replace_WithThreeCharacters_ShouldReturnCorrectFormat() throws Exception {
        // Arrange
        String inputText = "abc";
        String expectedOutput = "*b$";
        when(textReplaceService.replace(inputText))
            .thenReturn(ResponseEntity.ok(expectedOutput));

        // Act & Assert
        mockMvc.perform(get("/")
                .param("text", inputText))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedOutput));

        verify(textReplaceService, times(1)).replace(inputText);
    }

    @Test
    void replace_WithTwoCharacters_ShouldReturnEmptyString() throws Exception {
        // Arrange
        String inputText = "ab";
        when(textReplaceService.replace(inputText))
            .thenReturn(ResponseEntity.ok(""));

        // Act & Assert
        mockMvc.perform(get("/")
                .param("text", inputText))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(textReplaceService, times(1)).replace(inputText);
    }

    @Test
    void replace_WithSingleCharacter_ShouldReturnBadRequest() throws Exception {
        // Arrange
        String inputText = "a";
        when(textReplaceService.replace(inputText))
            .thenReturn(ResponseEntity.badRequest().build());

        // Act & Assert
        mockMvc.perform(get("/")
                .param("text", inputText))
                .andExpect(status().isBadRequest());

        verify(textReplaceService, times(1)).replace(inputText);
    }

    @Test
    void replace_WithEmptyString_ShouldReturnBadRequest() throws Exception {
        // Arrange
        String inputText = "";
        when(textReplaceService.replace(inputText))
            .thenReturn(ResponseEntity.badRequest().build());

        // Act & Assert
        mockMvc.perform(get("/")
                .param("text", inputText))
                .andExpect(status().isBadRequest());

        verify(textReplaceService, times(1)).replace(inputText);
    }

    @Test
    void replace_WithMissingTextParameter_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/"))
                .andExpect(status().isBadRequest());

        verify(textReplaceService, never()).replace(anyString());
    }

    @Test
    void replace_WithSpecialCharacters_ShouldReturnOkResponse() throws Exception {
        // Arrange
        String inputText = "a@#b";
        String expectedOutput = "*@#$";
        when(textReplaceService.replace(inputText))
            .thenReturn(ResponseEntity.ok(expectedOutput));

        // Act & Assert
        mockMvc.perform(get("/")
                .param("text", inputText))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedOutput));

        verify(textReplaceService, times(1)).replace(inputText);
    }

    @Test
    void replace_WithWhitespace_ShouldReturnOkResponse() throws Exception {
        // Arrange
        String inputText = "a b c";
        String expectedOutput = "* b $";
        when(textReplaceService.replace(inputText))
            .thenReturn(ResponseEntity.ok(expectedOutput));

        // Act & Assert
        mockMvc.perform(get("/")
                .param("text", inputText))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedOutput));

        verify(textReplaceService, times(1)).replace(inputText);
    }

    @Test
    void replace_WithNumericString_ShouldReturnOkResponse() throws Exception {
        // Arrange
        String inputText = "12345";
        String expectedOutput = "*234$";
        when(textReplaceService.replace(inputText))
            .thenReturn(ResponseEntity.ok(expectedOutput));

        // Act & Assert
        mockMvc.perform(get("/")
                .param("text", inputText))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedOutput));

        verify(textReplaceService, times(1)).replace(inputText);
    }

    @Test
    void replace_ServiceThrowsException_ShouldPropagateException() throws Exception {
        // Arrange
        String inputText = "test";
        when(textReplaceService.replace(inputText))
            .thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(get("/")
                .param("text", inputText))
                .andExpect(status().is5xxServerError());

        verify(textReplaceService, times(1)).replace(inputText);
    }

    @Test
    void replace_WithLongString_ShouldReturnOkResponse() throws Exception {
        // Arrange
        String inputText = "thisisaverylongstringfortesting";
        String expectedOutput = "*hisisaverylongstringfortestin$";
        when(textReplaceService.replace(inputText))
            .thenReturn(ResponseEntity.ok(expectedOutput));

        // Act & Assert
        mockMvc.perform(get("/")
                .param("text", inputText))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedOutput));

        verify(textReplaceService, times(1)).replace(inputText);
    }

    @Test
    void replace_WithUrlEncodedCharacters_ShouldDecodeAndProcess() throws Exception {
        // Arrange
        String inputText = "hello world";
        String expectedOutput = "*ello worl$";
        when(textReplaceService.replace(inputText))
            .thenReturn(ResponseEntity.ok(expectedOutput));

        // Act & Assert - URL encoded space is %20
        mockMvc.perform(get("/")
                .param("text", "hello world"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedOutput));

        verify(textReplaceService, times(1)).replace(inputText);
    }

    @Test
    void replace_VerifyServiceInteraction_CalledExactlyOnce() throws Exception {
        // Arrange
        String inputText = "test";
        when(textReplaceService.replace(inputText))
            .thenReturn(ResponseEntity.ok("*es$"));

        // Act
        mockMvc.perform(get("/")
                .param("text", inputText))
                .andExpect(status().isOk());

        // Assert
        verify(textReplaceService, times(1)).replace(inputText);
        verify(textReplaceService, times(1)).replace(anyString());
        verifyNoMoreInteractions(textReplaceService);
    }

    @Test
    void replace_WithMultipleRequests_ShouldHandleEachIndependently() throws Exception {
        // Arrange
        when(textReplaceService.replace("abc"))
            .thenReturn(ResponseEntity.ok("*b$"));
        when(textReplaceService.replace("xyz"))
            .thenReturn(ResponseEntity.ok("*y$"));

        // Act & Assert - First request
        mockMvc.perform(get("/")
                .param("text", "abc"))
                .andExpect(status().isOk())
                .andExpect(content().string("*b$"));

        // Act & Assert - Second request
        mockMvc.perform(get("/")
                .param("text", "xyz"))
                .andExpect(status().isOk())
                .andExpect(content().string("*y$"));

        verify(textReplaceService, times(1)).replace("abc");
        verify(textReplaceService, times(1)).replace("xyz");
    }
}