package com.reverse.performance.internal.persistence;

import com.reverse.performance.internal.dto.request.PeerReviewRequest;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PeerReviewMapper {
    void savePeerReview(PeerReviewRequest request);
}
