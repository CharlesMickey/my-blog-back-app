package ru.art.home.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.TestPropertySource;
import ru.art.home.configuration.DataSourceConfiguration;
import ru.art.home.model.Post;
import ru.art.home.repository.JdbcCommentRepository;
import ru.art.home.repository.JdbcPostRepository;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(classes = {
    DataSourceConfiguration.class,
    PostService.class,
    JdbcPostRepository.class,
    JdbcCommentRepository.class
})
@TestPropertySource(locations = "classpath:application-test.properties")
class PostServiceIntegrationTest {

    @Autowired
    private PostService postService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM comments");
        jdbcTemplate.execute("DELETE FROM posts");

        jdbcTemplate.update("""
                        INSERT INTO posts (id, title, text, tags, likes_count, comments_count)
                        VALUES (1,'Тестовый пост','Контент тестового поста','java',5,2)
                """);
        jdbcTemplate.update("""
                        INSERT INTO comments (id, text, post_id)
                        VALUES (1,'Комментарий к посту',1)
                """);
    }

    @Test
    void getPosts_returnsAllPosts() {
        Map<String, Object> result = postService.getPosts("", 1, 10);
        @SuppressWarnings("unchecked")
        List<Post> posts = (List<Post>) result.get("posts");

        assertEquals(1, posts.size());
        assertEquals("Тестовый пост", posts.get(0).getTitle());
        assertEquals(5, posts.get(0).getLikesCount());
    }

    @Test
    void getPost_returnsSinglePost() {
        Post post = postService.getPost(1L);
        assertNotNull(post);
        assertEquals("Тестовый пост", post.getTitle());
        assertEquals(5, post.getLikesCount());
        assertEquals(2, post.getCommentsCount());
    }

    @Test
    void createPost_addsPostToDb() {
        Post newPost = new Post(null, "Новый пост", "Контент нового поста",
                List.of("новый", "java"), 999, 999);
        Post saved = postService.createPost(newPost);

        assertNotNull(saved.getId());
        assertEquals(0, saved.getLikesCount());
        assertEquals(0, saved.getCommentsCount());

        Map<String, Object> result = postService.getPosts("", 1, 10);
        @SuppressWarnings("unchecked")
        List<Post> posts = (List<Post>) result.get("posts");

        assertEquals(2, posts.size());
    }

    @Test
    void deletePost_removesPost() {
        postService.deletePost(1L);

        Map<String, Object> result = postService.getPosts("", 1, 10);
        @SuppressWarnings("unchecked")
        List<Post> posts = (List<Post>) result.get("posts");

        assertTrue(posts.isEmpty());
    }

    @Test
    void getPosts_filterByTag() {

        Map<String, Object> result = postService.getPosts("#java", 1, 10);
        @SuppressWarnings("unchecked")
        List<Post> posts = (List<Post>) result.get("posts");

        assertEquals(1, posts.size());

        result = postService.getPosts("python", 1, 10);
        @SuppressWarnings("unchecked")
        List<Post> posts2 = (List<Post>) result.get("posts");

        assertTrue(posts2.isEmpty());
    }

    @Test
    void getPosts_paginationWorks() {

        for (int i = 2; i <= 15; i++) {
            jdbcTemplate.update("""
                        INSERT INTO posts (id, title, text, tags, likes_count, comments_count)
                        VALUES (?,?,?,?,?,?)
                    """, i, "Пост " + i, "Контент " + i, "тег" + i, i, i);
        }

        Map<String, Object> result = postService.getPosts("", 1, 10);
        @SuppressWarnings("unchecked")
        List<Post> postsPage1 = (List<Post>) result.get("posts");

        assertEquals(10, postsPage1.size());
        assertTrue((Boolean) result.get("hasNext"));
        assertFalse((Boolean) result.get("hasPrev"));

        result = postService.getPosts("", 2, 10);
        @SuppressWarnings("unchecked")
        List<Post> postsPage2 = (List<Post>) result.get("posts");

        assertEquals(5, postsPage2.size());
        assertFalse((Boolean) result.get("hasNext"));
        assertTrue((Boolean) result.get("hasPrev"));
    }

    @Test
    void incrementLikes_increasesLikesCount() {
        int initialLikes = postService.getPost(1L).getLikesCount();
        int newLikes = postService.incrementLikes(1L);

        assertEquals(initialLikes + 1, newLikes);

        Integer dbLikes = jdbcTemplate.queryForObject(
                "SELECT likes_count FROM posts WHERE id = 1", Integer.class);
        assertEquals(initialLikes + 1, dbLikes);
    }

    @Test
    void getPosts_textTruncation() {
        Post longPost = new Post(null, "Длинный пост",
                "Очень длинный текст поста, который должен быть обрезан до 128 символов. "
                + "Создаем пост с длинным текстомСоздаем пост с длинным текстом "
                + "Создаем пост с длинным текстомСоздаем пост с длинным текстомСоздаем пост с длинным ",
                List.of("длинный"), 0, 0);

        postService.createPost(longPost);

        Map<String, Object> result = postService.getPosts("", 1, 10);
        @SuppressWarnings("unchecked")
        List<Post> posts = (List<Post>) result.get("posts");

        Post retrievedPost = posts.stream()
                .filter(p -> p.getTitle().equals("Длинный пост"))
                .findFirst()
                .orElseThrow();

        assertTrue(retrievedPost.getText().length() <= 129);
        assertTrue(retrievedPost.getText().endsWith("…"));
    }

    @Test
    void updatePost_updatesPost() {
        Post postToUpdate = postService.getPost(1L);
        postToUpdate.setTitle("Обновленный заголовок");
        postToUpdate.setText("Обновленный текст");
        postToUpdate.setTags(List.of("обновленный"));

        Post updated = postService.updatePost(postToUpdate);

        assertEquals("Обновленный заголовок", updated.getTitle());
        assertEquals("Обновленный текст", updated.getText());
        assertEquals(List.of("обновленный"), updated.getTags());

        assertEquals(5, updated.getLikesCount());
        assertEquals(2, updated.getCommentsCount());
    }
}
