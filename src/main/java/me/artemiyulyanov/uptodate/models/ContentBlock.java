package me.artemiyulyanov.uptodate.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "articles_content_blocks")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContentBlock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Article article;

    private String type, text;
}
