package com.reverse.attendance.internal.persistence;

import com.reverse.attendance.internal.dto.response.AttendanceVacationHistoryItemResponse;
import com.reverse.attendance.internal.persistence.row.ApprovalVacationBalanceRow;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ApprovalVacationHistoryMapper {
    List<AttendanceVacationHistoryItemResponse> findMyVacationHistory(
            @Param("employeeId") Long employeeId);

    List<ApprovalVacationBalanceRow> findVacationBalanceItems(
            @Param("employeeId") Long employeeId, @Param("year") int year);
}
