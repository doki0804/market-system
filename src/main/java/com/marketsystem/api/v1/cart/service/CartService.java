package com.marketsystem.api.v1.cart.service;

import com.marketsystem.api.v1.cart.dto.CartItemDto;
import com.marketsystem.api.v1.cart.dto.CartRequestDto;
import com.marketsystem.api.v1.cart.entity.Cart;
import com.marketsystem.api.v1.cart.entity.CartId;
import com.marketsystem.api.v1.cart.repository.CartRepository;
import com.marketsystem.api.v1.common.exception.BusinessException;
import com.marketsystem.api.v1.common.enums.ErrorCode;
import com.marketsystem.api.v1.customer.entity.Customer;
import com.marketsystem.api.v1.customer.repository.CustomerRepository;
import com.marketsystem.api.v1.product.dto.ProductDto;
import com.marketsystem.api.v1.product.entity.Product;
import com.marketsystem.api.v1.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    /**
     * Cart에 상품을 등록하거나 수량 추가, 제거, 삭제하는 메서드
     * @param cartRequestDto
     */
    @Transactional
    public void addToCart(CartRequestDto cartRequestDto) {
        // 고객 조회
        Customer customer = customerRepository.findById(cartRequestDto.getCustomerId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));

        // 요청된 모든 제품 ID 수집
        Set<Long> productIds = cartRequestDto.getProductList().stream()
                .map(CartRequestDto.Product::getProductId)
                .collect(Collectors.toSet());

        // 모든 제품을 한 번에 조회
        List<Product> products = productRepository.findAllByIdIn(productIds);

        // 제품 존재 여부 확인
        if (products.size() != productIds.size()) {
            List<Long> foundIds = products.stream()
                    .map(Product::getId)
                    .toList();
            List<Long> notFoundIds = productIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Product IDs not found: " + notFoundIds);
        }

        // 제품 ID에 따른 매핑
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        // 장바구니 ID 수집
        List<CartId> cartIds = cartRequestDto.getProductList().stream()
                .map(p -> new CartId(customer.getId(), p.getProductId()))
                .toList();

        // 기존 장바구니 항목 한 번에 조회
        List<Cart> existingCarts = cartRepository.findAllById(cartIds);
        Map<CartId, Cart> existingCartMap = existingCarts.stream()
                .collect(Collectors.toMap(Cart::getId, Function.identity()));

        // 업데이트할 장바구니 리스트 초기화
        List<Cart> cartsToSave = new ArrayList<>();
        List<Cart> cartsToDelete = new ArrayList<>();

        for (var p : cartRequestDto.getProductList()) {
            var product = productMap.get(p.getProductId());
            var cartId = new CartId(customer.getId(), product.getId());

            // 기존 장바구니 수량
            var existingCart = existingCartMap.get(cartId);
            int existingQuantity = existingCart != null ? existingCart.getQuantity() : 0;

            // 새로운 수량 합산
            int newQuantity = existingQuantity + p.getQuantity();

            // 재고 검사
            if (product.getStock() < newQuantity) {
                throw new BusinessException(ErrorCode.OUT_OF_STOCK,
                        "Requested quantity: " + p.getQuantity() + ", Available: " + product.getStock());
            }

            if (existingCart != null) {
                // 기존 장바구니 업데이트
                updateQuantity(existingCart, p.getQuantity());
                if (existingCart.getQuantity() <= 0) {
                    cartsToDelete.add(existingCart);
                } else {
                    cartsToSave.add(existingCart);
                }
            } else {
                // 새로운 장바구니 생성
                var cart = new Cart(cartId, customer, product, p.getQuantity());
                cartsToSave.add(cart);
            }
        }

        // 삭제할 장바구니 항목을 한 번에 삭제
        if (!cartsToDelete.isEmpty()) {
            cartRepository.deleteAll(cartsToDelete);
        }

        // 저장할 장바구니 항목을 한 번에 저장
        if (!cartsToSave.isEmpty()) {
            try {
                cartRepository.saveAll(cartsToSave);
            } catch (OptimisticLockingFailureException e) {
                throw new BusinessException(ErrorCode.CONFLICT, "Cart update conflict. Please try again.");
            }
        }
    }

    protected void updateQuantity(Cart cart, int quantity) {
        if(cart.getQuantity() + quantity <= 0) {
             deleteCart(cart);
        }else {
            cart.addQuantity(quantity);
        }
    }

    protected void deleteCart(Cart cart) {
        cartRepository.delete(cart);
    }

    /**
     * customerId를 받아 해당 고객의 장바구니 항목을 반환하는 메서드 예시.
     */
    @Transactional(readOnly = true)
    public CartItemDto getCartItems(Long customerId) {
        customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));
        List<Cart> cartItems = getCartItemsEntity(customerId);
        List<ProductDto> productDtoList = cartItems.stream()
                .map(c -> ProductDto.builder()
                        .id(c.getProduct().getId())
                        .name(c.getProduct().getName())
                        .price(c.getProduct().getPrice())
                        .stock(c.getProduct().getStock())
                        .build())
                .collect(Collectors.toList());

        return CartItemDto.builder()
                .customerId(customerId)
                .productList(productDtoList)
                .build();
    }

    @Transactional(readOnly = true)
    public List<Cart> getCartItemsEntity(Long customerId) {
        return cartRepository.findByCustomerIdWithProduct(customerId);
    }

    /**
     * 장바구니를 비우는 메서드
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void clearCart(Long customerId) {
        try {
            List<Cart> cartItems = cartRepository.findByCustomerId(customerId);
            cartRepository.deleteAll(cartItems);
        } catch (Exception e) {
            // 예외를 다시 던지지 않고, 로그로만 기록
            logger.error("Failed to clear cart for Customer ID: {}. Error: {}", customerId, e.getMessage());
        }
    }

}
