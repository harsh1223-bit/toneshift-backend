package com.mailai.controller;

import com.mailai.model.Email;
import com.mailai.repository.EmailRepository;
import com.mailai.service.GeminiService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/emails")
public class EmailController {

    private final GeminiService geminiService;
    private final EmailRepository emailRepository;

    public EmailController(GeminiService geminiService, EmailRepository emailRepository) {
        this.geminiService = geminiService;
        this.emailRepository = emailRepository;
    }

    @PostMapping("/analyze")
    public Email analyzeEmail(@RequestBody Email email) {

        String response = geminiService.summarizeEmail(email.getContent());

        email.setSummary(response);

        return emailRepository.save(email);
    }
}
