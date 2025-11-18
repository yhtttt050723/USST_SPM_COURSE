package com.usst.spm.demo.repository;

import com.usst.spm.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByStudentNo(String studentNo);
    boolean existsByStudentNo(String studentNo);
}