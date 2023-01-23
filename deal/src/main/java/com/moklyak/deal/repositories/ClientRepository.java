package com.moklyak.deal.repositories;

import com.moklyak.deal.entities.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {

}
