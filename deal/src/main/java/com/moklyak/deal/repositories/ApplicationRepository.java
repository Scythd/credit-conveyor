package com.moklyak.deal.repositories;

import com.moklyak.deal.entities.Application;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
}
