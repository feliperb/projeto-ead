package com.ead.course.repositories;

import com.ead.course.models.LessonModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LessonRepository extends JpaRepository<LessonModel, UUID> {

    @Query(value="SELECT * FROM tb_lessons WHERE module_module_id = :moduleId", nativeQuery = true)
    List<LessonModel> findAllLessonsIntoModule(@Param("moduleId") UUID moduleId);

    @Query(value = "SELECT * FROM tb_lessons WHERE module_module_id IN :moduleIds", nativeQuery = true)
    List<LessonModel> findAllLessonsIntoModules(@Param("moduleIds") List<UUID> moduleIds);

    @Query(value="SELECT * FROM tb_lessons WHERE lesson_id = :lessonId", nativeQuery = true)
    Optional<LessonModel> findByLessonId(@Param("lessonId") UUID lessonId);

    boolean existsByTitle(String name);

    boolean existsByModule_ModuleId(UUID moduleId);
}
