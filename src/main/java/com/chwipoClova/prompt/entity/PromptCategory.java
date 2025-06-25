package com.chwipoClova.prompt.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "prompt_category")
public class PromptCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 100)
    @NotNull
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @ColumnDefault("current_timestamp()")
    @Column(name = "reg_dt")
    private Instant regDt;

    @ColumnDefault("current_timestamp()")
    @Column(name = "upd_dt")
    private Instant updDt;

    @Size(max = 20)
    @Column(name = "code", length = 20)
    private String code;

}