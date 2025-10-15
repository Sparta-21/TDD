package com.sparta.tdd.domain.review.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.tdd.domain.auth.UserDetailsImpl;
import com.sparta.tdd.domain.review.dto.*;
import com.sparta.tdd.domain.review.dto.request.ReviewReplyRequestDto;
import com.sparta.tdd.domain.review.dto.request.ReviewRequestDto;
import com.sparta.tdd.domain.review.dto.response.ReviewReplyResponseDto;
import com.sparta.tdd.domain.review.dto.response.ReviewResponseDto;
import com.sparta.tdd.domain.review.service.ReviewReplyService;
import com.sparta.tdd.domain.review.service.ReviewService;
import com.sparta.tdd.domain.user.enums.UserAuthority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
@EnableSpringDataWebSupport
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private ReviewReplyService reviewReplyService;

    private UserDetailsImpl customerDetails;
    private UserDetailsImpl ownerDetails;
    private UUID reviewId;
    private UUID storeId;
    private UUID orderId;
    private UUID replyId;

    @BeforeEach
    void 초기세팅() {
        reviewId = UUID.randomUUID();
        storeId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        replyId = UUID.randomUUID();

        customerDetails = new UserDetailsImpl(1L, "customer", UserAuthority.CUSTOMER);
        ownerDetails = new UserDetailsImpl(2L, "owner", UserAuthority.OWNER);
    }

    // UserDetails를 제대로 주입하는 헬퍼 메서드
    private RequestPostProcessor withUserDetails(UserDetailsImpl userDetails) {
        return request -> {
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.getSession().setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
            return request;
        };
    }

    // ==================== 리뷰 테스트 ====================

    @Test
    @DisplayName("리뷰 생성 성공")
    void 리뷰생성_성공() throws Exception {
        // given
        ReviewRequestDto requestDto = new ReviewRequestDto(
                "맛있어요!",
                storeId,
                5,
                "image.jpg"
        );

        ReviewResponseDto responseDto = new ReviewResponseDto(
                reviewId,
                storeId,
                orderId,
                "맛있어요!",
                5,
                1L,
                "image.jpg",
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        );

        given(reviewService.createReview(eq(1L), eq(orderId), any(ReviewRequestDto.class)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(post("/v1/reviews/order/{orderId}", orderId)
                        .with(withUserDetails(customerDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/v1/reviews/" + reviewId))
                .andExpect(jsonPath("$.reviewId").value(reviewId.toString()))
                .andExpect(jsonPath("$.content").value("맛있어요!"))
                .andExpect(jsonPath("$.rating").value(5));
    }

    @Test
    @DisplayName("리뷰 생성 실패 - 평점 null")
    void 평점_안주고_리뷰_써지는지_테스트() throws Exception {
        // given
        ReviewRequestDto requestDto = new ReviewRequestDto(
                "맛있어요!",
                storeId,
                null,
                "image.jpg"
        );

        // when & then
        mockMvc.perform(post("/v1/reviews/order/{orderId}", orderId)
                        .with(withUserDetails(customerDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("리뷰 조회 성공")
    void 리뷰_조회() throws Exception {
        // given
        ReviewResponseDto responseDto = new ReviewResponseDto(
                reviewId,
                storeId,
                orderId,
                "맛있어요!",
                5,
                1L,
                "image.jpg",
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        );

        given(reviewService.getReview(reviewId)).willReturn(responseDto);

        // when & then
        mockMvc.perform(get("/v1/reviews/{reviewId}", reviewId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(reviewId.toString()))
                .andExpect(jsonPath("$.content").value("맛있어요!"));
    }

    @Test
    @DisplayName("리뷰 수정 성공")
    void 리뷰_수정() throws Exception {
        // given
        ReviewUpdateDto updateDto = new ReviewUpdateDto(
                "더 맛있어요!",
                4,
                "new_image.jpg"
        );

        ReviewResponseDto responseDto = new ReviewResponseDto(
                reviewId,
                storeId,
                orderId,
                "더 맛있어요!",
                4,
                1L,
                "new_image.jpg",
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        );

        given(reviewService.updateReview(eq(reviewId), eq(1L), any(ReviewUpdateDto.class)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(patch("/v1/reviews/{reviewId}", reviewId)
                        .with(withUserDetails(customerDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("더 맛있어요!"))
                .andExpect(jsonPath("$.rating").value(4));
    }

    @Test
    @DisplayName("가게별 리뷰 목록 조회 성공")
    void 리뷰_목록_조회() throws Exception {
        // given
        List<ReviewResponseDto> reviews = List.of(
                new ReviewResponseDto(UUID.randomUUID(), storeId, orderId, "리뷰1", 5, 1L, null,
                        LocalDateTime.now(), LocalDateTime.now(), null),
                new ReviewResponseDto(UUID.randomUUID(), storeId, orderId, "리뷰2", 4, 1L, null,
                        LocalDateTime.now(), LocalDateTime.now(), null)
        );
        Page<ReviewResponseDto> page = new PageImpl<>(reviews, PageRequest.of(0, 10), reviews.size());

        given(reviewService.getReviewsByStore(eq(storeId), any())).willReturn(page);

        // when & then
        mockMvc.perform(get("/v1/reviews/store/{storeId}", storeId)
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @DisplayName("리뷰 삭제 성공")
    void 리뷰_삭제() throws Exception {
        // given
        doNothing().when(reviewService).deleteReview(reviewId, 1L);

        // when & then
        mockMvc.perform(delete("/v1/reviews/{reviewId}", reviewId)
                        .with(withUserDetails(customerDetails)))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    // ==================== 답글 테스트 ====================

    @Test
    @DisplayName("답글 생성 성공")
    void 답글_생성() throws Exception {
        // given
        ReviewReplyRequestDto requestDto = new ReviewReplyRequestDto("감사합니다!");

        ReviewReplyResponseDto responseDto = new ReviewReplyResponseDto(
                replyId,
                reviewId,
                storeId,
                2L,
                "감사합니다!",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        given(reviewReplyService.createReply(eq(reviewId), eq(2L), any(ReviewReplyRequestDto.class)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(post("/v1/reviews/{reviewId}/reply", reviewId)
                        .with(withUserDetails(ownerDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/v1/reviews/" + reviewId + "/reply"))
                .andExpect(jsonPath("$.replyId").value(replyId.toString()))
                .andExpect(jsonPath("$.reviewId").value(reviewId.toString()))
                .andExpect(jsonPath("$.content").value("감사합니다!"))
                .andExpect(jsonPath("$.ownerId").value(2L));
    }

    @Test
    @DisplayName("답글 생성 실패 - 주인이 아닐 때")
    void 답글_생성_실패_주인아님() throws Exception {
        // given
        ReviewReplyRequestDto requestDto = new ReviewReplyRequestDto("감사합니다!");

        given(reviewReplyService.createReply(eq(reviewId), eq(1L), any(ReviewReplyRequestDto.class)))
                .willThrow(new IllegalArgumentException("해당 가게의 소유자만 답글을 작성할 수 있습니다."));

        // when & then
        mockMvc.perform(post("/v1/reviews/{reviewId}/reply", reviewId)
                        .with(withUserDetails(customerDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("해당 가게의 소유자만 답글을 작성할 수 있습니다."));
    }

    @Test
    @DisplayName("답글 생성 실패 - content가 null")
    void 빈_답글_생성_시도() throws Exception {
        // given
        ReviewReplyRequestDto requestDto = new ReviewReplyRequestDto(null);

        // when & then
        mockMvc.perform(post("/v1/reviews/{reviewId}/reply", reviewId)
                        .with(withUserDetails(ownerDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("답글 수정 성공")
    void 답글수정() throws Exception {
        // given
        ReviewReplyRequestDto requestDto = new ReviewReplyRequestDto("수정된 답글입니다!");

        ReviewReplyResponseDto responseDto = new ReviewReplyResponseDto(
                replyId,
                reviewId,
                storeId,
                2L,
                "수정된 답글입니다!",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        given(reviewReplyService.updateReply(eq(reviewId), eq(2L), any(ReviewReplyRequestDto.class)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(patch("/v1/reviews/{reviewId}/reply", reviewId)
                        .with(withUserDetails(ownerDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("수정된 답글입니다!"));
    }

    @Test
    @DisplayName("답글 삭제 성공")
    void 답글삭제() throws Exception {
        // given
        doNothing().when(reviewReplyService).deleteReply(reviewId, 2L);

        // when & then
        mockMvc.perform(delete("/v1/reviews/{reviewId}/reply", reviewId)
                        .with(withUserDetails(ownerDetails)))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("리뷰 조회 성공 - 답글 포함")
    void 리뷰_조회_답글_개별() throws Exception {
        // given
        ReviewResponseDto.ReviewReplyInfo replyInfo =
                new ReviewResponseDto.ReviewReplyInfo("답글 내용");

        ReviewResponseDto responseDto = new ReviewResponseDto(
                reviewId,
                storeId,
                orderId,
                "맛있어요!",
                5,
                1L,
                "image.jpg",
                LocalDateTime.now(),
                LocalDateTime.now(),
                replyInfo
        );

        given(reviewService.getReview(reviewId)).willReturn(responseDto);

        // when & then
        mockMvc.perform(get("/v1/reviews/{reviewId}", reviewId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(reviewId.toString()))
                .andExpect(jsonPath("$.content").value("맛있어요!"))
                .andExpect(jsonPath("$.reply").exists())
                .andExpect(jsonPath("$.reply.content").value("답글 내용"));
    }

    @Test
    @DisplayName("가게별 리뷰 목록 조회 성공 - 답글 포함")
    void 리뷰_답글_목록_조회() throws Exception {
        // given
        ReviewResponseDto.ReviewReplyInfo replyInfo =
                new ReviewResponseDto.ReviewReplyInfo("답글 내용");

        List<ReviewResponseDto> reviews = List.of(
                new ReviewResponseDto(
                        UUID.randomUUID(), storeId, orderId, "리뷰1", 5, 1L, null,
                        LocalDateTime.now(), LocalDateTime.now(), replyInfo),
                new ReviewResponseDto(
                        UUID.randomUUID(), storeId, orderId, "리뷰2", 4, 1L, null,
                        LocalDateTime.now(), LocalDateTime.now(), null)
        );
        Page<ReviewResponseDto> page = new PageImpl<>(reviews, PageRequest.of(0, 10), reviews.size());

        given(reviewService.getReviewsByStore(eq(storeId), any())).willReturn(page);

        // when & then
        mockMvc.perform(get("/v1/reviews/store/{storeId}", storeId)
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].reply").exists())
                .andExpect(jsonPath("$.content[0].reply.content").value("답글 내용"))
                .andExpect(jsonPath("$.content[1].reply").doesNotExist());
    }
}