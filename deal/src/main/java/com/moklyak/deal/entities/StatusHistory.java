package com.moklyak.deal.entities;

import com.moklyak.deal.enums.ApplicationStatus;
import com.moklyak.deal.enums.ChangeType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.sql.Timestamp;

@Getter
@Setter
public class StatusHistory implements Serializable {
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;
    private Timestamp time;
    @Enumerated(EnumType.STRING)
    private ChangeType changeType;
}
