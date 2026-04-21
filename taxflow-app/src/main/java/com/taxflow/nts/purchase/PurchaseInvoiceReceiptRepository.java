package com.taxflow.nts.purchase;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseInvoiceReceiptRepository extends JpaRepository<PurchaseInvoiceReceipt, Long> {

    boolean existsByTenant_IdAndNtsApprovalNumber(Long tenantId, String ntsApprovalNumber);

    @Query(
            "select p from PurchaseInvoiceReceipt p where p.tenant.id = :tenantId "
                    + "order by p.issueDate desc, p.syncedAt desc"
    )
    List<PurchaseInvoiceReceipt> findAllForTenant(@Param("tenantId") Long tenantId);

    @Query("select max(p.syncedAt) from PurchaseInvoiceReceipt p where p.tenant.id = :tenantId")
    Optional<Instant> findLatestSyncedAt(@Param("tenantId") Long tenantId);
}
