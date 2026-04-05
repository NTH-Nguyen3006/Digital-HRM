package com.company.hrm.module.leave.repository;

import com.company.hrm.module.leave.entity.LeaLeaveBalanceTransaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaLeaveBalanceTransactionRepository extends JpaRepository<LeaLeaveBalanceTransaction, Long> {

    List<LeaLeaveBalanceTransaction> findAllByLeaveBalanceLeaveBalanceIdOrderByTransactionDateDescLeaveBalanceTxnIdDesc(Long leaveBalanceId);
}
