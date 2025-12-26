package ru.art.home.my_blog_back_app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.multipart.MultipartFile;
import ru.art.home.my_blog_back_app.model.Comment;
import ru.art.home.my_blog_back_app.model.Post;
import ru.art.home.my_blog_back_app.repository.CommentRepository;
import ru.art.home.my_blog_back_app.repository.PostRepository;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Тесты сервиса постов")
class PostServiceTest {

    @MockitoBean
    private PostRepository postRepository;

    @MockitoBean
    private CommentRepository commentRepository;

    @Autowired
    private PostService postService;

    private Post testPost;

    @BeforeEach
    void setUp() {
        testPost = new Post(1L, "Test Post", "This is a test post content",
                Arrays.asList("test", "java"), 5, 2);

        String longText = "Это очень длинный пост. Но может быть и не совсем длинный"
                + "Но вот коротким его точно не назвать. Думаю в 128 символов должен буедт уложиться "
                + "Хотя и не точно. В общем нормальный это пост. Может всем постам пост и т.д.";
        Post testPostWithLongTet = new Post(2L, "Long Post", longText,
                List.of("long"), 0, 0);
    }

    @Test
    @DisplayName("Получение списка постов с пагинацией")
    void getPosts_shouldReturnPaginatedResults() {

        List<Post> posts = List.of(testPost);
        when(postRepository.findAllWithPaginationAndSearch(anyString(), anyInt(), anyInt()))
                .thenReturn(posts);
        when(postRepository.getLastPageNumber(anyString(), anyInt())).thenReturn(3);

        Map<String, Object> result = postService.getPosts("test", 1, 10);

        assertNotNull(result);
        assertFalse((Boolean) result.get("hasPrev"));
        assertTrue((Boolean) result.get("hasNext"));
        assertEquals(3, result.get("lastPage"));

        @SuppressWarnings("unchecked")
        List<Post> resultPosts = (List<Post>) result.get("posts");
        assertEquals(1, resultPosts.size());
        assertEquals("Test Post", resultPosts.get(0).getTitle());

        verify(postRepository).findAllWithPaginationAndSearch("test", 1, 10);
        verify(postRepository).getLastPageNumber("test", 10);
    }

    @Test
    @DisplayName("Обрезание текста при длине больше 128 символов")
    void getPosts_shouldTruncateText_whenTextTooLong() {

        String longText = "A".repeat(234);
        Post postWithLongText = new Post(1L, "Test", longText, List.of(), 0, 0);
        List<Post> posts = List.of(postWithLongText);

        when(postRepository.findAllWithPaginationAndSearch(anyString(), anyInt(), anyInt()))
                .thenReturn(posts);
        when(postRepository.getLastPageNumber(anyString(), anyInt())).thenReturn(1);

        Map<String, Object> result = postService.getPosts("", 1, 10);

        @SuppressWarnings("unchecked")
        List<Post> resultPosts = (List<Post>) result.get("posts");
        String truncatedText = resultPosts.get(0).getText();
        assertEquals(129, truncatedText.length());
        assertTrue(truncatedText.endsWith("…"));
    }

    @Test
    @DisplayName("Не обрезать текст при длине меньше или равно 128 символов")
    void getPosts_shouldNotTruncateText_whenTextShort() {

        String shortText = "Short text";
        Post postWithShortText = new Post(1L, "Test", shortText, List.of(), 0, 0);
        List<Post> posts = List.of(postWithShortText);

        when(postRepository.findAllWithPaginationAndSearch(anyString(), anyInt(), anyInt()))
                .thenReturn(posts);
        when(postRepository.getLastPageNumber(anyString(), anyInt())).thenReturn(1);

        Map<String, Object> result = postService.getPosts("", 1, 10);

        @SuppressWarnings("unchecked")
        List<Post> resultPosts = (List<Post>) result.get("posts");
        assertEquals(shortText, resultPosts.get(0).getText());
    }

