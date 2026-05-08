package com.eliteresume.api.repository;

import com.eliteresume.api.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {}
