package com.mts.application.service;

import com.mts.domain.dto.TransferRequest;
import com.mts.domain.dto.TransferResponse;

public interface TransferService {
    TransferResponse transfer(TransferRequest request);
}
