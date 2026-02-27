package com.mailai.controller;

import com.mailai.dto.AiRequest;
import com.mailai.dto.AiResult;
import com.mailai.dto.AnalyzeResponse;
import com.mailai.dto.RewriteResponse;
import com.mailai.model.AiAnalysis;
import com.mailai.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    
   @GetMapping("/history")
public ResponseEntity<Page<AiAnalysis>> getHistory(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "5") int size) {

    return ResponseEntity.ok(aiService.getUserHistory(page, size));
}


@PostMapping("/analyze")
public ResponseEntity<AnalyzeResponse> analyze(@RequestBody AiRequest request) {
    return ResponseEntity.ok(aiService.analyzeText(request));
}

@PostMapping("/rewrite")
public ResponseEntity<RewriteResponse> rewrite(@RequestBody AiRequest request) {
    return ResponseEntity.ok(aiService.rewriteEmail(request));
}


}

