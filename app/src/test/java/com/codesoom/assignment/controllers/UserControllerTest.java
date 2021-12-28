package com.codesoom.assignment.controllers;

import com.codesoom.assignment.UserNotFoundException;
import com.codesoom.assignment.application.UserService;
import com.codesoom.assignment.domain.User;
import com.codesoom.assignment.dto.UserData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("userController class")
@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String USER_NAME = "코드숨";
    private static final String USER_PASSWORD = "1234";
    private static final String USER_EMAIL = "codesoom@gmail.com";

    private static final String NEW_NAME = "스프링";
    private static final String NEW_PASSWORD = "5678";
    private static final String NEW_EMAIL = "spring@gmail.com";

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .alwaysDo(print())
                .build();

        User user = User.builder()
                .id(1L)
                .name(USER_NAME)
                .password(USER_PASSWORD)
                .email(USER_EMAIL)
                .build();

        given(userService.createUser(any(UserData.class))).willReturn(user);
    }

    @Nested
    @DisplayName("POST 요청은")
    class Describe_Post {
        @Nested
        @DisplayName("사용자 정보가 누락되지 않았다면")
        class Context_With_Valid_Attributes {
            @Test
            @DisplayName("사용자를 저장하고 Created를  응답한다.")
            void it_return_status() throws Exception {
                UserData userData = UserData.builder()
                        .name(USER_NAME)
                        .password(USER_PASSWORD)
                        .email(USER_EMAIL)
                        .build();
                String userContext = objectMapper.writeValueAsString(userData);

                mockMvc.perform(post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(userContext))
                        .andExpect(status().isCreated())
                        .andExpect(content().string(containsString(USER_NAME)));
            }
        }

        @Nested
        @DisplayName("사용자 정보가 누락되었다면")
        class Context_With_InValid_Attributes {
            @Test
            @DisplayName("잘못된 요청을 응답한다.")
            void it_return_status() throws Exception {
                UserData userData = UserData.builder()
                        .name(USER_NAME)
                        .password(USER_PASSWORD)
                        .build();
                String userContext = objectMapper.writeValueAsString(userData);

                mockMvc.perform(post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(userContext))
                        .andExpect(status().isBadRequest());
            }
        }
    }

    @Nested
    @DisplayName("Patch 요청은")
    class Describe_Patch {
        @Nested
        @DisplayName("id에 해당하는 사용자가 있다면")
        class Context_With_Exist_user {
            @BeforeEach
            void setUp() {
                given(userService.updateUser(eq(1L), any(UserData.class)))
                        .will(invocation -> {
                            Long id = invocation.getArgument(0);
                            UserData userData = invocation.getArgument(1);
                            return User.builder()
                                    .id(id)
                                    .name(userData.getName())
                                    .password(userData.getPassword())
                                    .email(userData.getEmail())
                                    .build();
                        });
            }

            @Test
            @DisplayName("사용자 정보를 수정하고 리턴한다.")
            void it_fix_user_return() throws Exception {
                UserData userData = UserData.builder()
                        .name(NEW_NAME)
                        .password(NEW_PASSWORD)
                        .email(NEW_EMAIL)
                        .build();
                String userContext = objectMapper.writeValueAsString(userData);

                mockMvc.perform(patch("/users/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(userContext))
                        .andExpect(status().isOk())
                        .andExpect(content().string(containsString(NEW_NAME)));

                verify(userService).updateUser(eq(1L), any(UserData.class));
            }
        }

        @Nested
        @DisplayName("id에 해당하는 사용자가 없다면")
        class Context_With_not_exist_user {
            @BeforeEach
            void setUp() {
                given(userService.updateUser(eq(1000L), any(UserData.class)))
                        .willThrow(new UserNotFoundException(1000L));
            }

            @Test
            @DisplayName("사용자 정보를 찾을 수 없다고 응답한다.")
            void it_fix_user_return() throws Exception {
                UserData userData = UserData.builder()
                        .name(NEW_NAME)
                        .password(NEW_PASSWORD)
                        .email(NEW_EMAIL)
                        .build();
                String userContext = objectMapper.writeValueAsString(userData);

                mockMvc.perform(patch("/users/1000")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(userContext))
                        .andExpect(status().isNotFound());

                verify(userService).updateUser(eq(1000L), any(UserData.class));
            }
        }
    }

    @Nested
    @DisplayName("Delete 요청은")
    class Describe_Delete {
        @Nested
        @DisplayName("id에 해당하는 사용자가 있다면")
        class Context_With_Exist_user {
            @Test
            @DisplayName("사용자를 삭제하고 NoContent를 응답한다.")
            void it_return_user() throws Exception {
                mockMvc.perform(delete("/users/1"))
                        .andExpect(status().isNoContent());

                verify(userService).deleteUser(1L);
            }
        }

        @Nested
        @DisplayName("id에 해당하는 사용자가 없다면")
        class Context_With_Not_Exist_user {
            @BeforeEach
            void setUp() {
                given(userService.deleteUser(1000L))
                        .willThrow(new UserNotFoundException(1000L));
            }

            @Test
            @DisplayName("사용자를 찾을 수 없다고 응답한다.")
            void it_return_user() throws Exception {
                mockMvc.perform(delete("/users/1000"))
                        .andExpect(status().isNotFound());

                verify(userService).deleteUser(1000L);
            }
        }
    }
}
