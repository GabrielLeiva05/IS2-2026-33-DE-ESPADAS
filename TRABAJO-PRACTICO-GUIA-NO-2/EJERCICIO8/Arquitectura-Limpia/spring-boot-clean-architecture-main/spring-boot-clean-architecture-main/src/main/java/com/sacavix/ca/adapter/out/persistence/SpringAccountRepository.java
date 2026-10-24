package com.sacavix.ca.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringAccountRepository extends JpaRepository<AccountEntity, Long> {
}