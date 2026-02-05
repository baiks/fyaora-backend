package com.gler.assignment.services;

import org.springframework.http.ResponseEntity;

public interface TextReplaceService {
    ResponseEntity replace(String text);
}
