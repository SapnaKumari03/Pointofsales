package com.j20_pos.service;

import com.j20_pos.dto.*;
import com.j20_pos.entity.Order;

import java.util.List;

public interface CashService {

    CashShiftDTO openShift(OpenShiftDTO dto);

    CashShiftDTO closeShift(CloseShiftDTO dto);

    CashShiftDTO getActiveShift();

    List<CashShiftDTO> getShiftHistory();

    CashShiftDTO getShiftById(Long id);

    List<CashTransactionDTO> getShiftTransactions(Long shiftId);

    List<CashTransactionDTO> getAllTransactions();

    CashTransactionDTO recordCashPayout(CashPayoutDTO dto);

    PaymentSummaryDTO getPaymentSummary();

    void recordSalePayment(Order order);
}
