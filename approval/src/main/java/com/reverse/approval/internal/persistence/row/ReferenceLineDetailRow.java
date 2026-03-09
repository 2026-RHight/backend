package com.reverse.approval.internal.persistence.row;

import java.time.LocalDateTime;

public record ReferenceLineDetailRow(
        Long referencerId, String referencerName, String referenceRank, LocalDateTime readDate) {}
