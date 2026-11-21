package com.j20_pos.service;

import com.j20_pos.dto.*;
import com.j20_pos.entity.CashShift;
import com.j20_pos.entity.CashTransaction;
import com.j20_pos.entity.Order;
import com.j20_pos.entity.Payment;
import com.j20_pos.exception.BadRequestException;
import com.j20_pos.repository.CashShiftRepository;
import com.j20_pos.repository.CashTransactionRepository;
import com.j20_pos.repository.OrderRepository;
import com.j20_pos.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CashServiceImpl implements CashService {

    private final CashShiftRepository cashShiftRepository;
    private final CashTransactionRepository cashTransactionRepository;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");

    @Override
    @Transactional
    public CashShiftDTO openShift(OpenShiftDTO dto) {
        Optional<CashShift> existingActive = cashShiftRepository.findTopByStatusInOrderByStartTimeDesc(List.of("OPEN", "ACTIVE"));
        if (existingActive.isPresent()) {
            throw new BadRequestException("An active cash shift (" + existingActive.get().getShiftNumber()
                    + ") is already open. Please close it before opening a new shift.");
        }

        BigDecimal openingFloat = dto.getOpeningFloat() != null
                ? dto.getOpeningFloat().setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        String shiftNumber = generateUniqueShiftNumber();
        String cashierName = (dto.getCashierName() != null && !dto.getCashierName().isBlank())
                ? dto.getCashierName().trim() : "Store Cashier";
        String cashierId = (dto.getCashierId() != null && !dto.getCashierId().isBlank())
                ? dto.getCashierId().trim() : "CASHIER-1";
        String terminalId = (dto.getTerminalId() != null && !dto.getTerminalId().isBlank())
                ? dto.getTerminalId().trim() : "POS-01";

        CashShift shift = CashShift.builder()
                .shiftNumber(shiftNumber)
                .cashierId(cashierId)
                .cashierName(cashierName)
                .terminalId(terminalId)
                .startTime(LocalDateTime.now())
                .status("OPEN")
                .openingFloat(openingFloat)
                .cashSales(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .cashPaidOut(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .expectedCash(openingFloat)
                .closureNotes(dto.getNotes() != null ? dto.getNotes().trim() : "Shift Opening")
                .transactions(new ArrayList<>())
                .build();

        CashTransaction floatTx = CashTransaction.builder()
                .shift(shift)
                .type("FLOAT")
                .amount(openingFloat)
                .referenceNumber(shiftNumber)
                .reason("Opening Register Float")
                .performedBy(cashierName)
                .timestamp(LocalDateTime.now())
                .build();

        shift.addTransaction(floatTx);
        CashShift savedShift = cashShiftRepository.save(shift);
        return mapToDTO(savedShift);
    }

    @Override
    @Transactional
    public CashShiftDTO closeShift(CloseShiftDTO dto) {
        CashShift active = cashShiftRepository.findTopByStatusInOrderByStartTimeDesc(List.of("OPEN", "ACTIVE"))
                .orElseThrow(() -> new BadRequestException("No active cash shift found to close."));

        BigDecimal openingFloat = active.getOpeningFloat() != null ? active.getOpeningFloat() : BigDecimal.ZERO;
        BigDecimal cashSales = active.getCashSales() != null ? active.getCashSales() : BigDecimal.ZERO;
        BigDecimal cashPaidOut = active.getCashPaidOut() != null ? active.getCashPaidOut() : BigDecimal.ZERO;

        BigDecimal expectedCash = openingFloat.add(cashSales).subtract(cashPaidOut).setScale(2, RoundingMode.HALF_UP);
        BigDecimal actualCash = dto.getActualCash().setScale(2, RoundingMode.HALF_UP);
        BigDecimal variance = actualCash.subtract(expectedCash).setScale(2, RoundingMode.HALF_UP);

        active.setEndTime(LocalDateTime.now());
        active.setExpectedCash(expectedCash);
        active.setActualCash(actualCash);
        active.setVariance(variance);
        active.setStatus("CLOSED");

        if (dto.getClosureNotes() != null && !dto.getClosureNotes().isBlank()) {
            active.setClosureNotes(dto.getClosureNotes().trim());
        }
        if (dto.getCashCountDetails() != null && !dto.getCashCountDetails().isBlank()) {
            active.setCashCountDetails(dto.getCashCountDetails().trim());
        }

        CashShift saved = cashShiftRepository.save(active);
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CashShiftDTO getActiveShift() {
        Optional<CashShift> active = cashShiftRepository.findTopByStatusInOrderByStartTimeDesc(List.of("OPEN", "ACTIVE"));
        return active.map(this::mapToDTO).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CashShiftDTO> getShiftHistory() {
        return cashShiftRepository.findAllByOrderByStartTimeDesc().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CashShiftDTO getShiftById(Long id) {
        CashShift shift = cashShiftRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Cash shift not found with ID: " + id));
        return mapToDTO(shift);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CashTransactionDTO> getShiftTransactions(Long shiftId) {
        return cashTransactionRepository.findByShiftIdOrderByTimestampDesc(shiftId).stream()
                .map(this::mapTransactionToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CashTransactionDTO> getAllTransactions() {
        return cashTransactionRepository.findAllByOrderByTimestampDesc().stream()
                .map(this::mapTransactionToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CashTransactionDTO recordCashPayout(CashPayoutDTO dto) {
        CashShift active = cashShiftRepository.findTopByStatusInOrderByStartTimeDesc(List.of("OPEN", "ACTIVE"))
                .orElseThrow(() -> new BadRequestException("Cannot process cash payout: No active cash shift is open."));

        BigDecimal amount = dto.getAmount().setScale(2, RoundingMode.HALF_UP);
        BigDecimal currentExpected = active.getExpectedCash() != null ? active.getExpectedCash() : BigDecimal.ZERO;

        if (currentExpected.compareTo(amount) < 0) {
            throw new BadRequestException("Insufficient cash in drawer for payout. Expected cash in register is ₹"
                    + currentExpected + ", requested payout is ₹" + amount);
        }

        BigDecimal newPaidOut = (active.getCashPaidOut() != null ? active.getCashPaidOut() : BigDecimal.ZERO).add(amount);
        BigDecimal newExpected = (active.getOpeningFloat() != null ? active.getOpeningFloat() : BigDecimal.ZERO)
                .add(active.getCashSales() != null ? active.getCashSales() : BigDecimal.ZERO)
                .subtract(newPaidOut);

        active.setCashPaidOut(newPaidOut);
        active.setExpectedCash(newExpected);

        String performedBy = dto.getPaidTo().trim();
        if (dto.getApprovedBy() != null && !dto.getApprovedBy().isBlank()) {
            performedBy += " (Approved: " + dto.getApprovedBy().trim() + ")";
        }

        String referenceNumber = "PAYOUT-" + System.currentTimeMillis() % 100000;

        CashTransaction tx = CashTransaction.builder()
                .shift(active)
                .type("PAYOUT")
                .amount(amount)
                .referenceNumber(referenceNumber)
                .reason(dto.getReason().trim())
                .performedBy(performedBy)
                .timestamp(LocalDateTime.now())
                .build();

        active.addTransaction(tx);
        cashShiftRepository.save(active);

        return mapTransactionToDTO(tx);
    }

    @Override
    @Transactional
    public void recordSalePayment(Order order) {
        if (order == null) return;

        String method = order.getPaymentMethod() != null ? order.getPaymentMethod().toUpperCase() : "CASH";
        String gateway;
        if ("CASH".equals(method)) {
            gateway = "CASH_DRAWER";
        } else if ("UPI".equals(method)) {
            gateway = "MOCK_UPI";
        } else {
            gateway = "MOCK_CARD";
        }

        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(method)
                .amount(order.getGrandTotal())
                .amountTendered(order.getAmountPaid() != null ? order.getAmountPaid() : order.getGrandTotal())
                .changeGiven(order.getChangeDue() != null ? order.getChangeDue() : BigDecimal.ZERO)
                .transactionReference(order.getInvoiceNumber())
                .paymentStatus("COMPLETED")
                .paymentGateway(gateway)
                .paymentTime(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);

        // If CASH, record in active CashShift
        if ("CASH".equals(method)) {
            Optional<CashShift> activeOpt = cashShiftRepository.findTopByStatusInOrderByStartTimeDesc(List.of("OPEN", "ACTIVE"));
            if (activeOpt.isPresent()) {
                CashShift active = activeOpt.get();
                BigDecimal currentCashSales = active.getCashSales() != null ? active.getCashSales() : BigDecimal.ZERO;
                BigDecimal currentPaidOut = active.getCashPaidOut() != null ? active.getCashPaidOut() : BigDecimal.ZERO;
                BigDecimal opening = active.getOpeningFloat() != null ? active.getOpeningFloat() : BigDecimal.ZERO;

                BigDecimal newCashSales = currentCashSales.add(order.getGrandTotal());
                BigDecimal newExpected = opening.add(newCashSales).subtract(currentPaidOut);

                active.setCashSales(newCashSales);
                active.setExpectedCash(newExpected);

                CashTransaction saleTx = CashTransaction.builder()
                        .shift(active)
                        .type("SALE")
                        .amount(order.getGrandTotal())
                        .referenceNumber(order.getInvoiceNumber())
                        .reason("POS Cash Sale (" + order.getInvoiceNumber() + ")")
                        .performedBy(order.getCashierName() != null ? order.getCashierName() : active.getCashierName())
                        .timestamp(LocalDateTime.now())
                        .build();

                active.addTransaction(saleTx);
                cashShiftRepository.save(active);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentSummaryDTO getPaymentSummary() {
        List<Order> orders = orderRepository.findAll();

        BigDecimal cashTotal = BigDecimal.ZERO;
        int cashTxCount = 0;
        BigDecimal upiTotal = BigDecimal.ZERO;
        int upiTxCount = 0;
        BigDecimal cardTotal = BigDecimal.ZERO;
        int cardTxCount = 0;

        for (Order o : orders) {
            if ("COMPLETED".equalsIgnoreCase(o.getStatus())) {
                String method = o.getPaymentMethod() != null ? o.getPaymentMethod().toUpperCase() : "CASH";
                BigDecimal grandTotal = o.getGrandTotal() != null ? o.getGrandTotal() : BigDecimal.ZERO;

                if ("CASH".equals(method)) {
                    cashTotal = cashTotal.add(grandTotal);
                    cashTxCount++;
                } else if ("UPI".equals(method)) {
                    upiTotal = upiTotal.add(grandTotal);
                    upiTxCount++;
                } else if ("CARD".equals(method)) {
                    cardTotal = cardTotal.add(grandTotal);
                    cardTxCount++;
                }
            }
        }

        BigDecimal totalSales = cashTotal.add(upiTotal).add(cardTotal).setScale(2, RoundingMode.HALF_UP);
        int totalOrders = cashTxCount + upiTxCount + cardTxCount;

        return PaymentSummaryDTO.builder()
                .totalSales(totalSales)
                .totalOrders(totalOrders)
                .cashTotal(cashTotal.setScale(2, RoundingMode.HALF_UP))
                .cashTxCount(cashTxCount)
                .upiTotal(upiTotal.setScale(2, RoundingMode.HALF_UP))
                .upiTxCount(upiTxCount)
                .cardTotal(cardTotal.setScale(2, RoundingMode.HALF_UP))
                .cardTxCount(cardTxCount)
                .build();
    }

    private String generateUniqueShiftNumber() {
        int year = LocalDate.now().getYear();
        long count = cashShiftRepository.count() + 1;
        String candidate = String.format("SHIFT-%d-%03d", year, count);
        while (cashShiftRepository.findByShiftNumber(candidate).isPresent()) {
            count++;
            candidate = String.format("SHIFT-%d-%03d", year, count);
        }
        return candidate;
    }

    private CashShiftDTO mapToDTO(CashShift s) {
        List<CashTransactionDTO> txDTOs = s.getTransactions() != null ? s.getTransactions().stream()
                .map(this::mapTransactionToDTO)
                .collect(Collectors.toList()) : Collections.emptyList();

        String openedAt = s.getStartTime() != null ? s.getStartTime().format(DISPLAY_FORMATTER) : null;
        String closedAt = s.getEndTime() != null ? s.getEndTime().format(DISPLAY_FORMATTER) : null;

        String varianceType = "BALANCED";
        if (s.getVariance() != null) {
            if (s.getVariance().compareTo(BigDecimal.ZERO) > 0) {
                varianceType = "SURPLUS";
            } else if (s.getVariance().compareTo(BigDecimal.ZERO) < 0) {
                varianceType = "SHORTAGE";
            }
        }

        return CashShiftDTO.builder()
                .id(s.getId())
                .shiftNumber(s.getShiftNumber())
                .shiftCode(s.getShiftNumber())
                .cashierId(s.getCashierId())
                .cashierName(s.getCashierName())
                .terminalId(s.getTerminalId())
                .startTime(s.getStartTime())
                .openedAt(openedAt)
                .endTime(s.getEndTime())
                .closedAt(closedAt)
                .status(s.getStatus())
                .openingFloat(s.getOpeningFloat())
                .cashSales(s.getCashSales())
                .cashPaidOut(s.getCashPaidOut())
                .expectedCash(s.getExpectedCash())
                .actualCash(s.getActualCash())
                .variance(s.getVariance())
                .varianceType(varianceType)
                .closureNotes(s.getClosureNotes())
                .notes(s.getClosureNotes())
                .cashCountDetails(s.getCashCountDetails())
                .createdAt(s.getCreatedAt())
                .transactions(txDTOs)
                .build();
    }

    private CashTransactionDTO mapTransactionToDTO(CashTransaction tx) {
        String formattedTime = tx.getTimestamp() != null ? tx.getTimestamp().format(DISPLAY_FORMATTER) : null;
        return CashTransactionDTO.builder()
                .id(tx.getId())
                .shiftId(tx.getShift() != null ? tx.getShift().getId() : null)
                .shiftCode(tx.getShift() != null ? tx.getShift().getShiftNumber() : null)
                .type(tx.getType())
                .amount(tx.getAmount())
                .referenceNumber(tx.getReferenceNumber())
                .reason(tx.getReason())
                .paidTo(tx.getPerformedBy())
                .performedBy(tx.getPerformedBy())
                .approvedBy(tx.getPerformedBy())
                .timestamp(tx.getTimestamp())
                .formattedTime(formattedTime)
                .build();
    }
}
