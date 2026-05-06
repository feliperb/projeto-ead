package com.ead.course.repositories;

import com.ead.course.models.CourseModel;
import com.ead.course.enums.CourseLevel;
import com.ead.course.enums.CourseStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CourseRepositoryTest {

    @Autowired
    private CourseRepository courseRepository;

    @Test
    @DisplayName("existsByName should return true when course exists")
    void existsByName_success() {
        CourseModel course = createValidCourse();
        course.setName("Java Course");

        courseRepository.save(course);

        boolean exists = courseRepository.existsByName("Java Course");

        assertTrue(exists);
    }

    @Test
    @DisplayName("existsByName should return false when course does not exist")
    void existsByName_false() {
        boolean exists = courseRepository.existsByName("Nonexistent");

        assertFalse(exists);
    }

    @Test
    @DisplayName("existsByCourseId should return true when course exists")
    void existsByCourseId_success() {
        CourseModel course = createValidCourse();

        course = courseRepository.save(course);

        boolean exists = courseRepository.existsByCourseId(course.getCourseId());

        assertTrue(exists);
    }

    @Test
    @DisplayName("existsByCourseId should return false when course does not exist")
    void existsByCourseId_false() {
        UUID randomId = UUID.randomUUID();

        boolean exists = courseRepository.existsByCourseId(randomId);

        assertFalse(exists);
    }

    // 🔥 Método helper pra evitar repetir setup
    private CourseModel createValidCourse() {
        CourseModel course = new CourseModel();
        course.setName("Curso Teste");
        course.setDescription("Descrição");
        course.setImageUrl("http://image.com");
        course.setCourseLevel(CourseLevel.BEGINNER); // obrigatório
        course.setCourseStatus(CourseStatus.IN_PROGRESS); // obrigatório
        course.setUserInstructor(UUID.randomUUID());
        course.setCreationDate(LocalDateTime.now());
        course.setLastUpdateDate(LocalDateTime.now());
        return course;
    }
}