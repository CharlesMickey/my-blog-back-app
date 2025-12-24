package ru.art.home.my_blog_back_app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    private Long id;
    private String title;
    private String text;
    private List<String> tags;
    private int likesCount;
    private int commentsCount;
}
