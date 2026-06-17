package com.example.lms.controller;

import com.example.lms.dto.CreateMemberRequestDto;
import com.example.lms.dto.MemberDto;
import com.example.lms.dto.UpdateMemberRequestDto;
import com.example.lms.model.Member;
import com.example.lms.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MemberControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll(); // Clean slate for each test
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void memberCrudFlow_createRetrieveUpdateDelete() throws Exception {
        // 1. Create Member
        CreateMemberRequestDto createDto = new CreateMemberRequestDto(
            "CRUD Member", "crud.member@example.com", LocalDate.of(2023, 3, 10)
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.name", is("CRUD Member")))
            .andExpect(jsonPath("$.email", is("crud.member@example.com")))
            .andReturn();

        MemberDto createdMember = objectMapper.readValue(createResult.getResponse().getContentAsString(), MemberDto.class);
        Long memberId = createdMember.id();
        assertNotNull(memberId);

        // 2. Retrieve Member
        mockMvc.perform(get("/api/v1/members/{id}", memberId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(memberId.intValue())))
            .andExpect(jsonPath("$.name", is("CRUD Member")));

        // 3. Update Member
        UpdateMemberRequestDto updateDto = new UpdateMemberRequestDto(
            "CRUD Member Updated", "crud.member.updated@example.com", LocalDate.of(2023, 4, 15)
        );

        mockMvc.perform(put("/api/v1/members/{id}", memberId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is("CRUD Member Updated")))
            .andExpect(jsonPath("$.email", is("crud.member.updated@example.com")));

        // Verify update in DB
        Member updatedDbMember = memberRepository.findById(memberId).orElseThrow();
        assertEquals("CRUD Member Updated", updatedDbMember.getName());
        assertEquals("crud.member.updated@example.com", updatedDbMember.getEmail());

        // 4. Soft Delete Member
        mockMvc.perform(delete("/api/v1/members/{id}", memberId))
            .andExpect(status().isNoContent());

        // 5. Verify Soft Delete (Attempt to retrieve should fail with 404)
        mockMvc.perform(get("/api/v1/members/{id}", memberId))
            .andExpect(status().isNotFound());
        
        assertEquals(0, memberRepository.count(), "No active members should remain after soft delete.");
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void addMember_duplicateEmail_shouldFailWithConflict() throws Exception {
        Member initialMember = Member.builder()
            .name("Existing Member")
            .email("existing.member@example.com")
            .joinDate(LocalDate.now())
            .build();
        memberRepository.save(initialMember);

        CreateMemberRequestDto createDuplicateDto = new CreateMemberRequestDto(
            "Another New Member", "existing.member@example.com", // Same email
            LocalDate.of(2024, 2, 1)
        );

        mockMvc.perform(post("/api/v1/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDuplicateDto)))
            .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void getAllMembers_withPagination() throws Exception {
        memberRepository.save(Member.builder().name("Alice Smith").email("alice@example.com").joinDate(LocalDate.now()).build());
        memberRepository.save(Member.builder().name("Bob Johnson").email("bob@example.com").joinDate(LocalDate.now()).build());
        memberRepository.save(Member.builder().name("Charlie Brown").email("charlie@example.com").joinDate(LocalDate.now()).build());

        mockMvc.perform(get("/api/v1/members").param("page", "0").param("size", "2").param("sort", "name,asc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(2)))
            .andExpect(jsonPath("$.totalElements", is(3)))
            .andExpect(jsonPath("$.totalPages", is(2)))
            .andExpect(jsonPath("$.content[0].name", is("Alice Smith"))) // Assuming default sort or sorted by name asc
            .andExpect(jsonPath("$.content[1].name", is("Bob Johnson")));
    }
} 