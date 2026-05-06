package com.ead.course.repositories;

import com.ead.course.models.LessonModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LessonRepository extends JpaRepository<LessonModel, UUID> {

    List<LessonModel> findByModule_ModuleId(UUID moduleId);

    List<LessonModel> findByModule_ModuleIdIn(List<UUID> moduleIds);

    boolean existsByTitle(String title);

    boolean existsByModule_ModuleId(UUID moduleId);

    boolean existsByTitleAndModule_ModuleId(String title, UUID moduleId);
}