package ru.yandex.practicum.repository;

import ru.yandex.practicum.model.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {

    List<Comment> findByPostId(Long postId);

    Optional<Comment> findById(Long id);

    Comment save(Comment comment);

    Comment update(Comment comment);

    void deleteById(Long id);
}
