package com.chwipoClova.feedback.repository;

import com.chwipoClova.feedback.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    List<Feedback> findByQaQaIdOrderByFeedbackId(Long qaId);

    Optional<Feedback> findByQaQaIdAndType(Long qaId, Integer Type);

    void deleteByQaQaId(Long qaId);


}
