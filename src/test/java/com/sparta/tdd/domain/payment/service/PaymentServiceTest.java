package com.sparta.tdd.domain.payment.service;

import static com.sparta.tdd.domain.payment.enums.CardCompany.KB;
import static com.sparta.tdd.domain.payment.enums.CardCompany.SHINHAN;
import static com.sparta.tdd.domain.payment.enums.PaymentStatus.CANCELLED;
import static com.sparta.tdd.domain.payment.enums.PaymentStatus.COMPLETED;
import static com.sparta.tdd.domain.payment.enums.PaymentStatus.PENDING;
import static com.sparta.tdd.domain.store.enums.StoreCategory.KOREAN;
import static com.sparta.tdd.domain.user.enums.UserAuthority.CUSTOMER;
import static com.sparta.tdd.domain.user.enums.UserAuthority.OWNER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sparta.tdd.domain.menu.dto.MenuRequestDto;
import com.sparta.tdd.domain.menu.entity.Menu;
import com.sparta.tdd.domain.order.dto.OrderItemInfoDto;
import com.sparta.tdd.domain.order.entity.Order;
import com.sparta.tdd.domain.order.enums.OrderStatus;
import com.sparta.tdd.domain.order.repository.OrderRepository;
import com.sparta.tdd.domain.orderMenu.entity.OrderMenu;
import com.sparta.tdd.domain.payment.dto.PaymentDetailResponseDto;
import com.sparta.tdd.domain.payment.dto.PaymentListResponseDto;
import com.sparta.tdd.domain.payment.dto.UpdatePaymentStatusRequest;
import com.sparta.tdd.domain.payment.entity.Payment;
import com.sparta.tdd.domain.payment.repository.PaymentRepository;
import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.store.repository.StoreRepository;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.domain.user.repository.UserRepository;
import com.sparta.tdd.global.exception.BusinessException;
import com.sparta.tdd.global.exception.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentResultProcessService paymentResultProcessService;

    @InjectMocks
    private PaymentService paymentService;

    private User customer;
    private User owner;
    private Store store;
    private Order order;
    private Payment payment;
    private Menu menu;

    @BeforeEach
    void setUp() {
        // Customer
        customer = User.builder()
            .username("customer")
            .password("password123!")
            .nickname("고객")
            .authority(CUSTOMER)
            .build();
        ReflectionTestUtils.setField(customer, "id", 1L);

        // Owner
        owner = User.builder()
            .username("owner")
            .password("password123@")
            .nickname("사장")
            .authority(OWNER)
            .build();
        ReflectionTestUtils.setField(owner, "id", 2L);

        // Store
        store = Store.builder()
            .name("맛있는 식당")
            .description("정말 맛있어요")
            .category(KOREAN)
            .user(owner)
            .build();
        ReflectionTestUtils.setField(store, "id", UUID.randomUUID());

        // Menu
        menu = Menu.builder()
            .name("김치찌개")
            .description("맛있는 김치찌개")
            .price(11000)
            .imageUrl(null)
            .store(store)
            .build();
        ReflectionTestUtils.setField(menu, "id", UUID.randomUUID());

        // Order
        order = Order.builder()
            .address("서울시 강남구")
            .orderStatus(OrderStatus.PENDING)
            .orderMenuList(new ArrayList<>())
            .store(store)
            .user(customer)
            .build();
        ReflectionTestUtils.setField(order, "id", UUID.randomUUID());

        // OrderMenu
        OrderMenu orderMenu = OrderMenu.builder()
            .menu(menu)
            .quantity(2)
            .price(8000)
            .order(order)
            .build();
        ReflectionTestUtils.setField(orderMenu, "id", UUID.randomUUID());
        order.addOrderMenu(orderMenu);

        // Payment
        payment = Payment.builder()
            .number("TEST-PAY-1")
            .amount(16000L)
            .cardCompany(SHINHAN)
            .cardNumber("1234567890123456")
            .status(COMPLETED)
            .user(customer)
            .order(order)
            .build();
        ReflectionTestUtils.setField(payment, "id", UUID.randomUUID());
    }

    @Nested
    @DisplayName("고객 결제 내역 조회")
    class GetCustomerPaymentHistoryTest {

        @Test
        @DisplayName("고객의 결제 내역을 페이징하여 조회")
        void getCustomerPaymentHistory() {
            // given
            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
            List<Payment> payments = List.of(payment);
            Page<Payment> paymentPage = new PageImpl<>(payments, pageable, 1);

            when(paymentRepository.findPaymentsByUserId(eq(1L), eq(null), any(Pageable.class)))
                .thenReturn(paymentPage);

            // when
            Page<PaymentListResponseDto> result = paymentService.getCustomerPaymentHistory(1L, pageable, null);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().getFirst().paymentNumber()).isEqualTo(payment.getNumber());
            assertThat(result.getContent().getFirst().price()).isEqualTo(payment.getAmount());

            verify(paymentRepository).findPaymentsByUserId(eq(1L), eq(null), any(Pageable.class));
        }

        @Test
        @DisplayName("키워드로 필터링하여 조회")
        void getCustomerPaymentHistoryWithKeyword() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            List<Payment> payments = List.of(payment);
            Page<Payment> paymentPage = new PageImpl<>(payments, pageable, 1);

            when(paymentRepository.findPaymentsByUserId(eq(1L), eq("SHINHAN"), any(Pageable.class)))
                .thenReturn(paymentPage);

            // when
            Page<PaymentListResponseDto> result = paymentService.getCustomerPaymentHistory(1L, pageable, "SHINHAN");

            // then
            assertThat(result.getContent()).hasSize(1);
            verify(paymentRepository).findPaymentsByUserId(eq(1L), eq("SHINHAN"), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("가게 결제 내역 조회")
    class GetStorePaymentHistoryTest {

        @Test
        @DisplayName("가게 주인이 본인 가게의 결제 내역 조회")
        void getStorePaymentHistory() {
            // given
            UUID storeId = (UUID) ReflectionTestUtils.getField(store, "id");
            Pageable pageable = PageRequest.of(0, 10);
            List<Payment> payments = List.of(payment);
            Page<Payment> paymentPage = new PageImpl<>(payments, pageable, 1);

            when(storeRepository.existsByIdAndUserIdAndDeletedAtIsNull(storeId, 2L))
                .thenReturn(true);
            when(paymentRepository.findPaymentsByStoreId(eq(storeId), eq(null), any(Pageable.class)))
                .thenReturn(paymentPage);

            // when
            Page<PaymentListResponseDto> result = paymentService.getStorePaymentHistory(2L, storeId, pageable, null);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().storeName()).isEqualTo(store.getName());

            verify(storeRepository).existsByIdAndUserIdAndDeletedAtIsNull(storeId, 2L);
            verify(paymentRepository).findPaymentsByStoreId(eq(storeId), eq(null), any(Pageable.class));
        }

        @Test
        @DisplayName("본인 가게가 아닌 경우 예외 발생")
        void getStorePaymentHistory_notOwner() {
            // given
            UUID storeId = (UUID) ReflectionTestUtils.getField(store, "id");
            Pageable pageable = PageRequest.of(0, 10);

            when(storeRepository.existsByIdAndUserIdAndDeletedAtIsNull(storeId, 1L))
                .thenReturn(false);

            // when & then
            assertThatThrownBy(() -> paymentService.getStorePaymentHistory(1L, storeId, pageable, null))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.GET_STORE_PAYMENT_DENIED.getMessage());

            verify(storeRepository).existsByIdAndUserIdAndDeletedAtIsNull(storeId, 1L);
            verify(paymentRepository, never()).findPaymentsByStoreId(storeId, null, pageable);
        }
    }

    @Nested
    @DisplayName("결제 상세 조회")
    class GetPaymentHistoryDetailTest {

        @Test
        @DisplayName("결제 상세 정보 조회 성공")
        void getPaymentHistoryDetail() {
            // given
            UUID paymentId = (UUID) ReflectionTestUtils.getField(payment, "id");

            when(paymentRepository.findPaymentDetailById(paymentId))
                .thenReturn(Optional.of(payment));

            // when
            PaymentDetailResponseDto result = paymentService.getPaymentHistoryDetail(paymentId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.paymentNumber()).isEqualTo(payment.getNumber());
            assertThat(result.price()).isEqualTo(payment.getAmount());
            assertThat(result.cardCompany()).isEqualTo(payment.getCardCompany().getDescription());
            assertThat(result.cardNumber()).isEqualTo("1234 **** **** 3456");
            assertThat(result.restaurant().storeName()).isEqualTo(store.getName());
            assertThat(result.orderItem()).hasSize(1);
            assertThat(result.orderItem().getFirst().menuName()).isEqualTo(menu.getName());
            assertThat(result.orderItem().getFirst().quantity()).isEqualTo(2);
            assertThat(result.orderItem().getFirst().totalPrice()).isEqualTo(16000);

            verify(paymentRepository).findPaymentDetailById(paymentId);
        }

        @Test
        @DisplayName("존재하지 않는 결제 ID로 조회 시 예외 발생")
        void getPaymentHistoryDetail_notFound() {
            // given
            UUID paymentId = UUID.randomUUID();
            when(paymentRepository.findPaymentDetailById(paymentId))
                .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> paymentService.getPaymentHistoryDetail(paymentId))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.PAYMENT_NOT_FOUND.getMessage());

            verify(paymentRepository).findPaymentDetailById(paymentId);
        }
    }

    @Nested
    @DisplayName("결제 상태 변경")
    class ChangePaymentStatusTest {

        @Test
        @DisplayName("결제 상태를 COMPLETED로 변경")
        void changePaymentStatusToCompleted() {
            // given
            UUID paymentId = (UUID) ReflectionTestUtils.getField(payment, "id");
            Payment pendingPayment = Payment.builder()
                .number("TEST-PAY-1")
                .amount(10000L)
                .cardCompany(KB)
                .cardNumber("1234567890123456")
                .status(PENDING)
                .user(customer)
                .build();
            ReflectionTestUtils.setField(pendingPayment, "id", paymentId);

            UpdatePaymentStatusRequest request = new UpdatePaymentStatusRequest(COMPLETED);

            when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(pendingPayment));

            // when
            paymentService.changePaymentStatus(paymentId, request);

            // then
            assertThat(pendingPayment.getStatus()).isEqualTo(COMPLETED);
            assertThat(pendingPayment.getProcessedAt()).isNotNull();

            verify(paymentRepository).findById(paymentId);
        }

        @Test
        @DisplayName("결제 상태를 CANCELLED로 변경")
        void changePaymentStatusToCancelled() {
            // given
            UUID paymentId = (UUID) ReflectionTestUtils.getField(payment, "id");
            UpdatePaymentStatusRequest request = new UpdatePaymentStatusRequest(CANCELLED);

            when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

            // when
            paymentService.changePaymentStatus(paymentId, request);

            // then
            assertThat(payment.getStatus()).isEqualTo(CANCELLED);

            verify(paymentRepository).findById(paymentId);
        }

        @Test
        @DisplayName("존재하지 않는 결제 ID로 상태 변경 시 예외 발생")
        void changePaymentStatus_notFound() {
            // given
            UUID paymentId = UUID.randomUUID();
            UpdatePaymentStatusRequest request = new UpdatePaymentStatusRequest(COMPLETED);

            when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> paymentService.changePaymentStatus(paymentId, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_NOT_FOUND);

            verify(paymentRepository).findById(paymentId);
        }
    }

    @Nested
    @DisplayName("비즈니스 로직 검증")
    class BusinessLogicTest {

        @Test
        @DisplayName("결제 금액과 주문 메뉴 총합 일치 검증")
        void verifyPaymentAmountMatchesOrderTotal() {
            // given
            UUID paymentId = (UUID) ReflectionTestUtils.getField(payment, "id");

            when(paymentRepository.findPaymentDetailById(paymentId))
                .thenReturn(Optional.of(payment));

            // when
            PaymentDetailResponseDto result = paymentService.getPaymentHistoryDetail(paymentId);

            // then
            int orderTotal = result.orderItem().stream()
                .mapToInt(OrderItemInfoDto::totalPrice)
                .sum();

            assertThat(result.price()).isEqualTo(orderTotal);
        }

        @Test
        @DisplayName("COMPLETED 상태 변경 시 approvedAt 자동 설정")
        void verifyApprovedAtSetWhenCompleted() {
            // given
            UUID paymentId = (UUID) ReflectionTestUtils.getField(payment, "id");

            Payment pendingPayment = Payment.builder()
                .number("TEST-PAY-1")
                .amount(10000L)
                .cardCompany(SHINHAN)
                .cardNumber("1234567890123456")
                .status(PENDING)
                .user(customer)
                .build();
            ReflectionTestUtils.setField(pendingPayment, "id", paymentId);

            UpdatePaymentStatusRequest request = new UpdatePaymentStatusRequest(COMPLETED);

            when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(pendingPayment));

            // when
            paymentService.changePaymentStatus(paymentId, request);

            // then
            assertThat(pendingPayment.getProcessedAt()).isNotNull();
        }
    }
}
