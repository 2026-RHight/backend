package com.reverse.hr.internal.dto.response;

public record EvidenceFileResponseDTO(
        Long hrFileId,
        String fileTitle,
        String fileUrl
) {
}
