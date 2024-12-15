package me.artemiyulyanov.uptodate.models.text;

import lombok.*;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArticleTextFragment {
    private String text;
    private ArticleTextFragmentType type;

    public enum ArticleTextFragmentType {
        DEFAULT, BOLD, CURSIVE, IMAGE;
    }
}