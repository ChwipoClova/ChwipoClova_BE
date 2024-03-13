package com.chwipoClova.interview.repository;

import com.chwipoClova.interview.entity.Interview;
import com.chwipoClova.resume.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InterviewRepository extends JpaRepository<Interview, Long> {

    Optional<Interview> findByUserUserIdAndInterviewIdAndDelFlag(Long userId, Long interviewId, Integer delFlag);

    List<Interview> findByUserUserIdAndDelFlagOrderByRegDate(Long userId, Integer delFlag);
}
