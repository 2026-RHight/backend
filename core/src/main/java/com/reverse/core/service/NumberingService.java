package com.reverse.core.service;

import com.reverse.core.mapper.SequenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NumberingService {

    private final SequenceMapper sequenceMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generateSequence(String prefix) {
        String year = String.valueOf(LocalDateTime.now().getYear());
        int affectedRows = sequenceMapper.updateSequenceNumber(prefix, year);

        if (affectedRows == 0) {
            try {
                sequenceMapper.insertInitialSequence(prefix, year)
            } catch (DuplicateKeyException e){
                sequenceMapper.updateSequenceNumber(year, prefix);
            }
        }

        int sequenceNumber = sequenceMapper.getSequenceNumber(prefix, year);

        return String.format("%s-%s-%04d", prefix, year, sequenceNumber);
    }
}
