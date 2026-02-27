package com.mailai.dto;

import lombok.Data;

@Data
public class AiResponse {

    private String summary;
    private String smartReply;
    private String sentiment;
    private String priority;
}
