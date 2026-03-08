package com.reverse.approval.internal.persistence.row;

import java.time.LocalDateTime;

public record RecipientLineDetailRow(
        Long receiverId, String receiverName, String receiverRank, LocalDateTime readDate) {}
