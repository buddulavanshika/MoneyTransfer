package com.mts.application.service;

import com.mts.application.entities.Account;
import com.mts.application.entities.TransactionLog;
import com.mts.application.repository.AccountRepository;
import com.mts.application.repository.TransactionLogRepository;
import com.mts.domain.dto.TransferRequest;
import com.mts.domain.dto.TransferResponse;
import com.mts.domain.enums.AccountStatus;
import com.mts.domain.enums.TransactionStatus;
import com.mts.domain.exceptions.AccountNotActiveException;
import com.mts.domain.exceptions.DuplicateTransferException;
import com.mts.domain.exceptions.InsufficientBalanceException;
import com.mts.domain.exceptions.OptimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceImplTest {

    @Mock
    private AccountService accountService;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionLogRepository logRepository;

    @InjectMocks
    private TransferServiceImpl transferService;

    private TransferRequest validRequest;
    private Account sender;
    private Account receiver;

    @BeforeEach
    void setUp() {
        validRequest = new TransferRequest();
        validRequest.setSourceAccountId("1");
        validRequest.setDestinationAccountId("2");
        validRequest.setAmount(new BigDecimal("100.00"));
        validRequest.setCurrency("USD");
        validRequest.setIdempotencyKey("idem-001");

        sender = new Account();
        sender.setId(1L);
        sender.setHolderName("Alice");
        sender.setBalance(new BigDecimal("500.00"));
        sender.setStatus(AccountStatus.ACTIVE);
        sender.setVersion(0L);

        receiver = new Account();
        receiver.setId(2L);
        receiver.setHolderName("Bob");
        receiver.setBalance(new BigDecimal("200.00"));
        receiver.setStatus(AccountStatus.ACTIVE);
        receiver.setVersion(0L);
    }

    @Test
    @DisplayName("Successful transfer debits source, credits destination, returns TransferResponse")
    void transfer_success() {
        when(logRepository.findByIdempotencyKey("idem-001")).thenReturn(Optional.empty());
        when(logRepository.saveAndFlush(any(TransactionLog.class))).thenAnswer(inv -> {
            TransactionLog log = inv.getArgument(0);
            return log;
        });
        when(accountService.getAccountById("1")).thenReturn(sender);
        when(accountService.getAccountById("2")).thenReturn(receiver);
        when(accountRepository.saveAndFlush(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        when(logRepository.save(any(TransactionLog.class))).thenAnswer(inv -> inv.getArgument(0));

        TransferResponse response = transferService.transfer(validRequest);

        assertThat(response).isNotNull();
        assertThat(response.transactionId()).isNotBlank();
        assertThat(response.sourceAccountId()).isEqualTo("1");
        assertThat(response.destinationAccountId()).isEqualTo("2");
        assertThat(response.amount()).isEqualByComparingTo("100.00");
        assertThat(response.status()).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(sender.getBalance()).isEqualByComparingTo("400.00");
        assertThat(receiver.getBalance()).isEqualByComparingTo("300.00");
        verify(accountRepository, times(2)).saveAndFlush(any(Account.class));
    }

    @Test
    @DisplayName("Duplicate idempotency key throws DuplicateTransferException")
    void transfer_duplicateIdempotencyKey_throws() {
        TransactionLog existing = new TransactionLog();
        existing.setId("existing-id");
        existing.setIdempotencyKey("idem-001");
        when(logRepository.findByIdempotencyKey("idem-001")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> transferService.transfer(validRequest))
                .isInstanceOf(DuplicateTransferException.class)
                .hasMessageContaining("idempotency");

        verify(accountService, never()).getAccountById(anyString());
        verify(accountRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Insufficient balance throws InsufficientBalanceException and logs FAILED")
    void transfer_insufficientBalance_throws() {
        sender.setBalance(new BigDecimal("50.00"));
        when(logRepository.findByIdempotencyKey("idem-001")).thenReturn(Optional.empty());
        when(logRepository.saveAndFlush(any(TransactionLog.class))).thenAnswer(inv -> inv.getArgument(0));
        when(accountService.getAccountById("1")).thenReturn(sender);
        when(accountService.getAccountById("2")).thenReturn(receiver);
        when(logRepository.save(any(TransactionLog.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThatThrownBy(() -> transferService.transfer(validRequest))
                .isInstanceOf(InsufficientBalanceException.class);

        verify(logRepository).save(argThat(log -> log.getStatus() == TransactionStatus.FAILED));
        verify(accountRepository, never()).saveAndFlush(any(Account.class));
    }

    @Test
    @DisplayName("Inactive account throws AccountNotActiveException")
    void transfer_inactiveAccount_throws() {
        when(logRepository.findByIdempotencyKey("idem-001")).thenReturn(Optional.empty());
        when(logRepository.saveAndFlush(any(TransactionLog.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(accountService).validateAccountForTransfer("1");
        doThrow(new AccountNotActiveException("Account 2 is not ACTIVE"))
                .when(accountService).validateAccountForTransfer("2");

        assertThatThrownBy(() -> transferService.transfer(validRequest))
                .isInstanceOf(AccountNotActiveException.class);

        verify(logRepository).save(argThat(log -> log.getStatus() == TransactionStatus.FAILED));
    }

    @Test
    @DisplayName("Optimistic lock on save throws OptimisticLockException and logs FAILED")
    void transfer_optimisticLockConflict_throws() {
        when(logRepository.findByIdempotencyKey("idem-001")).thenReturn(Optional.empty());
        when(logRepository.saveAndFlush(any(TransactionLog.class))).thenAnswer(inv -> inv.getArgument(0));
        when(accountService.getAccountById("1")).thenReturn(sender);
        when(accountService.getAccountById("2")).thenReturn(receiver);
        when(accountRepository.saveAndFlush(any(Account.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException(Account.class, sender.getId()));
        when(logRepository.save(any(TransactionLog.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThatThrownBy(() -> transferService.transfer(validRequest))
                .isInstanceOf(OptimisticLockException.class)
                .hasMessageContaining("retry");

        verify(logRepository).save(argThat(log -> log.getStatus() == TransactionStatus.FAILED
                && "Concurrent modification conflict".equals(log.getFailureReason())));
    }
}
