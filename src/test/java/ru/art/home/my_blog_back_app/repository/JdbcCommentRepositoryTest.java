package ru.art.home.my_blog_back_app.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.art.home.my_blog_back_app.model.Comment;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJdbcTest
@Import(JdbcCommentRepository.class)
class JdbcCommentRepositoryTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    CommentRepository repository;

    @Test
    void findByPostId_returnsCommentsForPost() {
        List<Comment> comments = repository.findByPostId(1L);

        assertEquals(2, comments.size());
        assertEquals(1L, comments.getFirst().getPostId());
        assertNotNull(comments.getFirst().getText());
    }

    @Test
    void findById_returnsComment() {
        Optional<Comment> comment = repository.findById(1L);

        assertTrue(comment.isPresent());
        assertEquals(1L, comment.get().getId());
        assertEquals(1L, comment.get().getPostId());
    }

    @Test
    void findById_returnsEmpty_whenNotFound() {
        assertTrue(repository.findById(999L).isEmpty());
    }

    @Test
    void save_insertsCommentAndGeneratesId() {
        Comment comment = new Comment(null, "Новый комментарий", 2L);

        Comment saved = repository.save(comment);

        assertNotNull(saved.getId());
        assertEquals("Новый комментарий", saved.getText());
        assertEquals(2L, saved.getPostId());

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM comments WHERE id = ?",
                Integer.class,
                saved.getId()
        );

        assertEquals(1, count);
    }

    @Test
    void update_updatesTextOnly() {
        Optional<Comment> before = repository.findById(1L);
        assertTrue(before.isPresent());

        Comment updated = new Comment(before.get().getId(), "Обновленный текст", before.get().getPostId());
        repository.update(updated);

        Optional<Comment> after = repository.findById(1L);
        assertTrue(after.isPresent());
        assertEquals("Обновленный текст", after.get().getText());
    }

    @Test
    void deleteById_removesComment() {
        repository.deleteById(1L);

        assertTrue(repository.findById(1L).isEmpty());
    }

    @Test
    void deletePost_cascadeDeletesComments() {
        jdbcTemplate.update("DELETE FROM posts WHERE id = 1");

        List<Comment> comments = repository.findByPostId(1L);

        assertTrue(comments.isEmpty());
    }
}
