package com.ead.course.controllers;

import com.ead.course.dtos.ModuleRecordDto;
import com.ead.course.models.ModuleModel;
import com.ead.course.services.CourseService;
import com.ead.course.services.ModuleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/modules")
public class ModuleController {

    final ModuleService moduleService;
    final CourseService courseService;

    public ModuleController(ModuleService moduleService, CourseService courseService) {
        this.moduleService = moduleService;
        this.courseService = courseService;
    }

    @PostMapping("/courses/{courseId}")
    public ResponseEntity<ModuleModel> createModule(@PathVariable UUID courseId, @RequestBody @Valid ModuleRecordDto dto) {
        var course = courseService.getById(courseId);
        ModuleModel module = moduleService.create(dto, course);
        return ResponseEntity.status(HttpStatus.CREATED).body(module);
    }

    @GetMapping("/courses/{courseId}")
    public ResponseEntity<List<ModuleModel>> getAllModules(@PathVariable UUID courseId) {
        return ResponseEntity.ok(moduleService.getAllModules(courseId));
    }

    @GetMapping("/{moduleId}")
    public ResponseEntity<ModuleModel> getModuleById(@PathVariable UUID moduleId) {
        return ResponseEntity.ok(moduleService.getById(moduleId));
    }

    @PutMapping("/{moduleId}")
    public ResponseEntity<ModuleModel> updateModule(@PathVariable UUID moduleId, @RequestBody @Valid ModuleRecordDto dto) {
        return ResponseEntity.ok(moduleService.updateById(moduleId, dto));
    }

    @DeleteMapping("/{moduleId}")
    public ResponseEntity<Void> deleteModule(@PathVariable UUID moduleId) {
        moduleService.deleteById(moduleId);
        return ResponseEntity.noContent().build();
    }
}
