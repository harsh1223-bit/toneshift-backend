package com.mailai.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Email {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subject;

    @Column(length = 5000)
    private String content;

    private String summary;

    private String sentiment;

    private String priority;

    @ManyToOne
    private User user;
}
