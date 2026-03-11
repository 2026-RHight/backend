package com.reverse.hr.internal.persistence;

import com.reverse.hr.internal.domain.enums.EmployeeState;
import com.reverse.hr.internal.persistence.row.FailedHrEventRow;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FailedHrEventRepository {

    int upsertFailedEvent(
            @Param("sourceApprovalId") Long sourceApprovalId,
            @Param("targetEmployeeState") EmployeeState targetEmployeeState,
            @Param("effectiveFrom") LocalDate effectiveFrom,
            @Param("reason") String reason,
            @Param("payloadJson") String payloadJson,
            @Param("failureMessage") String failureMessage);

    Integer acquireRetryLock(@Param("lockName") String lockName);

    Integer releaseRetryLock(@Param("lockName") String lockName);

    List<FailedHrEventRow> findRetryTargets(
            @Param("limit") int limit, @Param("maxRetryCount") int maxRetryCount);

    int markRetrying(@Param("failedEventId") Long failedEventId);

    int markResolved(@Param("failedEventId") Long failedEventId);

    int markRetryFailure(
            @Param("failedEventId") Long failedEventId,
            @Param("failureMessage") String failureMessage,
            @Param("maxRetryCount") int maxRetryCount);
}
