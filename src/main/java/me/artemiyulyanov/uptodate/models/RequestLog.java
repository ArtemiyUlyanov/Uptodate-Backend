package me.artemiyulyanov.uptodate.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "requests_logs")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RequestLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String method;
    private String url;

    @Column(length = 5000)
    private String headers;

    @Column(length = 5000)
    private String requestBody;

    @Column(length = 5000)
    private String responseBody;

    private int statusCode;
    private long duration;
    private LocalDateTime timestamp = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}