package com.example.vite_coding_boot.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.vite_coding_boot.application.port.out.AssignmentRepository;
import com.example.vite_coding_boot.application.port.out.AuditLogRepository;
import com.example.vite_coding_boot.application.port.out.UserRepository;
import com.example.vite_coding_boot.domain.model.ApprovalStatus;
import com.example.vite_coding_boot.domain.model.Assignment;
import com.example.vite_coding_boot.domain.model.AuditLog;
import com.example.vite_coding_boot.domain.model.Role;
import com.example.vite_coding_boot.domain.model.User;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceTest {

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AssignmentService assignmentService;

    private User creator;
    private User member;
    private User leader;
    private Assignment assignment;

    @BeforeEach
    void setUp() throws Exception {
        creator = new User("member1", "1234", "조원1", Role.MEMBER);
        member = new User("member2", "1234", "조원2", Role.MEMBER);
        leader = new User("leader", "1234", "조장", Role.LEADER);
        setUserId(leader, 100L);
        assignment = new Assignment("과제1", "설명1", creator, LocalDate.of(2099, 1, 1), LocalDate.of(2099, 12, 31));
    }

    private void setUserId(User user, Long id) throws Exception {
        Field idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(user, id);
    }

    @Test
    void createAssignment_setsCreatedBy() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(creator));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Assignment result = assignmentService.createAssignment("과제1", "설명1", 1L, LocalDate.of(2099, 1, 1), LocalDate.of(2099, 12, 31));

        assertNotNull(result);
        assertEquals("과제1", result.getTitle());
        assertEquals(creator, result.getCreatedBy());
        assertNull(result.getUser());
        verify(assignmentRepository).save(any(Assignment.class));
    }

    @Test
    void createAssignment_userNotFound_throwsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> assignmentService.createAssignment("과제", "설명", 999L, LocalDate.now(), LocalDate.now()));
    }

    @Test
    void findAllAssignments_returnsList() {
        when(assignmentRepository.findAll()).thenReturn(List.of(assignment));

        List<Assignment> results = assignmentService.findAllAssignments();

        assertEquals(1, results.size());
        assertEquals("과제1", results.get(0).getTitle());
    }

    @Test
    void findById_returnsAssignment() {
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));

        Optional<Assignment> result = assignmentService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("과제1", result.get().getTitle());
    }

    @Test
    void deleteAssignment_callsRepository() {
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(userRepository.findById(100L)).thenReturn(Optional.of(leader));
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assignmentService.deleteAssignment(1L, 100L);

        verify(assignmentRepository).deleteById(1L);
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void findAssignmentsByUser_returnsList() {
        when(assignmentRepository.findByUser(member)).thenReturn(List.of(assignment));

        List<Assignment> results = assignmentService.findAssignmentsByUser(member);

        assertEquals(1, results.size());
    }

    @Test
    void approveAssignment_setsAssignee() throws Exception {
        setUserId(member, 2L);
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(userRepository.findById(2L)).thenReturn(Optional.of(member));
        when(userRepository.findById(100L)).thenReturn(Optional.of(leader));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Assignment result = assignmentService.approveAssignment(1L, 2L, 100L);

        assertEquals(ApprovalStatus.APPROVED, result.getApprovalStatus());
        assertEquals(member, result.getUser());
        verify(assignmentRepository).save(assignment);
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void approveAssignment_notPending_throwsException() {
        assignment.setApprovalStatus(ApprovalStatus.APPROVED);
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));

        assertThrows(IllegalStateException.class,
                () -> assignmentService.approveAssignment(1L, 2L, 100L));
    }

    @Test
    void approveAssignment_notFound_throwsException() {
        when(assignmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> assignmentService.approveAssignment(999L, 2L, 100L));
    }

    @Test
    void rejectAssignment_success() {
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(100L)).thenReturn(Optional.of(leader));
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Assignment result = assignmentService.rejectAssignment(1L, "내용 부족", 100L);

        assertEquals(ApprovalStatus.REJECTED, result.getApprovalStatus());
        assertEquals("내용 부족", result.getRejectionReason());
        verify(assignmentRepository).save(assignment);
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void rejectAssignment_notPending_throwsException() {
        assignment.setApprovalStatus(ApprovalStatus.APPROVED);
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));

        assertThrows(IllegalStateException.class,
                () -> assignmentService.rejectAssignment(1L, "사유", 100L));
    }

    @Test
    void rejectAssignment_notFound_throwsException() {
        when(assignmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> assignmentService.rejectAssignment(999L, "사유", 100L));
    }

    @Test
    void updateAssignment_updatesFields() {
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Assignment result = assignmentService.updateAssignment(1L, "수정된 과제", "수정된 설명",
                LocalDate.of(2099, 2, 1), LocalDate.of(2099, 11, 30));

        assertEquals("수정된 과제", result.getTitle());
        assertEquals("수정된 설명", result.getDescription());
        assertEquals(LocalDate.of(2099, 2, 1), result.getStartDate());
        assertEquals(LocalDate.of(2099, 11, 30), result.getDueDate());
    }

    @Test
    void updateAssignment_approvedResetsToPending() {
        assignment.setApprovalStatus(ApprovalStatus.APPROVED);
        assignment.setUser(member);
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Assignment result = assignmentService.updateAssignment(1L, "수정", "설명",
                LocalDate.of(2099, 2, 1), LocalDate.of(2099, 11, 30));

        assertEquals(ApprovalStatus.PENDING, result.getApprovalStatus());
        assertNull(result.getUser());
    }

    @Test
    void submitFinalResult_success() throws Exception {
        setUserId(member, 2L);
        assignment.setApprovalStatus(ApprovalStatus.APPROVED);
        assignment.setUser(member);
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Assignment result = assignmentService.submitFinalResult(1L, "최종 결과물", 2L);

        assertEquals("최종 결과물", result.getFinalResult());
        assertNotNull(result.getResultRegisteredAt());
    }

    @Test
    void submitFinalResult_notApproved_throwsException() {
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));

        assertThrows(IllegalStateException.class,
                () -> assignmentService.submitFinalResult(1L, "결과", 2L));
    }

    @Test
    void submitFinalResult_notAssignee_throwsException() throws Exception {
        setUserId(member, 2L);
        assignment.setApprovalStatus(ApprovalStatus.APPROVED);
        assignment.setUser(member);
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));

        assertThrows(IllegalStateException.class,
                () -> assignmentService.submitFinalResult(1L, "결과", 999L));
    }

    @Test
    void findAssignmentsByCreatorOrAssignee_returnsList() {
        when(assignmentRepository.findByCreatedByOrUser(creator, creator)).thenReturn(List.of(assignment));

        List<Assignment> results = assignmentService.findAssignmentsByCreatorOrAssignee(creator);

        assertEquals(1, results.size());
    }
}
