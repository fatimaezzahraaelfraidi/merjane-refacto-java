package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.enums.ProductType;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.ProductService;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@UnitTest
public class ProductServiceTests {

    @Mock
    private NotificationService notificationService;
    @Mock
    private ProductRepository productRepository;
    private ProductService productService;

    @BeforeEach
    public void setUp() {
        productService = new DefaultProductService(productRepository, notificationService);
    }

    @Test
    public void should_decrementStock_when_normalProductIsInStock() {
        Product product = normalProduct(10, 5);
        when(productRepository.save(product)).thenReturn(product);

        productService.processProduct(product);

        assertEquals(4, product.getAvailableQuantity());
        verify(productRepository).save(product);
    }

    @Test
    public void should_sendDelayNotification_when_normalProductIsOutOfStock() {
        Product product = normalProduct(15, 0);
        when(productRepository.save(product)).thenReturn(product);

        productService.processProduct(product);

        verify(notificationService).sendDelayNotification(15, product.getName());
    }

    @Test
    public void should_decrementStock_when_expirableProductIsInStockAndNotExpired() {
        Product product = expirableProduct(LocalDate.now().plusDays(10));
        when(productRepository.save(product)).thenReturn(product);

        productService.processProduct(product);

        assertEquals(4, product.getAvailableQuantity());
    }

    @Test
    public void should_sendExpirationNotification_when_expirableProductIsExpired() {
        Product product = expirableProduct(LocalDate.now().minusDays(1));
        when(productRepository.save(product)).thenReturn(product);

        productService.processProduct(product);

        verify(notificationService)
                .sendExpirationNotification(product.getName(), product.getExpiryDate());
        assertEquals(0, product.getAvailableQuantity());
    }

    @Test
    public void should_decrementStock_when_seasonalProductIsInSeasonAndInStock() {
        Product product = seasonalProduct(
                5,
                LocalDate.now().minusDays(5),
                LocalDate.now().plusDays(60)
        );
        when(productRepository.save(product)).thenReturn(product);

        productService.processProduct(product);

        assertEquals(4, product.getAvailableQuantity());
    }

    @Test
    public void should_sendOutOfStockNotification_when_seasonalProductIsNotInSeason() {
        Product product = seasonalProduct(
                5,
                LocalDate.now().plusDays(30),
                LocalDate.now().plusDays(90)
        );
        when(productRepository.save(product)).thenReturn(product);

        productService.processProduct(product);

        verify(notificationService).sendOutOfStockNotification(product.getName());
        assertEquals(0, product.getAvailableQuantity());
    }

    @Test
    public void should_sendOutOfStock_when_seasonalProductLeadTimeExceedsSeason() {
        Product product = seasonalProduct(
                100,
                LocalDate.now().minusDays(5),
                LocalDate.now().plusDays(10)
        );
        when(productRepository.save(product)).thenReturn(product);

        productService.processProduct(product);

        verify(notificationService).sendOutOfStockNotification(product.getName());
        assertEquals(0, product.getAvailableQuantity());
    }

    private Product normalProduct(int leadTime, int available) {
        return new Product(null, leadTime, available, ProductType.NORMAL, "USB Cable", null, null, null);
    }

    private Product expirableProduct(LocalDate expiryDate) {
        return new Product(null, 10, 5, ProductType.EXPIRABLE, "Charger", expiryDate, null, null);
    }

    private Product seasonalProduct(int leadTime, LocalDate seasonStart, LocalDate seasonEnd) {
        return new Product(null, leadTime, 5, ProductType.SEASONAL, "HDMI Cable", null, seasonStart, seasonEnd);
    }
}
