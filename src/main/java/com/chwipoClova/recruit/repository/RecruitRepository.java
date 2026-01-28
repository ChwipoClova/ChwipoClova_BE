package com.chwipoClova.recruit.repository;

import com.chwipoClova.recruit.entity.Recruit;
import com.chwipoClova.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;
import java.util.List;

public interface RecruitRepository extends JpaRepository<Recruit, Long> {
    List<Recruit> findByDelFlagAndRegDateLessThanEqual(Integer delFlag, Timestamp baseDate);

    long deleteByUser(User user);
}
