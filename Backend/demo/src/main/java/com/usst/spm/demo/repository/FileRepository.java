package com.usst.spm.demo.repository;

import com.usst.spm.demo.model.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Long> {
}

