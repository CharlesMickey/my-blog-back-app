package ru.art.home.my_blog_back_app.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    private Long id;
    @NotBlank(message = "Заголовок не может быть пустым")
    private String title;
    @NotBlank(message = "Пост не может быть пустым")
    private String text;
    private List<String> tags;
    @PositiveOrZero(message = "Количество лайков от 0 и более")
    private int likesCount;
    @PositiveOrZero(message = "Количество комментов от 0 и более")
    private int commentsCount;
}
