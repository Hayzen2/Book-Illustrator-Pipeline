package com.example.BookIllustrator.repository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.example.BookIllustrator.entity.Project;
import com.example.BookIllustrator.entity.ProjectStep;
import com.example.BookIllustrator.entity.User;
import com.example.BookIllustrator.enums.GlobalStatus;
import com.example.BookIllustrator.enums.StepName;
import com.example.BookIllustrator.enums.StepStatus;

@DataJpaTest
class ProjectStepRepositoryTest {

    @Autowired
    private ProjectStepRepository projectStepRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void claimStepForExecution_returnsOneWhenPending() {
        ProjectStep step = createProjectStep(StepStatus.PENDING, StepName.STYLE);

        int updated = projectStepRepository.claimStepForExecution(step.getProject().getId(), StepName.STYLE);

        assertEquals(1, updated);
    }

    @Test
    void claimStepForExecution_returnsZeroWhenNotPendingOrFailed() {
        ProjectStep step = createProjectStep(StepStatus.COMPLETED, StepName.STYLE);

        int updated = projectStepRepository.claimStepForExecution(step.getProject().getId(), StepName.STYLE);

        assertEquals(0, updated);
    }

    private ProjectStep createProjectStep(StepStatus status, StepName stepName) {
        User user = new User();
        user.setName("tester");
        user.setEmail("tester@example.com");
        user = userRepository.save(user);

        Project project = new Project();
        project.setTitle("Project");
        project.setBookFilePath("/tmp/book.txt");
        project.setStatus(GlobalStatus.DRAFT);
        project.setUser(user);
        project = projectRepository.save(project);

        ProjectStep step = new ProjectStep();
        step.setProject(project);
        step.setStepName(stepName);
        step.setStatus(status);
        step.setUpdatedAt(LocalDateTime.now().minusMinutes(5));
        return projectStepRepository.save(step);
    }
}
