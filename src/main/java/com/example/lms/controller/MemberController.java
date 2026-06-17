package com.example.lms.controller;

import com.example.lms.dto.CreateMemberRequestDto;
import com.example.lms.dto.MemberDto;
import com.example.lms.dto.UpdateMemberRequestDto;
import com.example.lms.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@Tag(name = "Member Management", description = "APIs for managing library members")
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "Add a new member", description = "Registers a new member in the library system.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Member registered successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MemberDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "409", description = "Duplicate email",
            content = @Content(mediaType = "application/json"))
    })
    @PostMapping
    public ResponseEntity<MemberDto> addMember(@Valid @RequestBody CreateMemberRequestDto createMemberRequestDto) {
        MemberDto newMember = memberService.addMember(createMemberRequestDto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}").buildAndExpand(newMember.id()).toUri();
        return ResponseEntity.created(location).body(newMember);
    }

    @Operation(summary = "Get a member by ID", description = "Retrieves details of a specific member by their ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Member found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MemberDto.class))),
        @ApiResponse(responseCode = "404", description = "Member not found",
            content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/{id}")
    public ResponseEntity<MemberDto> getMemberById(
        @Parameter(description = "ID of the member to retrieve", required = true, example = "1")
        @PathVariable Long id) {
        MemberDto memberDto = memberService.getMemberById(id);
        return ResponseEntity.ok(memberDto);
    }

    @Operation(summary = "List all members", description = "Retrieves a paginated list of all library members.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Members listed successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))) // Schema should be Page<MemberDto>
    })
    @GetMapping
    public ResponseEntity<Page<MemberDto>> getAllMembers(
        @Parameter(description = "Pagination and sorting parameters") 
        @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        Page<MemberDto> memberPage = memberService.getAllMembers(pageable);
        return ResponseEntity.ok(memberPage);
    }

    @Operation(summary = "Update member details", description = "Updates details of an existing member. Only provided fields are updated.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Member updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MemberDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "Member not found",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "409", description = "Duplicate email if email is changed to an existing one",
            content = @Content(mediaType = "application/json"))
    })
    @PutMapping("/{id}")
    public ResponseEntity<MemberDto> updateMember(
        @Parameter(description = "ID of the member to update", required = true, example = "1")
        @PathVariable Long id,
        @Valid @RequestBody UpdateMemberRequestDto updateMemberRequestDto) {
        MemberDto updatedMember = memberService.updateMember(id, updateMemberRequestDto);
        return ResponseEntity.ok(updatedMember);
    }

    @Operation(summary = "Soft delete a member", description = "Marks a member as deleted. The member is not physically removed.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Member soft-deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Member not found",
            content = @Content(mediaType = "application/json"))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteMember(
        @Parameter(description = "ID of the member to soft delete", required = true, example = "1")
        @PathVariable Long id) {
        memberService.softDeleteMember(id);
        return ResponseEntity.noContent().build();
    }
} 