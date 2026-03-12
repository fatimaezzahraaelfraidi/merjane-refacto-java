package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRED)
public class DefaultProductService implements ProductService {

    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    public DefaultProductService(ProductRepository productRepository, NotificationService notificationService) {
        this.productRepository = productRepository;
        this.notificationService = notificationService;
    }

    @Override
    public void processProduct(Product product) {
        if (product == null) {
            log.warn("Attempted to process a null product.");
            return;
        }
        if (product.getType() == null) {
            log.warn("Product id={} has no type defined.", product.getId());
            return;
        }

        switch (product.getType()) {
            case NORMAL -> processNormalProduct(product);
            case SEASONAL -> processSeasonalProduct(product);
            case EXPIRABLE -> processExpirableProduct(product);
            default -> log.error("Unknown product type: {}", product.getType());
        }
    }

    @Override
    public void notifyDelay(int leadTime, Product product) {
        product.setLeadTime(leadTime);
        productRepository.save(product);
        notificationService.sendDelayNotification(leadTime, product.getName());
    }

    private void processNormalProduct(Product product) {
        if (product.isInStock()) {
            product.decrementStock();
            productRepository.save(product);
        } else if (product.getLeadTime() > 0) {
            notifyDelay(product.getLeadTime(), product);
        }
    }

    private void processSeasonalProduct(Product product) {
        if (!product.isInSeason() || product.leadTimeExceedsSeason()) {
            notificationService.sendOutOfStockNotification(product.getName());
            markAsUnavailable(product);
        } else if (product.isInStock()) {
            product.decrementStock();
            productRepository.save(product);
        } else {
            notifyDelay(product.getLeadTime(), product);
        }
    }

    private void markAsUnavailable(Product product) {
        product.setAvailableQuantity(0);
        productRepository.save(product);
    }

    private void processExpirableProduct(Product product) {
        if (product.isInStock() && !product.isExpired()) {
            product.decrementStock();
            productRepository.save(product);
        } else {
            notificationService.sendExpirationNotification(
                    product.getName(),
                    product.getExpiryDate()
            );
            product.setAvailableQuantity(0);
            productRepository.save(product);
        }
    }
}