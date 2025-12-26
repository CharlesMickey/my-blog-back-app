package ru.art.home.my_blog_back_app.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.art.home.my_blog_back_app.model.Post;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcPostRepository implements PostRepository {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Post> postRowMapper;

    public JdbcPostRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.postRowMapper = (rs, rowNum) -> {
            Post post = new Post();
            post.setId(rs.getLong("id"));
            post.setTitle(rs.getString("title"));
            post.setText(rs.getString("text"));
            String tagsStr = rs.getString("tags");
            post.setTags(tagsStr != null ? Arrays.asList(tagsStr.split(",")) : List.of());
            post.setLikesCount(rs.getInt("likes_count"));
            post.setCommentsCount(rs.getInt("comments_count"));
            return post;
        };
    }

    @Override
    public List<Post> findAll() {
        return jdbcTemplate.query("SELECT * FROM posts", postRowMapper);
    }

    @Override
    public List<Post> findAllWithPaginationAndSearch(String search, int pageNumber, int pageSize) {
        int offset = (pageNumber - 1) * pageSize;

        if (search == null || search.trim().isEmpty()) {
            return jdbcTemplate.query(
                    "SELECT * FROM posts ORDER BY id DESC LIMIT ? OFFSET ?",
                    postRowMapper, pageSize, offset
            );
        }

        String[] words = Arrays.stream(search.split("\\s+"))
                .filter(w -> !w.isEmpty())
                .toArray(String[]::new);

        String titleSearch = Arrays.stream(words)
                .filter(w -> !w.startsWith("#"))
                .reduce((a, b) -> a + " " + b)
                .orElse("");

        List<String> tags = Arrays.stream(words)
                .filter(w -> w.startsWith("#"))
                .map(w -> w.substring(1))
                .toList();

        StringBuilder sql = new StringBuilder("SELECT * FROM posts WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (!titleSearch.isEmpty()) {
            sql.append(" AND title LIKE ?");
            params.add("%" + titleSearch + "%");
        }

        for (String tag : tags) {
            sql.append(" AND tags LIKE ?");
            params.add("%" + tag + "%");
        }

        sql.append(" ORDER BY id DESC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add(offset);

        return jdbcTemplate.query(sql.toString(), postRowMapper, params.toArray());
    }

    @Override
    public Optional<Post> findById(Long id) {
        List<Post> posts = jdbcTemplate.query("SELECT * FROM posts WHERE id = ?", postRowMapper, id);
        return posts.stream().findFirst();
    }

    @Override
    public Post save(Post post) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO posts (title, text, tags, likes_count, comments_count) VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getText());
            ps.setString(3, String.join(",", post.getTags()));
            ps.setInt(4, post.getLikesCount());
            ps.setInt(5, post.getCommentsCount());
            return ps;
        }, keyHolder);

        post.setId(keyHolder.getKey().longValue());
        return post;
    }

    @Override
    public Post update(Post post) {
        jdbcTemplate.update(
                "UPDATE posts SET title = ?, text = ?, tags = ?, likes_count = ?, comments_count = ? WHERE id = ?",
                post.getTitle(),
                post.getText(),
                String.join(",", post.getTags()),
                post.getLikesCount(),
                post.getCommentsCount(),
                post.getId()
        );
        return post;
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM posts WHERE id = ?", id);
    }

    @Override
    public void incrementLikes(Long postId) {
        jdbcTemplate.update("UPDATE posts SET likes_count = likes_count + 1 WHERE id = ?", postId);
    }

    @Override
    public void updateImage(Long postId, byte[] image) {
        jdbcTemplate.update("UPDATE posts SET image = ? WHERE id = ?", image, postId);
    }

    @Override
    public byte[] getImage(Long postId) {
        return jdbcTemplate.queryForObject(
                "SELECT image FROM posts WHERE id = ?",
                byte[].class,
                postId
        );
    }

    @Override
    public int countPosts(String search) {
        if (search == null || search.trim().isEmpty()) {
            return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM posts", Integer.class);
        } else {
            String searchPattern = "%" + search + "%";
            return jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM posts WHERE title LIKE ? OR text LIKE ?",
                    Integer.class,
                    searchPattern, searchPattern
            );
        }
    }

    @Override
    public int getLastPageNumber(String search, int pageSize) {
        int total = countPosts(search);
        return (int) Math.ceil((double) total / pageSize);
    }
}
