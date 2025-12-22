package ru.art.home.repository;

import ru.art.home.model.Post;

import java.util.List;
import java.util.Optional;

public interface PostRepository {

    List<Post> findAll();

    List<Post> findAllWithPaginationAndSearch(String search, int pageNumber, int pageSize);

    Optional<Post> findById(Long id);

    Post save(Post post);

    Post update(Post post);

    void deleteById(Long id);

    void incrementLikes(Long postId);

    void updateImage(Long postId, byte[] image);

    byte[] getImage(Long postId);

    int countPosts(String search);

    int getLastPageNumber(String search, int pageSize);
}
