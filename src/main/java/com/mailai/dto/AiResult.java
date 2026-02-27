package com.mailai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AiResult {
    private String analysis;
    private String toneScore;
    private String politeReply;
}
