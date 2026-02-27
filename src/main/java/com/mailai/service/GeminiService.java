package com.mailai.service;

import org.springframework.stereotype.Service;

@Service
public class GeminiService {

    public String summarizeEmail(String content) {
        return "AI Summary for: " + content;
    }
}
