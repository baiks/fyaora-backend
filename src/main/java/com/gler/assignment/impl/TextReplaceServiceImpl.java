package com.gler.assignment.impl;

import com.gler.assignment.services.TextReplaceService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class TextReplaceServiceImpl implements TextReplaceService {

    @Override
    public ResponseEntity<String> replace(String text) {
        if (text == null) {
            return ResponseEntity.badRequest().build();
        }
        if (text.length() < 2) {
            return ResponseEntity.badRequest().build();
        }
        if (text.length() == 2) {
            return ResponseEntity.ok("");
        }
        String result = "*" + text.substring(1, text.length() - 1) + "$";
        return ResponseEntity.ok(result);
    }
}
