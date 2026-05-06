package com.ead.course.repositories;

import com.ead.course.enums.CourseLevel;
import com.ead.course.enums.CourseStatus;
import com.ead.course.models.CourseModel;
import com.ead.course.models.ModuleModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ModuleRepositoryTest {

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private CourseRepository courseRepository;

    private CourseModel createCourse() {
        CourseModel course = new CourseModel();
        course.setName("Curso Teste");
        course.setDescription("Desc");
        course.setImageUrl("http://img");
        course.setCourseLevel(CourseLevel.BEGINNER);
        course.setCourseStatus(CourseStatus.IN_PROGRESS);
        course.setCreationDate(LocalDateTime.now());
        course.setLastUpdateDate(LocalDateTime.now());
        course.setUserInstructor(UUID.randomUUID());

        return courseRepository.save(course);
    }

    private void createModule(CourseModel course, String title) {
        ModuleModel module = new ModuleModel();
        module.setTitle(title);
        module.setDescription("Desc module");
        module.setCreationDate(LocalDateTime.now());
        module.setCourse(course);

        moduleRepository.save(module);
    }

    @Test
    void existsByTitle_success() {
        CourseModel course = createCourse();
        createModule(course, "Modulo 1");

        boolean exists = moduleRepository.existsByTitle("Modulo 1");

        assertTrue(exists);
    }

    @Test
    void existsByCourseId_success() {
        CourseModel course = createCourse();
        createModule(course, "Modulo 1");

        boolean exists = moduleRepository.existsByCourse_CourseId(course.getCourseId());

        assertTrue(exists);
    }

    @Test
    void findAllModulesIntoCourse_success() {
        CourseModel course = createCourse();
        createModule(course, "Modulo 1");
        createModule(course, "Modulo 2");

        var result = moduleRepository.findAllModulesIntoCourse(course.getCourseId());

        assertEquals(2, result.size());
    }

    @Test
    void findAllModulesIntoCourse_empty() {
        UUID randomId = UUID.randomUUID();

        var result = moduleRepository.findAllModulesIntoCourse(randomId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}