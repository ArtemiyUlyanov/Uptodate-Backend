package me.artemiyulyanov.uptodate.models.text;

import lombok.*;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Component
public class ArticleTextFragment {
    private String text, type;
}