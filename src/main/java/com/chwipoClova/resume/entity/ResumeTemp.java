package com.chwipoClova.resume.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "Resume_temp")
public class ResumeTemp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resumeId", nullable = false)
    private Long id;

    @Lob
    @Column(name = "fileName")
    private String fileName;

    @Lob
    @Column(name = "filePath")
    private String filePath;

    @Column(name = "fileSize")
    private Long fileSize;

    @Lob
    @Column(name = "originalFileName")
    private String originalFileName;

    @NotNull
    @Column(name = "regDate", nullable = false)
    private Date regDate;

    @Lob
    @Column(name = "summary")
    private String summary;

    @NotNull
    @Column(name = "delFlag", nullable = false)
    private Integer delFlag;

    @Column(name = "modifyDate")
    private Date modifyDate;

    @Lob
    @Column(name = "originText")
    private String originText;

}