package com.j20_pos.controller;

import com.j20_pos.dto.*;
import com.j20_pos.service.CashService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cash")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RequiredArgsConstructor
public class CashController {

    private final CashService cashService;

    @PostMapping("/shifts/open")
    public ResponseEntity<ApiResponse<CashShiftDTO>> openShift(@Valid @RequestBody OpenShiftDTO dto) {
        CashShiftDTO shift = cashService.openShift(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Cash shift opened successfully", shift));
    }

    @PostMapping("/shifts/close")
    public ResponseEntity<ApiResponse<CashShiftDTO>> closeShift(@Valid @RequestBody CloseShiftDTO dto) {
        CashShiftDTO shift = cashService.closeShift(dto);
        return ResponseEntity.ok(ApiResponse.ok("Cash shift closed successfully", shift));
    }

    @GetMapping("/shifts/active")
    public ResponseEntity<ApiResponse<CashShiftDTO>> getActiveShift() {
        CashShiftDTO active = cashService.getActiveShift();
        String message = active != null ? "Active cash shift retrieved" : "No active cash shift found";
        return ResponseEntity.ok(ApiResponse.ok(message, active));
    }

    @GetMapping("/shifts/history")
    public ResponseEntity<ApiResponse<List<CashShiftDTO>>> getShiftHistory() {
        List<CashShiftDTO> history = cashService.getShiftHistory();
        return ResponseEntity.ok(ApiResponse.ok("Cash shift history retrieved successfully", history));
    }

    @GetMapping("/shifts/{id}")
    public ResponseEntity<ApiResponse<CashShiftDTO>> getShiftById(@PathVariable Long id) {
        CashShiftDTO shift = cashService.getShiftById(id);
        return ResponseEntity.ok(ApiResponse.ok("Cash shift retrieved successfully", shift));
    }

    @GetMapping("/shifts/{id}/transactions")
    public ResponseEntity<ApiResponse<List<CashTransactionDTO>>> getShiftTransactions(@PathVariable Long id) {
        List<CashTransactionDTO> transactions = cashService.getShiftTransactions(id);
        return ResponseEntity.ok(ApiResponse.ok("Shift transactions retrieved successfully", transactions));
    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<CashTransactionDTO>>> getAllTransactions() {
        List<CashTransactionDTO> transactions = cashService.getAllTransactions();
        return ResponseEntity.ok(ApiResponse.ok("Cash transactions audit log retrieved successfully", transactions));
    }

    @PostMapping("/payout")
    public ResponseEntity<ApiResponse<CashTransactionDTO>> recordPayout(@Valid @RequestBody CashPayoutDTO dto) {
        CashTransactionDTO payout = cashService.recordCashPayout(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Cash payout recorded successfully", payout));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<PaymentSummaryDTO>> getPaymentSummary() {
        PaymentSummaryDTO summary = cashService.getPaymentSummary();
        return ResponseEntity.ok(ApiResponse.ok("Payment summary retrieved successfully", summary));
    }
}
