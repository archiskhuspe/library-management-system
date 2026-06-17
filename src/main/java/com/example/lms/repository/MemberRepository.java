package com.example.lms.repository;

import com.example.lms.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>, JpaSpecificationExecutor<Member> {

    /**
     * Checks if a member exists with the given email.
     * This will respect the @Where clause on the Member entity (i.e., only check active members).
     */
    boolean existsByEmail(String email);

    /**
     * Finds a member by their email.
     * This will respect the @Where clause on the Member entity.
     */
    Optional<Member> findByEmail(String email);

} 