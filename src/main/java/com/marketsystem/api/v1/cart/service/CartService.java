package com.marketsystem.api.v1.cart.service;

import com.marketsystem.api.v1.cart.dto.CartItemDto;
import com.marketsystem.api.v1.cart.entity.Cart;
import com.marketsystem.api.v1.cart.entity.CartId;
import com.marketsystem.api.v1.cart.repository.CartRepository;
import com.marketsystem.api.v1.common.exception.BusinessException;
import com.marketsystem.api.v1.common.exception.ErrorCode;
import com.marketsystem.api.v1.customer.entity.Customer;
import com.marketsystem.api.v1.customer.repository.CustomerRepository;
import com.marketsystem.api.v1.product.entity.Product;
import com.marketsystem.api.v1.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    @Transactional
    public void addToCart(Long customerId, Long productId, int quantity) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        CartId cartId = new CartId(customer.getId(), product.getId());
        Optional<Cart> optionalCart = cartRepository.findById(cartId);

        if(product.getStock() < quantity) {
            throw new BusinessException(ErrorCode.OUT_OF_STOCK, "Requested quantity: " + quantity + ", Available: " + product.getStock());
        }

        if(optionalCart.isPresent()) {
            Cart cart = optionalCart.get();
            updateQuantity(cart, quantity);
        } else {
            Cart cart = Cart.builder()
                    .id(cartId)
                    .customer(customer)
                    .product(product)
                    .quantity(quantity)
                    .build();
            cartRepository.save(cart);
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
    public List<CartItemDto> getCartItems(Long customerId) {
        List<Cart> cartItems = getCartItemsEntity(customerId);

        return cartItems.stream()
                .map(c -> CartItemDto.builder()
                        .customerId(c.getCustomer().getId())
                        .customerName(c.getCustomer().getName())
                        .productId(c.getProduct().getId())
                        .productName(c.getProduct().getName())
                        .productQuantity(c.getQuantity())
                        .productStock(c.getProduct().getStock())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Cart> getCartItemsEntity(Long customerId) {
        return cartRepository.findByCustomerIdWithProduct(customerId);
    }

    /**
     * 장바구니를 비우는 메서드
     */
    @Transactional
    public void clearCart(Long customerId) {
        List<Cart> cartItems = cartRepository.findByCustomerId(customerId);
        cartRepository.deleteAll(cartItems);
    }

}
