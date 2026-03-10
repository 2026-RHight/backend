package com.reverse.performance.internal.persistence;

import com.reverse.performance.internal.dto.request.PeerReviewRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PeerReviewMapper {
    void savePeerReview(PeerReviewRequest request);

    int countByEvalIdAndReviewerId(
            @Param("evalId") Long evalId, @Param("reviewerId") Long reviewerId);
}
