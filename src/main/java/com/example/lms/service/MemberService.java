package com.example.lms.service;

import com.example.lms.dto.CreateMemberRequestDto;
import com.example.lms.dto.MemberDto;
import com.example.lms.dto.UpdateMemberRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberService {

    MemberDto addMember(CreateMemberRequestDto createMemberRequestDto);

    MemberDto getMemberById(Long id);

    Page<MemberDto> getAllMembers(Pageable pageable);

    MemberDto updateMember(Long id, UpdateMemberRequestDto updateMemberRequestDto);

    void softDeleteMember(Long id);
} 