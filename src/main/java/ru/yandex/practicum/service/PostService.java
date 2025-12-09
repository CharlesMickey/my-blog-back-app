package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.model.Post;
import ru.yandex.practicum.repository.CommentRepository;
import ru.yandex.practicum.repository.PostRepository;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public Map<String, Object> getPosts(String search, int pageNumber, int pageSize) {
        List<Post> posts = postRepository.findAllWithPaginationAndSearch(search, pageNumber, pageSize);

        posts.forEach(post -> {
            if (post.getText().length() > 128) {
                post.setText(post.getText().substring(0, 128) + "…");
            }
        });

        int lastPage = postRepository.getLastPageNumber(search, pageSize);
        boolean hasPrev = pageNumber > 1;
        boolean hasNext = pageNumber < lastPage;

        return Map.of(
                "posts", posts,
                "hasPrev", hasPrev,
                "hasNext", hasNext,
                "lastPage", lastPage
        );
    }

    public Post getPost(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
    }

    @Transactional
    public Post createPost(Post post) {
        post.setLikesCount(0);
        post.setCommentsCount(0);
        return postRepository.save(post);
    }

    @Transactional
    public Post updatePost(Post post) {
        Post existing = postRepository.findById(post.getId())
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + post.getId()));

        post.setLikesCount(existing.getLikesCount());
        post.setCommentsCount(existing.getCommentsCount());

        return postRepository.update(post);
    }

    @Transactional
    public void deletePost(Long id) {
        commentRepository.findByPostId(id).forEach(comment
                -> commentRepository.deleteById(comment.getId())
        );

        postRepository.deleteById(id);
    }

    @Transactional
    public int incrementLikes(Long postId) {
        postRepository.incrementLikes(postId);
        return postRepository.findById(postId)
                .map(Post::getLikesCount)
                .orElse(0);
    }

    @Transactional
    public void updateImage(Long postId, MultipartFile image) throws IOException {
        postRepository.updateImage(postId, image.getBytes());
    }

    public byte[] getImage(Long postId) {
        return postRepository.getImage(postId);
    }
}
