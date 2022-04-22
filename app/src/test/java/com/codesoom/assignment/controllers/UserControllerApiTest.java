package com.codesoom.assignment.controllers;

import com.codesoom.assignment.Utf8MockMvc;
import com.codesoom.assignment.domain.User;
import com.codesoom.assignment.domain.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Utf8MockMvc
@DisplayName("User 컨트롤러에서")
public class UserControllerApiTest {
    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String validUserInput = "{\"name\": \"김갑생\", \"email\": \"gabseng@naver.com\", \"password\": \"gabgabhada123\"}";

    @BeforeEach
    void setUp() {
        // 리포지토리 초기화
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("POST /user 요청은")
    class Describe_post_user {
        private final MockHttpServletRequestBuilder requestBuilder;

        public Describe_post_user() {
            requestBuilder = post("/user");
        }

        @Nested
        @DisplayName("유효한 입력값을 받는다면")
        class Context_valid_input {
            private final ResultActions actions;

            public Context_valid_input() throws Exception {
                actions = mockMvc.perform(requestBuilder);
            }

            @Test
            @DisplayName("201 CREATE 응답을 반환한다.")
            void It_returns_201_created_response() throws Exception {
                actions.andExpect(status().isCreated());
            }

            @Test
            @DisplayName("리포지토리에 User 를 추가하고, 추가한 User 를 반환한다.")
            void It_returns_user() throws Exception {
                assertThat(userRepository.count()).isNotZero();
                User user = userRepository.findAll().iterator().next();

                actions.andExpect(content().string(objectMapper.writeValueAsString(user)));
            }
        }
    }
}
