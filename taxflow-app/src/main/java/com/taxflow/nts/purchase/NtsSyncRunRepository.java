package com.taxflow.nts.purchase;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NtsSyncRunRepository extends JpaRepository<NtsSyncRun, Long> {

    @Query("select r from NtsSyncRun r where r.tenant.id = :tenantId order by r.startedAt desc")
    List<NtsSyncRun> findRecentByTenant(@Param("tenantId") Long tenantId, Pageable pageable);
}
