package com.ead.course.controllers;

import com.ead.course.dtos.LessonRecordDto;
import com.ead.course.models.LessonModel;
import com.ead.course.services.LessonService;
import com.ead.course.services.ModuleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
        var module = moduleService.getById(moduleId);
        LessonModel lesson = lessonService.create(dto, module);
        return ResponseEntity.status(HttpStatus.CREATED).body(lesson);
    }

    @GetMapping("/modules/{moduleId}")
    public ResponseEntity<List<LessonModel>> getAllLessons(@PathVariable UUID moduleId) {
        return ResponseEntity.ok(lessonService.getAllLessons(moduleId));
    }

    @GetMapping("/{lessonId}")
    public ResponseEntity<LessonModel> getLessonById(@PathVariable UUID lessonId) {
        return ResponseEntity.ok(lessonService.getById(lessonId));
    }

    @PutMapping("/{lessonId}")
    public ResponseEntity<LessonModel> updateLesson(@PathVariable UUID lessonId, @RequestBody @Valid LessonRecordDto dto) {
        return ResponseEntity.ok(lessonService.updateById(lessonId, dto));
    }

    @DeleteMapping("/{lessonId}")
    public ResponseEntity<Void> deleteLesson(@PathVariable UUID lessonId) {
        lessonService.deleteById(lessonId);
        return ResponseEntity.noContent().build();
    }
}
