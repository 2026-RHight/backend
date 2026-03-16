package com.reverse.attendance.internal.persistence;

import com.reverse.attendance.internal.dto.response.AttendanceRequestHistoryItemResponse;
import com.reverse.attendance.internal.dto.response.RequestStatusCountResponse;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ApprovalRequestHistoryMapper {
    List<AttendanceRequestHistoryItemResponse> findMyRequestHistory(
            @Param("employeeId") Long employeeId);

    RequestStatusCountResponse countMyRequestHistory(@Param("employeeId") Long employeeId);
}
