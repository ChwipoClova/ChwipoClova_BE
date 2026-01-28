package com.chwipoClova.resume.repository;

import com.chwipoClova.resume.entity.Resume;
import com.chwipoClova.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    List<Resume> findByUserUserIdAndDelFlagOrderByRegDate(Long userId, Integer delFlag);

    Optional<Resume> findByUserUserIdAndResumeIdAndDelFlag(Long userId, Long resumeId, Integer delFlag);

    Optional<Resume> findTop1ByUserUserIdAndDelFlagOrderByRegDate(Long userId, Integer delFlag);

    long deleteByUser(User user);

}
