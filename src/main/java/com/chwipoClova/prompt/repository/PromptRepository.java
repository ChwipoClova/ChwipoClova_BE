package com.chwipoClova.prompt.repository;

import com.chwipoClova.prompt.entity.Prompt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PromptRepository extends JpaRepository<Prompt, Long> {
    List<Prompt> findByCategory_CodeIgnoreCaseAndUseYnIgnoreCaseOrderByUpdDtDesc(String code, String useYn);

    List<Prompt> findByCategory_CodeIgnoreCaseAndUseYnIgnoreCaseOrderByCategory_UpdDtDesc(String code, String useYn);
}