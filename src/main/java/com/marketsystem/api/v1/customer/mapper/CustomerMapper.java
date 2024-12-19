package com.marketsystem.api.v1.customer.mapper;

import com.marketsystem.api.v1.customer.dto.CustomerResponseDto;
import com.marketsystem.api.v1.customer.entity.Customer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    CustomerResponseDto toDto(Customer customer);
}
