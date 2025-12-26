package ru.art.home.my_blog_back_app.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.art.home.my_blog_back_app.controller.CommentController;
import ru.art.home.my_blog_back_app.model.Comment;
import ru.art.home.my_blog_back_app.service.CommentService;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
class CommentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CommentService commentService;

    @Autowired
    ObjectMapper objectMapper;

    private final String BASE = "/api/posts/1/comments";

    @Test
    void getComments_shouldReturnList() throws Exception {
        when(commentService.getCommentsByPostId(1L))
                .thenReturn(List.of(
                        new Comment(1L, "Текст 1", 1L),
                        new Comment(2L, "Текст 2", 1L)
                ));

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].text").value("Текст 1"));

        verify(commentService).getCommentsByPostId(1L);
    }

    @Test
    void getComment_shouldReturnComment() throws Exception {
        when(commentService.getComment(1L, 1L))
                .thenReturn(new Comment(1L, "Комментарий", 1L));

        mockMvc.perform(get(BASE + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Комментарий"));
    }

    @Test
    void createComment_shouldReturnCreated() throws Exception {
        Comment req = new Comment(null, "Новый", 1L);
        Comment saved = new Comment(10L, "Новый", 1L);

        when(commentService.createComment(any())).thenReturn(saved);

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.text").value("Новый"));
    }

    @Test
    void updateComment_shouldReturnUpdated() throws Exception {
        Comment req = new Comment(null, "Обновлённый", 1L);
        Comment updated = new Comment(1L, "Обновлённый", 1L);

        when(commentService.updateComment(any())).thenReturn(updated);

        mockMvc.perform(put(BASE + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Обновлённый"));
    }

    @Test
    void deleteComment_shouldReturnOk() throws Exception {
        mockMvc.perform(delete(BASE + "/1"))
                .andExpect(status().isOk());

        verify(commentService).deleteComment(1L);
    }


    @Test
    void createComment_whenTextBlank_shouldReturn400() throws Exception {
        Comment req = new Comment(null, " ", 1L);

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createComment_whenPostIdNull_shouldReturn400() throws Exception {
        Comment req = new Comment(null, "Комментарий", null);

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
