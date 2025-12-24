package ru.art.home.my_blog_back_app.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.art.home.my_blog_back_app.model.Comment;
import ru.art.home.my_blog_back_app.service.CommentService;

import java.util.List;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost")
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<List<Comment>> getComments(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getCommentsByPostId(postId));
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<Comment> getComment(
            @PathVariable Long postId,
            @PathVariable Long commentId) {
        return ResponseEntity.ok(commentService.getComment(postId, commentId));
    }

    @PostMapping
    public ResponseEntity<Comment> createComment(
            @PathVariable Long postId,
            @RequestBody Comment comment) {
        comment.setPostId(postId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.createComment(comment));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<Comment> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody Comment comment) {
        comment.setId(commentId);
        comment.setPostId(postId);
        return ResponseEntity.ok(commentService.updateComment(comment));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok().build();
    }
}
