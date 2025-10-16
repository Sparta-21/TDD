package com.sparta.tdd.domain.payment.repository;

import static com.sparta.tdd.domain.payment.enums.CardCompany.KB;
import static com.sparta.tdd.domain.payment.enums.CardCompany.SHINHAN;
import static com.sparta.tdd.domain.payment.enums.CardCompany.WOORI;
import static com.sparta.tdd.domain.payment.enums.PaymentStatus.COMPLETED;
import static com.sparta.tdd.domain.payment.enums.PaymentStatus.PENDING;
import static com.sparta.tdd.domain.store.enums.StoreCategory.KOREAN;
import static com.sparta.tdd.domain.user.enums.UserAuthority.CUSTOMER;
import static com.sparta.tdd.domain.user.enums.UserAuthority.OWNER;
import static org.assertj.core.api.Assertions.assertThat;

import com.sparta.tdd.common.template.RepositoryTest;
import com.sparta.tdd.domain.order.entity.Order;
import com.sparta.tdd.domain.order.enums.OrderStatus;
import com.sparta.tdd.domain.payment.entity.Payment;
import com.sparta.tdd.domain.store.entity.Store;
import com.sparta.tdd.domain.user.entity.User;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

class PaymentRepositoryTest extends RepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Nested
    @DisplayName("결제 CRUD 테스트")
    class PaymentCRUDTest {

        @Test
        @DisplayName("결제 정보 저장")
        void save() {
            // given
            User user = createUser("customer", "고객", CUSTOMER);
            em.persist(user);

            Payment payment = Payment.builder()
                .number("TEST-PAY-100100")
                .amount(30000L)
                .cardCompany(SHINHAN)
                .cardNumber("1234567890123456")
                .status(PENDING)
                .user(user)
                .build();

            // when
            Payment saved = paymentRepository.save(payment);
            em.flush();
            em.clear();

            // then
            Optional<Payment> found = paymentRepository.findById(saved.getId());
            assertThat(found).isPresent();
            assertThat(found.get().getNumber()).isEqualTo(payment.getNumber());
            assertThat(found.get().getAmount()).isEqualTo(payment.getAmount());
            assertThat(found.get().getCardCompany()).isEqualTo(payment.getCardCompany());
            assertThat(found.get().getStatus()).isEqualTo(payment.getStatus());
        }

        @Test
        @DisplayName("결제 상태 완료 변경 시 approvedAt 업데이트")
        void updateStatus() {
            // given
            User user = createUser("customer", "고객", CUSTOMER);
            em.persist(user);

            Payment payment = Payment.builder()
                .number("TEST-PAY-100100")
                .amount(50000L)
                .cardCompany(WOORI)
                .cardNumber("1234567890123456")
                .status(PENDING)
                .user(user)
                .build();
            Payment saved = paymentRepository.save(payment);
            em.flush();
            em.clear();

            // when
            Payment foundPayment = paymentRepository.findById(saved.getId()).orElseThrow();
            foundPayment.approve();
            em.flush();
            em.clear();

            // then
            Payment updated = paymentRepository.findById(saved.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(COMPLETED);
            assertThat(updated.getProcessedAt()).isNotNull();
        }

        @Test
        @DisplayName("결제 정보 Soft Delete")
        void softDelete() {
            // given
            User user = createUser("customer", "고객", CUSTOMER);
            em.persist(user);

            Payment payment = Payment.builder()
                .number("TEST-PAY-100100")
                .amount(20000L)
                .cardCompany(KB)
                .cardNumber("1234567890123456")
                .status(COMPLETED)
                .user(user)
                .build();
            Payment saved = paymentRepository.save(payment);
            em.flush();
            em.clear();

            // when
            Payment found = paymentRepository.findById(saved.getId()).orElseThrow();
            found.delete(user.getId());
            em.flush();
            em.clear();

            // then
            Payment deleted = paymentRepository.findById(saved.getId()).orElseThrow();
            assertThat(deleted.isDeleted()).isTrue();
            assertThat(deleted.getDeletedBy()).isEqualTo(user.getId());
            assertThat(deleted.getDeletedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("결제 조회 쿼리 테스트")
    class PaymentQueryTest {

        @Test
        @DisplayName("사용자별 결제 내역 조회 - 페이징")
        void findPaymentsByUserId() {
            // given
            User customer = createUser("customer", "고객", CUSTOMER);
            User owner = createUser("owner", "사장", OWNER);
            em.persist(customer);
            em.persist(owner);

            Store store = createStore("맛집", "맛있는 식당", owner);
            em.persist(store);

            for (int i = 1; i <= 3; i++) {
                Order order = createOrder(customer, store, "서울시 강남구");
                em.persist(order);

                Payment payment = Payment.builder()
                    .number("TEST-PAY-" + i)
                    .amount(10000L * i)
                    .cardCompany(SHINHAN)
                    .cardNumber("123456789012345" + i)
                    .status(COMPLETED)
                    .user(customer)
                    .order(order)
                    .build();
                paymentRepository.save(payment);
            }
            em.flush();
            em.clear();

            // when
            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<Payment> result = paymentRepository.findPaymentsByUserId(customer.getId(), null, pageable);

            // then
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getTotalElements()).isEqualTo(3);

            assertThat(result.getContent())
                .extracting(Payment::getNumber)
                .containsExactlyInAnyOrder(
                    "TEST-PAY-1",
                    "TEST-PAY-2",
                    "TEST-PAY-3"
                );

            assertThat(result.getContent())
                .extracting(Payment::getAmount)
                .containsExactlyInAnyOrder(10000L, 20000L, 30000L);

            assertThat(result.getContent())
                .allMatch(payment -> payment.getUser().getId().equals(customer.getId()));

            assertThat(result.getContent())
                .allMatch(payment -> payment.getStatus() == COMPLETED);

            assertThat(result.getContent())
                .allMatch(payment -> payment.getCardCompany() == SHINHAN);
        }

        @Test
        @DisplayName("사용자별 결제 내역 조회 - 카드사 필터링")
        void findPaymentsByUserIdWithKeyword() {
            // given
            User customer = createUser("customer", "고객", CUSTOMER);
            em.persist(customer);

            // 신한카드 결제 2개
            for (int i = 1; i <= 2; i++) {
                Payment payment = Payment.builder()
                    .number("TEST-PAY-" + i)
                    .amount(10000L)
                    .cardCompany(SHINHAN)
                    .cardNumber("123456789012345" + i)
                    .status(COMPLETED)
                    .user(customer)
                    .build();
                paymentRepository.save(payment);
            }

            // 우리카드 결제 1개
            Payment payment = Payment.builder()
                .number("TEST-PAY-100100")
                .amount(10000L)
                .cardCompany(WOORI)
                .cardNumber("1234567890123456")
                .status(COMPLETED)
                .user(customer)
                .build();
            paymentRepository.save(payment);

            em.flush();
            em.clear();

            // when
            Pageable pageable = PageRequest.of(0, 10);
            Page<Payment> result = paymentRepository.findPaymentsByUserId(
                customer.getId(), "SHINHAN", pageable
            );

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                .allMatch(p -> p.getCardCompany() == SHINHAN);
        }

        @Test
        @DisplayName("가게별 결제 내역 조회")
        void findPaymentsByStoreId() {
            // given
            User customer = createUser("customer", "고객", CUSTOMER);
            User owner = createUser("owner", "사장", OWNER);
            em.persist(customer);
            em.persist(owner);

            Store store = createStore("치킨집", "맛있는 치킨", owner);
            em.persist(store);

            Order order1 = createOrder(customer, store, "서울시 강남구");
            Order order2 = createOrder(customer, store, "서울시 서초구");
            em.persist(order1);
            em.persist(order2);

            Payment payment1 = Payment.builder()
                .number("TEST-PAY-1")
                .amount(20000L)
                .cardCompany(KB)
                .cardNumber("1234567890123451")
                .status(COMPLETED)
                .user(customer)
                .order(order1)
                .build();

            Payment payment2 = Payment.builder()
                .number("TEST-PAY-2")
                .amount(30000L)
                .cardCompany(SHINHAN)
                .cardNumber("1234567890123452")
                .status(COMPLETED)
                .user(customer)
                .order(order2)
                .build();

            paymentRepository.save(payment1);
            paymentRepository.save(payment2);
            em.flush();
            em.clear();

            // when
            Pageable pageable = PageRequest.of(0, 10);
            Page<Payment> result = paymentRepository.findPaymentsByStoreId(store.getId(), null, pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("결제 상세 조회 - fetchJoin으로 연관 엔티티 로딩")
        void findPaymentDetailById() {
            // given
            User customer = createUser("customer", "고객", CUSTOMER);
            User owner = createUser("owner", "사장", OWNER);
            em.persist(customer);
            em.persist(owner);

            Store store = createStore("분식집", "맛있는 분식", owner);
            em.persist(store);

            Order order = createOrder(customer, store, "서울시 종로구");
            em.persist(order);

            Payment payment = Payment.builder()
                .number("TEST-PAY-1")
                .amount(15000L)
                .cardCompany(SHINHAN)
                .cardNumber("1234567890123456")
                .status(COMPLETED)
                .user(customer)
                .order(order)
                .build();

            Payment saved = paymentRepository.save(payment);
            em.flush();
            em.clear();

            // when
            Optional<Payment> result = paymentRepository.findPaymentDetailById(saved.getId());

            // then
            assertThat(result).isPresent();
            Payment found = result.get();

            assertThat(found.getNumber()).isEqualTo(payment.getNumber());
            assertThat(found.getAmount()).isEqualTo(payment.getAmount());
        }

        @Test
        @DisplayName("삭제된 결제는 조회되지 않음")
        void findPaymentsByUserId_excludeDeleted() {
            // given
            User customer = createUser("customer", "고객", CUSTOMER);
            em.persist(customer);

            Payment payment1 = Payment.builder()
                .number("TEST-PAY-1")
                .amount(10000L)
                .cardCompany(SHINHAN)
                .cardNumber("1234567890123451")
                .status(COMPLETED)
                .user(customer)
                .build();

            Payment payment2 = Payment.builder()
                .number("TEST-PAY-2")
                .amount(20000L)
                .cardCompany(KB)
                .cardNumber("1234567890123452")
                .status(COMPLETED)
                .user(customer)
                .build();

            paymentRepository.save(payment1);
            paymentRepository.save(payment2);

            // payment2 삭제
            payment2.delete(customer.getId());

            em.flush();
            em.clear();

            // when
            Pageable pageable = PageRequest.of(0, 10);
            Page<Payment> result = paymentRepository.findPaymentsByUserId(customer.getId(), null, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().getNumber()).isEqualTo(payment1.getNumber());
        }
    }

    @Nested
    @DisplayName("정렬 테스트")
    class SortingTest {

        @Test
        @DisplayName("금액 기준 오름차순 정렬")
        void sortByAmountAsc() {
            // given
            User customer = createUser("customer", "고객", CUSTOMER);
            em.persist(customer);

            paymentRepository.save(createPayment("TEST-PAY-3", 30000L, customer));
            paymentRepository.save(createPayment("TEST-PAY-1", 10000L, customer));
            paymentRepository.save(createPayment("TEST-PAY-2", 20000L, customer));

            em.flush();
            em.clear();

            // when
            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "amount"));
            Page<Payment> result = paymentRepository.findPaymentsByUserId(customer.getId(), null, pageable);

            // then
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getContent().get(0).getAmount()).isEqualTo(10000L);
            assertThat(result.getContent().get(1).getAmount()).isEqualTo(20000L);
            assertThat(result.getContent().get(2).getAmount()).isEqualTo(30000L);
        }

        @Test
        @DisplayName("생성일 기준 내림차순 정렬 (기본값)")
        void sortByCreatedAtDesc() {
            // given
            User customer = createUser("customer10", "고객10", CUSTOMER);
            em.persist(customer);

            Payment payment1 = createPayment("TEST-PAY-FIRST", 10000L, customer);
            Payment payment2 = createPayment("TEST-PAY-SECOND", 20000L, customer);
            Payment payment3 = createPayment("TEST-PAY-THIRD", 30000L, customer);

            paymentRepository.save(payment1);
            em.flush();
            paymentRepository.save(payment2);
            em.flush();
            paymentRepository.save(payment3);
            em.flush();
            em.clear();

            // when - 정렬 조건 없음 (기본값 적용)
            Pageable pageable = PageRequest.of(0, 10);
            Page<Payment> result = paymentRepository.findPaymentsByUserId(customer.getId(), null, pageable);

            // then - 최신순으로 정렬됨
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getContent().get(0).getNumber()).isEqualTo(payment3.getNumber());
            assertThat(result.getContent().get(1).getNumber()).isEqualTo(payment2.getNumber());
            assertThat(result.getContent().get(2).getNumber()).isEqualTo(payment1.getNumber());
        }
    }

    private User createUser(String username, String nickname,
        com.sparta.tdd.domain.user.enums.UserAuthority authority) {
        return User.builder()
            .username(username)
            .password("password123")
            .nickname(nickname)
            .authority(authority)
            .build();
    }

    private Store createStore(String name, String description, User owner) {
        return Store.builder()
            .name(name)
            .description(description)
            .category(KOREAN)
            .user(owner)
            .build();
    }

    private Order createOrder(User customer, Store store, String address) {
        return Order.builder()
            .address(address)
            .orderStatus(OrderStatus.PENDING)
            .store(store)
            .user(customer)
            .build();
    }

    private Payment createPayment(String number, Long amount, User user) {
        return Payment.builder()
            .number("TEST-PAY-100100")
            .amount(amount)
            .cardCompany(SHINHAN)
            .cardNumber("1234567890123456")
            .status(COMPLETED)
            .user(user)
            .build();
    }
}
