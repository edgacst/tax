package com.taxflow.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByTenantIdOrderByCreatedAtDesc(Long tenantId);

    List<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<AuditLog> findByTenantIdAndActionOrderByCreatedAtDesc(Long tenantId, String action);

    List<AuditLog> findByResourceIdAndResourceTypeOrderByCreatedAtDesc(Long resourceId, String resourceType);

    @Query("SELECT a FROM AuditLog a WHERE a.createdAt BETWEEN :from AND :to ORDER BY a.createdAt DESC")
    List<AuditLog> findByDateRange(@Param("from") Instant from, @Param("to") Instant to);

    @Query("SELECT a FROM AuditLog a WHERE a.tenantId = :tenantId AND a.createdAt BETWEEN :from AND :to ORDER BY a.createdAt DESC")
    List<AuditLog> findByTenantAndDateRange(
            @Param("tenantId") Long tenantId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );
}
