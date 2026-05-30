package com.finale.repository;

import com.finale.entity.Presenza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PresenzaRepository extends JpaRepository<Presenza, Long> {
}
