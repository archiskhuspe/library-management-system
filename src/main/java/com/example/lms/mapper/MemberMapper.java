package com.example.lms.mapper;

import com.example.lms.dto.CreateMemberRequestDto;
import com.example.lms.dto.MemberDto;
import com.example.lms.model.Member;
import org.springframework.stereotype.Component;

@Component
public class MemberMapper {

    public MemberDto toDto(Member member) {
        if (member == null) {
            return null;
        }
        return new MemberDto(
            member.getId(),
            member.getName(),
            member.getEmail(),
            member.getJoinDate()
        );
    }

    public Member toEntity(CreateMemberRequestDto createMemberRequestDto) {
        if (createMemberRequestDto == null) {
            return null;
        }
        return Member.builder()
            .name(createMemberRequestDto.name())
            .email(createMemberRequestDto.email())
            .joinDate(createMemberRequestDto.joinDate())
            // isDeleted defaults to false in the entity
            .build();
    }

    // Logic for updating an existing Member entity from UpdateMemberRequestDto
    // will be handled in the MemberServiceImpl to manage partial updates.
} 