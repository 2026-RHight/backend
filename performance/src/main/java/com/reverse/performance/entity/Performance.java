package com.reverse.performance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Performance {

    @Id
    @Column(name = "performance_id")
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "work_item")
    @Enumerated(EnumType.ORDINAL)
    private WorkItem workItem;

    @Column(name = "work_detail")
    private String workDetail;

    @Column(name = "status")
    @Enumerated(EnumType.ORDINAL)
    private Status status;

    @Column(name = "achievment_rate")
    private Integer achievmentRate;

    @Column(name = "feedback")
    private String feedback;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "expected_end_date")
    private LocalDate expectedEndDate;

    @Column(name = "end_date")
    private LocalDate endDate;
}
