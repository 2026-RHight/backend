package com.reverse.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "sequence_doc")
@Comment("채번 테이블")
public class SequenceDoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "prefix", nullable = false, length = 3)
    private String prefix;

    @Column(name = "year", nullable = false, length = 4)
    private String year;

    @Column(name = "last_doc", nullable = false)
    private int lastDoc = 0;

}
