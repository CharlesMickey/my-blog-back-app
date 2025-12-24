package ru.art.home.my_blog_back_app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.art.home.my_blog_back_app.exception.NotFoundException;
import ru.art.home.my_blog_back_app.model.Comment;
import ru.art.home.my_blog_back_app.model.Post;
import ru.art.home.my_blog_back_app.repository.CommentRepository;
import ru.art.home.my_blog_back_app.repository.PostRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public List<Comment> getCommentsByPostId(Long postId) {
        return commentRepository.findByPostId(postId);
    }

    public Comment getComment(Long postId, Long commentId) {
        return commentRepository.findById(commentId)
                .filter(comment -> comment.getPostId().equals(postId))
                .orElseThrow(() -> new NotFoundException("Comment not found"));
    }

    @Transactional
    public Comment createComment(Comment comment) {
        Post post = postRepository.findById(comment.getPostId())
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + comment.getPostId()));

        Comment saved = commentRepository.save(comment);
        post.setCommentsCount(post.getCommentsCount() + 1);
        postRepository.update(post);

        return saved;
    }

    @Transactional
    public Comment updateComment(Comment comment) {
        return commentRepository.update(comment);
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        commentRepository.deleteById(commentId);

        Post post = postRepository.findById(comment.getPostId())
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + comment.getPostId()));

        post.setCommentsCount(Math.max(0, post.getCommentsCount() - 1));
        postRepository.update(post);
    }
}
