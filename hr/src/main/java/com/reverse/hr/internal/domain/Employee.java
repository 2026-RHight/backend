package com.reverse.hr.internal.domain;

import jakarta.persistence.*;
import lombok.*;
import org.aspectj.weaver.loadtime.definition.Definition;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "employee")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long employeeId;

    @Column(unique = true, nullable = false, columnDefinition = "TEXT")
    private String email;


}
