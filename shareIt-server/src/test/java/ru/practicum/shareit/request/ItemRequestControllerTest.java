package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ItemRequestService requestService;

    @Test
    void createRequest_shouldReturnRequest() throws Exception {
        ItemRequestDto dto = ItemRequestDto.builder().id(1L).description("Нужна лестница").build();
        when(requestService.create(anyLong(), any(ItemRequestDto.class))).thenReturn(dto);

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(dto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(dto.getDescription())));
    }

    @Test
    void getOwnRequests_shouldReturnList() throws Exception {
        ItemRequestDto dto = ItemRequestDto.builder().id(1L).description("Нужна лестница").build();
        when(requestService.getOwnRequests(anyLong())).thenReturn(List.of(dto));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id", is(dto.getId()), Long.class));
    }

    @Test
    void getAllRequests_shouldReturnList() throws Exception {
        when(requestService.getAllRequests(anyLong(), anyInt(), anyInt())).thenReturn(List.of());

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }
}