package com.marketsystem.api.v1.customer.service;

import com.marketsystem.api.v1.customer.dto.CustomerResponseDto;
import com.marketsystem.api.v1.customer.entity.Customer;
import com.marketsystem.api.v1.customer.mapper.CustomerMapper;
import com.marketsystem.api.v1.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Transactional
    public Customer createCustomer(String name) {
        Customer customer = new Customer();
        return customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public CustomerResponseDto getCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid customer ID: " + id));
        return customerMapper.toDto(customer);
    }

    @Transactional
    public void updateCustomer(Long id, String newName) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid customer ID: " + id));
        customerRepository.save(customer);
    }

    @Transactional
    public void deleteCustomer(Long id, String name) {
        Customer customer = customerRepository.findByIdAndName(id, name)
                .orElseThrow(() -> new IllegalArgumentException("Invalid customer ID: " + id));
        customerRepository.delete(customer);
    }
}