    @Test
    @DisplayName("Получение поста по ID")
    void getPost_shouldReturnPost_whenPostExists() {

        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));

        Post result = postService.getPost(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Post", result.getTitle());
        verify(postRepository).findById(1L);
    }

    @Test
    @DisplayName("Выброс исключения при получении несуществующего поста")
    void getPost_shouldThrowException_whenPostNotFound() {

        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> postService.getPost(1L));
        assertTrue(exception.getMessage().contains("Post not found"));
    }

    @Test
    @DisplayName("Создание нового поста")
    void createPost_shouldSetDefaultCountersAndSave() {

        Post newPost = new Post(null, "New Post", "Content", List.of("tag1"), 999, 999);
        Post savedPost = new Post(1L, "New Post", "Content", List.of("tag1"), 0, 0);

        when(postRepository.save(any(Post.class))).thenReturn(savedPost);

        Post result = postService.createPost(newPost);

        verify(postRepository).save(argThat(p ->
                p.getLikesCount() == 0 &&
                        p.getCommentsCount() == 0 &&
                        p.getTitle().equals("New Post")
        ));

        assertEquals(1L, result.getId());
        assertEquals(0, result.getLikesCount());
        assertEquals(0, result.getCommentsCount());
    }

    @Test
    @DisplayName("Обновление поста")
    void updatePost_shouldUpdateWithExistingCounters() {

        Post updatedPost = new Post(1L, "Updated Title", "Updated content",
                List.of("updated"), 0, 0);

        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(postRepository.update(any(Post.class))).thenReturn(updatedPost);

        Post result = postService.updatePost(updatedPost);

        verify(postRepository).update(argThat(p ->
                p.getId() == 1L &&
                        p.getTitle().equals("Updated Title") &&
                        p.getLikesCount() == 5 &&
                        p.getCommentsCount() == 2
        ));
    }

    @Test
    @DisplayName("Удаление поста с комментариями")
    void deletePost_shouldDeletePostAndComments() {

        List<Comment> comments = List.of(
                new Comment(1L, "Comment 1", 1L),
                new Comment(2L, "Comment 2", 1L)
        );
        when(commentRepository.findByPostId(1L)).thenReturn(comments);

        postService.deletePost(1L);

        verify(commentRepository).findByPostId(1L);
        verify(commentRepository).deleteById(1L);
        verify(commentRepository).deleteById(2L);
        verify(postRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Инкремент лайков поста")
    void incrementLikes_shouldIncrementAndReturnCount() {

        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));

        int result = postService.incrementLikes(1L);

        verify(postRepository).incrementLikes(1L);
        assertEquals(5, result);
    }

    @Test
    @DisplayName("Обновление изображения поста")
    void updateImage_shouldUpdateImage() throws IOException {

        MultipartFile image = mock(MultipartFile.class);
        when(image.getBytes()).thenReturn(new byte[]{1, 2, 3, 4, 5});

        postService.updateImage(1L, image);

        verify(postRepository).updateImage(eq(1L), any(byte[].class));
    }

    @Test
    @DisplayName("Получение изображения поста")
    void getImage_shouldReturnImageBytes() {

        byte[] imageBytes = new byte[]{1, 2, 3, 4, 5};
        when(postRepository.getImage(1L)).thenReturn(imageBytes);

        byte[] result = postService.getImage(1L);

        assertArrayEquals(imageBytes, result);
        verify(postRepository).getImage(1L);
    }

    @Test
    @DisplayName("Поиск постов с пустым поисковым запросом")
    void getPosts_shouldHandleEmptySearch() {
        List<Post> posts = List.of(testPost);
        when(postRepository.findAllWithPaginationAndSearch(eq(""), anyInt(), anyInt()))
                .thenReturn(posts);
        when(postRepository.getLastPageNumber(eq(""), anyInt())).thenReturn(1);

        Map<String, Object> result = postService.getPosts("", 1, 10);

        assertNotNull(result);
        verify(postRepository).findAllWithPaginationAndSearch("", 1, 10);
    }
}
