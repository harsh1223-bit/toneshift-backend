package com.mailai.util;

public class PromptBuilder {

    public static String buildEmailAnalysisPrompt(String content) {

        return """
        You are an AI Email Intelligence Engine.

        Analyze the following email and return STRICT JSON with this structure:

        {
          "summary": "short summary",
          "smartReply": "professional reply",
          "sentiment": "Positive | Negative | Neutral",
          "priority": "High | Medium | Low"
        }

        Email:
        """ + content;
    }
}
