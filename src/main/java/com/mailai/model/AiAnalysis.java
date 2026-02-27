package com.mailai.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiAnalysis {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originalContent;

    @Column(length = 2000)
    private String analysis;

    @Column(length = 2000)
    private String generatedReply;

    private String toneScore;

    private LocalDateTime createdAt;

    @ManyToOne
    @JsonIgnore
private User user;

}
