package com.sparta.tdd.domain.order.repository;

import com.sparta.tdd.common.template.RepositoryTest;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;

class OrderRepositoryTest extends RepositoryTest {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private EntityManager em;


}