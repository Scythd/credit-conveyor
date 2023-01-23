package com.moklyak.deal.enums;

import com.moklyak.deal.entities.StatusHistory;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public enum ApplicationStatus {
    PREAPPROVAL,
    APPROVED,
    CC_DENIED,
    CC_APPROVED,
    PREPARE_DOCUMENTS,
    DOCUMENT_CREATED,
    CLIENT_DENIED,
    DOCUMENT_SIGNED,
    CREDIT_ISSUED;

    public StatusHistory asStatusHistory(){
        StatusHistory sh = new StatusHistory();
        sh.setTime(Timestamp.valueOf(LocalDateTime.now()));
        sh.setStatus(this);
        sh.setChangeType(ChangeType.AUTOMATIC);
        return sh;
    }
}
