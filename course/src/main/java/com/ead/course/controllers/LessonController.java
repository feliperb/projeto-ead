package com.ead.course.controllers;

import com.ead.course.dtos.LessonRecordDto;
import com.ead.course.models.LessonModel;
import com.ead.course.services.LessonService;
import com.ead.course.services.ModuleService;
import com.ead.course.specification.SpecificationTemplate;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/lessons")
public class LessonController {

    final ModuleService moduleService;
    final LessonService lessonService;

    public LessonController(ModuleService moduleService, LessonService lessonService) {
        this.moduleService = moduleService;
        this.lessonService = lessonService;
    }

    @PostMapping("/modules/{moduleId}")
    public ResponseEntity<LessonModel> createLesson(@PathVariable UUID moduleId, @RequestBody @Valid LessonRecordDto dto) {
        log.info("Creating lesson '{}' for module ID: {}", dto.title(), moduleId);
        var module = moduleService.getById(moduleId);
        LessonModel lesson = lessonService.create(dto, module);
        log.info("Lesson created successfully with ID: {}", lesson.getLessonId());
        return ResponseEntity.status(HttpStatus.CREATED).body(lesson);
    }

    @GetMapping("/modules/{moduleId}")
    public ResponseEntity<Page<LessonModel>> getAllLessons(@PathVariable UUID moduleId,
                                                           SpecificationTemplate.LessonSpec spec,
                                                           Pageable pageable) {
        log.debug("Getting lessons for module ID: {} with pageable: {}", moduleId, pageable);
        Page<LessonModel> lessons = lessonService.getAllLessonsIntoModule(SpecificationTemplate.lessonModuleId(moduleId).and(spec), pageable);
        log.debug("Retrieved {} lessons for module", lessons.getTotalElements());
        return ResponseEntity.ok(lessons);
    }

    @GetMapping("/{lessonId}")
    public ResponseEntity<LessonModel> getLessonById(@PathVariable UUID lessonId) {
        log.debug("Getting lesson by ID: {}", lessonId);
        LessonModel lesson = lessonService.getById(lessonId);
        log.debug("Lesson found: {}", lesson.getTitle());
        return ResponseEntity.ok(lesson);
    }

    @PutMapping("/{lessonId}")
    public ResponseEntity<LessonModel> updateLesson(@PathVariable UUID lessonId, @RequestBody @Valid LessonRecordDto dto) {
        log.info("Updating lesson ID: {} with title: {}", lessonId, dto.title());
        LessonModel lesson = lessonService.updateById(lessonId, dto);
        log.info("Lesson updated successfully: {}", lesson.getTitle());
        return ResponseEntity.ok(lesson);
    }

    @DeleteMapping("/{lessonId}")
    public ResponseEntity<Void> deleteLesson(@PathVariable UUID lessonId) {
        log.info("Deleting lesson ID: {}", lessonId);
        lessonService.deleteById(lessonId);
        log.info("Lesson deleted successfully");
        return ResponseEntity.noContent().build();
    }
}
