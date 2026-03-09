package com.reverse.core.service;

import com.reverse.core.mapper.SequenceMapper;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NumberingService {

    private final SequenceMapper sequenceMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generateSequence(String prefix) {
        String docYear = String.valueOf(LocalDateTime.now().getYear());
        int affectedRows = sequenceMapper.incrementSequenceNumber(prefix, docYear);

        if (affectedRows == 0) {
            try {
                sequenceMapper.insertInitialSequence(prefix, docYear);
            } catch (DuplicateKeyException e) {
                sequenceMapper.incrementSequenceNumber(prefix, docYear);
            }
        }

        int sequenceNumber = sequenceMapper.getLastInsertId();

        return String.format("%s-%s-%04d", prefix, docYear, sequenceNumber);
    }
}
