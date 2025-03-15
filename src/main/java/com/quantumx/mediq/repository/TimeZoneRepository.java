package com.quantumx.mediq.repository;

import com.quantumx.mediq.model.TimeZone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeZoneRepository extends JpaRepository<TimeZone, Long> {
}
