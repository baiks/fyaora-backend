package com.gler.assignment.controllers;


import com.gler.assignment.services.TextReplaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TextReplaceController {
    private final TextReplaceService textReplaceService;

    @GetMapping
    ResponseEntity replace(@RequestParam String text) {
        return textReplaceService.replace(text);
    }
}
