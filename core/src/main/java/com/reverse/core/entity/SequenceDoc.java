package com.reverse.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.Comment;

@Entity
@Table(
        name = "sequence_doc",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_sequence_doc_prefix_year",
                        columnNames = {"prefix", "doc_year"}))
@Comment("채번 테이블")
public class SequenceDoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "prefix", nullable = false, length = 3)
    private String prefix;

    @Column(name = "doc_year", nullable = false, length = 4)
    private String docYear;

    @Column(name = "last_doc", nullable = false)
    private int lastDoc = 0;
}
