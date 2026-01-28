package com.chwipoClova.qa.repository;

import com.chwipoClova.qa.entity.Qa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QaRepository extends JpaRepository<Qa, Long> {

    Optional<Qa> findByInterviewInterviewIdAndQaIdAndDelFlag(Long interviewId, Long qaId, Integer delFlag);

    List<Qa> findByInterviewInterviewIdAndDelFlagOrderByQaId(Long interviewId, Integer delFlag);

    Qa findFirstByInterviewInterviewIdAndDelFlagOrderByQaIdDesc(Long interviewId, Integer delFlag);

    @Modifying
    @Query(value = "update Qa q set q.answer = null where q.interviewId = :interviewId and answer is not null and delFlag =:delFlag", nativeQuery = true)
    int initQa(@Param("interviewId") Long interviewId, @Param("delFlag") Integer delFlag);

    void deleteByInterviewInterviewId(Long interviewId);

    List<Qa> findByInterview_InterviewId(Long interviewId);

}
