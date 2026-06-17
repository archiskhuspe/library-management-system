package com.example.lms.service;

import com.example.lms.dto.CreateMemberRequestDto;
import com.example.lms.dto.MemberDto;
import com.example.lms.dto.UpdateMemberRequestDto;
import com.example.lms.exception.DuplicateEmailException;
import com.example.lms.exception.ResourceNotFoundException;
import com.example.lms.mapper.MemberMapper;
import com.example.lms.model.Member;
import com.example.lms.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private static final Logger log = LoggerFactory.getLogger(MemberServiceImpl.class);
    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;

    @Override
    @Transactional
    public MemberDto addMember(CreateMemberRequestDto createMemberRequestDto) {
        log.info("Attempting to add a new member with email: {}", createMemberRequestDto.email());

        if (memberRepository.existsByEmail(createMemberRequestDto.email())) {
            log.warn("Attempt to add member with duplicate active email: {}", createMemberRequestDto.email());
            throw new DuplicateEmailException(createMemberRequestDto.email());
        }

        Member member = memberMapper.toEntity(createMemberRequestDto);
        try {
            Member savedMember = memberRepository.save(member);
            log.info("Successfully added member with ID: {} and email: {}", savedMember.getId(), savedMember.getEmail());
            return memberMapper.toDto(savedMember);
        } catch (DataIntegrityViolationException e) {
            log.warn("Data integrity violation while adding member, likely duplicate email including soft-deleted: {}", 
                createMemberRequestDto.email(), e);
            throw new DuplicateEmailException("Member with email '" + createMemberRequestDto.email() + 
                "' already exists (possibly soft-deleted). Cause: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public MemberDto getMemberById(Long id) {
        log.debug("Fetching member by ID: {}", id);
        Member member = memberRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Member not found with ID: {}", id);
                return new ResourceNotFoundException("Member", "ID", id);
            });
        return memberMapper.toDto(member);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MemberDto> getAllMembers(Pageable pageable) {
        log.debug("Fetching all members. Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Member> memberPage = memberRepository.findAll(pageable); // No specific search criteria for members yet
        log.info("Found {} members.", memberPage.getTotalElements());
        return memberPage.map(memberMapper::toDto);
    }

    @Override
    @Transactional
    public MemberDto updateMember(Long id, UpdateMemberRequestDto updateMemberRequestDto) {
        log.info("Attempting to update member with ID: {}", id);
        Member existingMember = memberRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Member not found for update with ID: {}", id);
                return new ResourceNotFoundException("Member", "ID", id);
            });

        boolean updated = false;

        if (StringUtils.hasText(updateMemberRequestDto.name())) {
            existingMember.setName(updateMemberRequestDto.name());
            updated = true;
        }
        if (updateMemberRequestDto.joinDate() != null) {
            existingMember.setJoinDate(updateMemberRequestDto.joinDate());
            updated = true;
        }

        if (StringUtils.hasText(updateMemberRequestDto.email()) && !updateMemberRequestDto.email().equalsIgnoreCase(existingMember.getEmail())) {
            log.info("Email is being updated for member ID: {}. New email: {}, Old email: {}", 
                id, updateMemberRequestDto.email(), existingMember.getEmail());
            if (memberRepository.existsByEmail(updateMemberRequestDto.email())) {
                log.warn("Attempt to update member ID: {} with duplicate active email: {}", id, updateMemberRequestDto.email());
                throw new DuplicateEmailException(updateMemberRequestDto.email());
            }
            existingMember.setEmail(updateMemberRequestDto.email());
            updated = true;
        }

        if (updated) {
            try {
                Member updatedMember = memberRepository.save(existingMember);
                log.info("Successfully updated member with ID: {}", updatedMember.getId());
                return memberMapper.toDto(updatedMember);
            } catch (DataIntegrityViolationException e) {
                log.warn("Data integrity violation while updating member ID: {}, likely duplicate email: {}", 
                    id, existingMember.getEmail(), e);
                throw new DuplicateEmailException("Member with email '" + existingMember.getEmail() + 
                    "' already exists (possibly soft-deleted). Cause: " + e.getMessage());
            }
        } else {
            log.info("No changes detected for member ID: {}. Returning existing data.", id);
            return memberMapper.toDto(existingMember);
        }
    }

    @Override
    @Transactional
    public void softDeleteMember(Long id) {
        log.info("Attempting to soft delete member with ID: {}", id);
        Member member = memberRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Member not found for soft delete with ID: {}", id);
                return new ResourceNotFoundException("Member", "ID", id);
            });
        
        memberRepository.deleteById(member.getId());
        log.info("Successfully soft-deleted member with ID: {}", id);
    }
} 