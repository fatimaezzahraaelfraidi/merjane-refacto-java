package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.enums.ProductType;
import com.nimbleways.springboilerplate.exceptions.OrderNotFoundException;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.services.OrderService;
import com.nimbleways.springboilerplate.services.ProductService;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@UnitTest
public class OrderServiceTests {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductService productService;

    private OrderService orderService;

    @BeforeEach
    public void setUp() {
        orderService = new DefaultOrderService(orderRepository, productService);
    }

    @Test
    public void should_processAllProducts_when_orderExists() {
        Product p1 = normalProduct(15, 5);
        Product p2 = normalProduct(10, 0);
        Order order = orderWithItems(1L, p1, p2);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.processOrder(1L);

        verify(productService).processProduct(p1);
        verify(productService).processProduct(p2);
    }

    @Test
    public void should_throwOrderNotFoundException_when_orderDoesNotExist() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.processOrder(99L));
    }

    @Test
    public void should_notProcessAnyProduct_when_orderHasNoItems() {
        Order order = orderWithItems(1L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.processOrder(1L);

        verifyNoInteractions(productService);
    }

    private Order orderWithItems(Long id, Product... products) {
        Order order = new Order();
        order.setId(id);
        order.setItems(new HashSet<>(Arrays.asList(products)));
        return order;
    }

    private Product normalProduct(int leadTime, int availableQuantity) {
        return new Product(null, leadTime, availableQuantity, ProductType.NORMAL, "USB Cable", null, null, null);
    }
}