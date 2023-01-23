package com.moklyak.deal.repositories;

import com.moklyak.deal.entities.Credit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditRepository extends JpaRepository<Credit, Long> {
}
