package com.ead.course.repositories;

import com.ead.course.enums.CourseLevel;
import com.ead.course.enums.CourseStatus;
import com.ead.course.models.CourseModel;
import com.ead.course.models.LessonModel;
import com.ead.course.models.ModuleModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class LessonRepositoryTest {

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private CourseRepository courseRepository;

    private CourseModel createCourse() {
        CourseModel course = new CourseModel();
        course.setName("Curso Teste");
        course.setDescription("Desc");
        course.setImageUrl("http://img");
        course.setCourseLevel(CourseLevel.BEGINNER); // ajuste
        course.setCourseStatus(CourseStatus.IN_PROGRESS); // ajuste
        course.setCreationDate(LocalDateTime.now());
        course.setLastUpdateDate(LocalDateTime.now());
        course.setUserInstructor(UUID.randomUUID());

        return courseRepository.save(course);
    }

    private ModuleModel createModule(CourseModel course, String title) {
        ModuleModel module = new ModuleModel();
        module.setTitle(title);
        module.setDescription("Desc module");
        module.setCreationDate(LocalDateTime.now());
        module.setCourse(course);

        return moduleRepository.save(module);
    }

    private void createLesson(ModuleModel module, String title) {
        LessonModel lesson = new LessonModel();
        lesson.setTitle(title);
        lesson.setDescription("Desc lesson");
        lesson.setCreationDate(LocalDateTime.now());
        lesson.setVideoUrl("http://video.com/teste"); // 👈 FALTAVA ISSO
        lesson.setModule(module);

        lessonRepository.save(lesson);
    }

    @Test
    void findByModule_ModuleId_success() {
        CourseModel course = createCourse();
        ModuleModel module = createModule(course, "Module Test");

        createLesson(module, "Lesson 1");

        List<LessonModel> result =
                lessonRepository.findByModule_ModuleId(module.getModuleId());

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Lesson 1", result.getFirst().getTitle());
    }

    @Test
    @DisplayName("findByModule_ModuleIdIn should return lessons from multiple modules")
    void findByModule_ModuleIdIn_success() {
        CourseModel course = createCourse();

        ModuleModel module1 = createModule(course, "Module 1");
        ModuleModel module2 = createModule(course, "Module 2");

        createLesson(module1, "Lesson 1");
        createLesson(module2, "Lesson 2");

        List<LessonModel> result =
                lessonRepository.findByModule_ModuleIdIn(
                        List.of(module1.getModuleId(), module2.getModuleId())
                );

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("findByModule_ModuleId should return empty list when no lessons")
    void findByModule_ModuleId_empty() {
        UUID randomId = UUID.randomUUID();

        List<LessonModel> result =
                lessonRepository.findByModule_ModuleId(randomId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void existsByTitle_success() {
        CourseModel course = createCourse();
        ModuleModel module = createModule(course, "Module Test");

        createLesson(module, "Lesson 1");

        boolean exists = lessonRepository.existsByTitle("Lesson 1");

        assertTrue(exists);
    }

    @Test
    void existsByModule_ModuleId_success() {
        CourseModel course = createCourse();
        ModuleModel module = createModule(course, "Module Test");

        createLesson(module, "Lesson 1");

        boolean exists =
                lessonRepository.existsByModule_ModuleId(module.getModuleId());

        assertTrue(exists);
    }
}