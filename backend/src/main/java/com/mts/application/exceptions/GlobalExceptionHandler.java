package com.mts.application.exceptions;

import com.mts.domain.exceptions.AccountNotActiveException;
import com.mts.domain.exceptions.AccountNotFoundException;
import com.mts.domain.exceptions.DuplicateTransferException;
import com.mts.domain.exceptions.InsufficientBalanceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<String> handleAccountNotFoundException(AccountNotFoundException anfe){
        return new ResponseEntity<>(anfe.getMessage(), HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(AccountNotActiveException.class)
    public ResponseEntity<String> handleAccountNotActiveException(AccountNotActiveException anae){
        return new ResponseEntity<>(anae.getMessage(), HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(DuplicateTransferException.class)
    public ResponseEntity<String> handleDuplicateTransferException(DuplicateTransferException dtfe){
        return new ResponseEntity<>(dtfe.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<String> handleInsufficientBalanceException(InsufficientBalanceException ibe){
        return new ResponseEntity<>(ibe.getMessage(), HttpStatus.NOT_FOUND);
    }


}
