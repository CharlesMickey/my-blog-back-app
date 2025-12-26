package ru.art.home.my_blog_back_app.repository;

import ru.art.home.my_blog_back_app.model.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {

    List<Comment> findByPostId(Long postId);

    Optional<Comment> findById(Long id);

    Comment save(Comment comment);

    Comment update(Comment comment);

    void deleteById(Long id);
}
