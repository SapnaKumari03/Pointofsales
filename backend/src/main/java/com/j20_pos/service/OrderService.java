package com.j20_pos.service;

import com.j20_pos.dto.CheckoutRequestDTO;
import com.j20_pos.dto.OrderResponseDTO;

import java.time.LocalDate;
import java.util.List;

public interface OrderService {

    List<OrderResponseDTO> getAllOrders();

    List<OrderResponseDTO> searchOrders(String query, String paymentMethod, LocalDate startDate, LocalDate endDate);

    OrderResponseDTO getOrderById(Long id);

    OrderResponseDTO getOrderByInvoiceNumber(String invoiceNumber);

    OrderResponseDTO processCheckout(CheckoutRequestDTO request);
}
