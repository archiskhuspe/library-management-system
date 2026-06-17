package com.example.lms.service;

import com.example.lms.dto.CreateMemberRequestDto;
import com.example.lms.dto.MemberDto;
import com.example.lms.dto.UpdateMemberRequestDto;
import com.example.lms.exception.DuplicateEmailException;
import com.example.lms.exception.ResourceNotFoundException;
import com.example.lms.mapper.MemberMapper;
import com.example.lms.model.Member;
import com.example.lms.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberMapper memberMapper;

    @InjectMocks
    private MemberServiceImpl memberServiceImpl;

    private Member member1;
    private MemberDto memberDto1;
    private CreateMemberRequestDto createMemberRequestDto;
    private UpdateMemberRequestDto updateMemberRequestDto;

    @BeforeEach
    void setUp() {
        member1 = Member.builder()
            .id(1L)
            .name("Test Member 1")
            .email("test1@example.com")
            .joinDate(LocalDate.now().minusMonths(6))
            .isDeleted(false)
            .build();

        memberDto1 = new MemberDto(1L, "Test Member 1", "test1@example.com", member1.getJoinDate());
        createMemberRequestDto = new CreateMemberRequestDto("New Member", "new@example.com", LocalDate.now());
        updateMemberRequestDto = new UpdateMemberRequestDto("Updated Name", null, null);
    }

    @Test
    void addMember_success() {
        when(memberRepository.existsByEmail(createMemberRequestDto.email())).thenReturn(false);
        // Simulate the mapper returning a new entity based on the DTO for save
        Member newMemberEntity = Member.builder()
            .name(createMemberRequestDto.name())
            .email(createMemberRequestDto.email())
            .joinDate(createMemberRequestDto.joinDate())
            .build();
        Member savedMemberEntity = Member.builder()
            .id(2L) // Simulate ID generation
            .name(createMemberRequestDto.name())
            .email(createMemberRequestDto.email())
            .joinDate(createMemberRequestDto.joinDate())
            .build();
        MemberDto expectedDto = new MemberDto(2L, createMemberRequestDto.name(), createMemberRequestDto.email(), createMemberRequestDto.joinDate());

        when(memberMapper.toEntity(createMemberRequestDto)).thenReturn(newMemberEntity);
        when(memberRepository.save(any(Member.class))).thenReturn(savedMemberEntity);
        when(memberMapper.toDto(savedMemberEntity)).thenReturn(expectedDto);

        MemberDto result = memberServiceImpl.addMember(createMemberRequestDto);

        assertNotNull(result);
        assertEquals(expectedDto.name(), result.name());
        assertEquals(expectedDto.email(), result.email());
        verify(memberRepository).existsByEmail(createMemberRequestDto.email());
        verify(memberRepository).save(any(Member.class));
        verify(memberMapper).toEntity(createMemberRequestDto);
        verify(memberMapper).toDto(savedMemberEntity);
    }

    @Test
    void addMember_duplicateEmail_active() {
        when(memberRepository.existsByEmail(createMemberRequestDto.email())).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> memberServiceImpl.addMember(createMemberRequestDto));
        verify(memberRepository).existsByEmail(createMemberRequestDto.email());
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    void addMember_duplicateEmail_dataIntegrityViolation() {
        when(memberRepository.existsByEmail(createMemberRequestDto.email())).thenReturn(false);
        when(memberMapper.toEntity(createMemberRequestDto)).thenReturn(new Member()); 
        when(memberRepository.save(any(Member.class))).thenThrow(new DataIntegrityViolationException("Duplicate email from DB"));

        DuplicateEmailException exception = assertThrows(DuplicateEmailException.class, () -> memberServiceImpl.addMember(createMemberRequestDto));
        assertTrue(exception.getMessage().contains("already exists (possibly soft-deleted)"));
        verify(memberRepository).existsByEmail(createMemberRequestDto.email());
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void getMemberById_found() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member1));
        when(memberMapper.toDto(member1)).thenReturn(memberDto1);

        MemberDto result = memberServiceImpl.getMemberById(1L);

        assertNotNull(result);
        assertEquals(memberDto1.name(), result.name());
        verify(memberRepository).findById(1L);
    }

    @Test
    void getMemberById_notFound() {
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> memberServiceImpl.getMemberById(1L));
        verify(memberRepository).findById(1L);
    }

    @Test
    void getAllMembers_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Member> memberPage = new PageImpl<>(Collections.singletonList(member1), pageable, 1);

        when(memberRepository.findAll(pageable)).thenReturn(memberPage);
        when(memberMapper.toDto(member1)).thenReturn(memberDto1);

        Page<MemberDto> result = memberServiceImpl.getAllMembers(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(memberDto1.name(), result.getContent().get(0).name());
        verify(memberRepository).findAll(pageable);
    }

    @Test
    void updateMember_success_noEmailChange() {
        UpdateMemberRequestDto localUpdateDto = new UpdateMemberRequestDto("Updated Name", member1.getEmail(), LocalDate.now());
        // member1 already exists and is found
        Member memberToUpdate = Member.builder()
            .id(member1.getId())
            .name(member1.getName())
            .email(member1.getEmail())
            .joinDate(member1.getJoinDate())
            .build();
 
        Member updatedMemberEntity = Member.builder()
            .id(1L).name("Updated Name").email(member1.getEmail()).joinDate(LocalDate.now()).build();
        MemberDto updatedMemberDto = new MemberDto(1L, "Updated Name", member1.getEmail(), LocalDate.now());

        when(memberRepository.findById(1L)).thenReturn(Optional.of(memberToUpdate));
        when(memberRepository.save(any(Member.class))).thenReturn(updatedMemberEntity);
        when(memberMapper.toDto(updatedMemberEntity)).thenReturn(updatedMemberDto);

        MemberDto result = memberServiceImpl.updateMember(1L, localUpdateDto);

        assertNotNull(result);
        assertEquals("Updated Name", result.name());
        assertEquals(member1.getEmail(), result.email());
        verify(memberRepository).findById(1L);
        verify(memberRepository).save(memberToUpdate); // verify that the modified entity is saved
        assertEquals("Updated Name", memberToUpdate.getName()); // Check side effect on the passed entity
        assertEquals(LocalDate.now(), memberToUpdate.getJoinDate());
        verify(memberRepository, never()).existsByEmail(anyString());
    }

    @Test
    void updateMember_success_withEmailChange() {
        UpdateMemberRequestDto localUpdateDto = new UpdateMemberRequestDto("Updated Name", "updated@example.com", LocalDate.now());
        Member memberToUpdate = Member.builder()
            .id(member1.getId())
            .name(member1.getName())
            .email(member1.getEmail())
            .joinDate(member1.getJoinDate())
            .build();

        Member updatedMemberEntity = Member.builder().id(1L).name("Updated Name").email("updated@example.com").joinDate(LocalDate.now()).build();
        MemberDto updatedMemberDto = new MemberDto(1L, "Updated Name", "updated@example.com", LocalDate.now());

        when(memberRepository.findById(1L)).thenReturn(Optional.of(memberToUpdate));
        when(memberRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenReturn(updatedMemberEntity);
        when(memberMapper.toDto(updatedMemberEntity)).thenReturn(updatedMemberDto);

        MemberDto result = memberServiceImpl.updateMember(1L, localUpdateDto);

        assertNotNull(result);
        assertEquals("updated@example.com", result.email());
        verify(memberRepository).findById(1L);
        verify(memberRepository).existsByEmail("updated@example.com");
        verify(memberRepository).save(memberToUpdate);
        assertEquals("updated@example.com", memberToUpdate.getEmail());
    }

    @Test
    void updateMember_notFound() {
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> memberServiceImpl.updateMember(1L, updateMemberRequestDto));
        verify(memberRepository).findById(1L);
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    void updateMember_emailChange_duplicateEmail_active() {
        UpdateMemberRequestDto localUpdateDto = new UpdateMemberRequestDto(null, "taken@example.com", null);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member1)); 
        when(memberRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> memberServiceImpl.updateMember(1L, localUpdateDto));
        verify(memberRepository).findById(1L);
        verify(memberRepository).existsByEmail("taken@example.com");
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    void updateMember_emailChange_duplicateEmail_dataIntegrity() {
        UpdateMemberRequestDto localUpdateDto = new UpdateMemberRequestDto(null, "another@example.com", null);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member1));
        when(memberRepository.existsByEmail("another@example.com")).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenThrow(new DataIntegrityViolationException("DB duplicate email"));

        DuplicateEmailException exception = assertThrows(DuplicateEmailException.class, () -> memberServiceImpl.updateMember(1L, localUpdateDto));
        assertTrue(exception.getMessage().contains("already exists (possibly soft-deleted)"));
        verify(memberRepository).findById(1L);
        verify(memberRepository).existsByEmail("another@example.com");
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void updateMember_noChanges() {
        UpdateMemberRequestDto noChangeDto = new UpdateMemberRequestDto(null, null, null);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member1));
        when(memberMapper.toDto(member1)).thenReturn(memberDto1);

        MemberDto result = memberServiceImpl.updateMember(1L, noChangeDto);

        assertNotNull(result);
        assertEquals(memberDto1.name(), result.name());
        verify(memberRepository).findById(1L);
        verify(memberRepository, never()).save(any(Member.class));
        verify(memberMapper).toDto(member1);
    }

    @Test
    void softDeleteMember_success() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member1));
        doNothing().when(memberRepository).deleteById(1L);

        memberServiceImpl.softDeleteMember(1L);

        verify(memberRepository).findById(1L);
        verify(memberRepository).deleteById(1L);
    }

    @Test
    void softDeleteMember_notFound() {
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> memberServiceImpl.softDeleteMember(1L));
        verify(memberRepository).findById(1L);
        verify(memberRepository, never()).deleteById(anyLong());
    }
} 