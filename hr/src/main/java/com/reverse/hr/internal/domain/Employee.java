package com.reverse.hr.internal.domain;

import com.reverse.hr.internal.domain.enums.EmployeeState;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(uniqueConstraints = @UniqueConstraint(name = "UK_USER_EMPLOYEE_NUM", columnNames = {"employee_num"}))
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long employeeId;

    @Column(nullable = false)
    private String employeeNum;

    @Column(nullable = false)
    private String employeeName;

    @Column(nullable = false)
    private String employeePassword;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String ext;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false)
    private String bankName;

    // 계좌번호: 복호화 가능한 암호문
    @Column(name = "account_number_enc", nullable = false, columnDefinition = "TEXT")
    private String accountNumberEnc;

    // 계좌번호: 비교/검색용 해시
    @Column(name = "account_number_hash", nullable = false, length = 64)
    private String accountNumberHash;

    // 주민번호: 복호화 가능한 암호문
    @Column(name = "resident_number_enc", nullable = false, columnDefinition = "TEXT")
    private String residentNumberEnc;

    // 주민번호: 초기화 검증용 해시
    @Column(name = "resident_number_hash", nullable = false, length = 64)
    private String residentNumberHash;

    @Column(nullable = false)
    private Boolean initialState;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmployeeState employState;

    //TODO(클로이): fk 인사파일 테이블 조인
    @Column(nullable = false)
    private Long profileId;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "employee_role",
            joinColumns = @JoinColumn(name = "employee_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private java.util.Set<Role> roles = new java.util.HashSet<>();
}
