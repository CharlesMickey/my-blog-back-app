package ru.art.home.my_blog_back_app.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.art.home.my_blog_back_app.model.Comment;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcCommentRepository implements CommentRepository {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Comment> commentRowMapper;

    public JdbcCommentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.commentRowMapper = (rs, rowNum) -> new Comment(
                rs.getLong("id"),
                rs.getString("text"),
                rs.getLong("post_id")
        );
    }

    @Override
    public List<Comment> findByPostId(Long postId) {
        return jdbcTemplate.query(
                "SELECT * FROM comments WHERE post_id = ? ORDER BY id",
                commentRowMapper, postId
        );
    }

    @Override
    public Optional<Comment> findById(Long id) {
        List<Comment> comments = jdbcTemplate.query(
                "SELECT * FROM comments WHERE id = ?",
                commentRowMapper, id
        );
        return comments.stream().findFirst();
    }

    @Override
    public Comment save(Comment comment) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO comments (text, post_id) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, comment.getText());
            ps.setLong(2, comment.getPostId());
            return ps;
        }, keyHolder);

        comment.setId(keyHolder.getKey().longValue());
        return comment;
    }

    @Override
    public Comment update(Comment comment) {
        jdbcTemplate.update(
                "UPDATE comments SET text = ? WHERE id = ?",
                comment.getText(),
                comment.getId()
        );
        return comment;
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM comments WHERE id = ?", id);
    }
}
