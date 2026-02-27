package com.mailai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mailai.dto.AiRequest;
import com.mailai.dto.AiResponse;
import com.mailai.dto.AiResult;
import com.mailai.dto.AnalyzeResponse;
import com.mailai.dto.RewriteResponse;
import com.mailai.exception.AiServiceException;
import com.mailai.exception.UserNotFoundException;
import com.mailai.model.AiAnalysis;
import com.mailai.repository.AiAnalysisRepository;
import com.mailai.repository.UserRepository;
import com.mailai.util.PromptBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;


import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiService {

    private final AiAnalysisRepository aiAnalysisRepository;
    private final UserRepository userRepository;

    @Value("${ai.url}")
private String url;

@Value("${ai.model}")
private String model;


    @Value("${openrouter.api.key}")
    private String apiKey;


    private final RestTemplate restTemplate;

    public AnalyzeResponse analyzeText(AiRequest aiRequest) {

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(apiKey);
    headers.setContentType(MediaType.APPLICATION_JSON);

    Map<String, Object> requestBody = Map.of(
            "model", model,
            "messages", List.of(
                    Map.of(
                            "role", "system",
                            "content", """
You are an AI email assistant.

Analyze the tone of the email and respond STRICTLY in JSON format:

{
  "analysis": "short explanation of tone",
  "toneScore": "Professional | Neutral | Aggressive",
  "politeReply": "rewrite email in a polite and professional way"
}

Do not include any text outside JSON.
"""
                    ),
                    Map.of(
                            "role", "user",
                            "content", aiRequest.getContent()
                    )
            )
    );

    HttpEntity<Map<String, Object>> entity =
            new HttpEntity<>(requestBody, headers);

    ResponseEntity<String> response =
            restTemplate.postForEntity(url, entity, String.class);

    ObjectMapper mapper = new ObjectMapper();

    try {
        JsonNode root = mapper.readTree(response.getBody());

        String aiRawContent = root
                .path("choices")
                .get(0)
                .path("message")
                .path("content")
                .asText();

        // Clean possible markdown formatting
        aiRawContent = aiRawContent
                .replaceAll("```json", "")
                .replaceAll("```", "")
                .trim();

        JsonNode structured = mapper.readTree(aiRawContent);

        String analysisText = structured.get("analysis").asText();
        String toneScore = structured.get("toneScore").asText();
        String politeReply = structured.get("politeReply").asText();

        // Get logged-in user email
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        // Save to database
        AiAnalysis analysis = AiAnalysis.builder()
                .originalContent(aiRequest.getContent())
                .analysis(analysisText)
                .toneScore(toneScore)
                .generatedReply(politeReply)
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();

        aiAnalysisRepository.save(analysis);

        return new AnalyzeResponse(analysisText, toneScore, politeReply);

    } catch (Exception e) {
        throw new AiServiceException("Failed to process AI response");
    }
}
public RewriteResponse rewriteEmail(AiRequest aiRequest) {

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(apiKey);
    headers.setContentType(MediaType.APPLICATION_JSON);

    Map<String, Object> requestBody = Map.of(
            "model", model,
            "messages", List.of(
                    Map.of(
                            "role", "system",
                            "content", "Rewrite the following email in a polite and professional tone."
                    ),
                    Map.of(
                            "role", "user",
                            "content", aiRequest.getContent()
                    )
            )
    );

    HttpEntity<Map<String, Object>> entity =
            new HttpEntity<>(requestBody, headers);

    ResponseEntity<String> response =
            restTemplate.postForEntity(url, entity, String.class);

    try {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getBody());

        String rewritten = root
                .path("choices")
                .get(0)
                .path("message")
                .path("content")
                .asText();

        return new RewriteResponse(rewritten);



    } catch (Exception e) {
        throw new RuntimeException("Failed to parse rewrite response", e);
    }
}

public Page<AiAnalysis> getUserHistory(int page, int size) {

    String email = SecurityContextHolder.getContext()
            .getAuthentication()
            .getName();

    var user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

    Pageable pageable = PageRequest.of(
            page,
            size,
            Sort.by("createdAt").descending()
    );

    return aiAnalysisRepository.findByUserId(user.getId(), pageable);
}



    public AiResponse analyzeEmail(String content) {

        String prompt = PromptBuilder.buildEmailAnalysisPrompt(content);

        String url = "http://localhost:11434/api/generate";

        Map<String, Object> requestBody = Map.of(
                "model", "mistral",
                "prompt", prompt,
                "stream", false
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(url, entity, Map.class);

        String modelOutput = (String) response.getBody().get("response");

        return parseResponse(modelOutput);
    }

    private AiResponse parseResponse(String output) {

        AiResponse aiResponse = new AiResponse();

        // Very simple parsing (expects strict JSON from model)
        output = output.replaceAll("```json", "")
                       .replaceAll("```", "")
                       .trim();

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(output, AiResponse.class);
        } catch (Exception e) {
            aiResponse.setSummary("Parsing failed");
            aiResponse.setSmartReply(output);
            aiResponse.setSentiment("Unknown");
            aiResponse.setPriority("Unknown");
            return aiResponse;
        }
    }
}
