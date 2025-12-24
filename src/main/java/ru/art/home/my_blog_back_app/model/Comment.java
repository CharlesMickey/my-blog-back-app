package ru.art.home.my_blog_back_app.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    private Long id;
    @NotBlank(message = "Коммент не может быть пустым")
    private String text;
    @NotBlank(message = "ID поста не может быть пустым")
    private Long postId;
}
