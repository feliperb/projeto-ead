package com.ead.course.controllers;

import com.ead.course.dtos.ModuleRecordDto;
import com.ead.course.models.ModuleModel;
import com.ead.course.services.CourseService;
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
        log.info("Creating module '{}' for course ID: {}", dto.title(), courseId);
        var course = courseService.getById(courseId);
        ModuleModel module = moduleService.create(dto, course);
        log.info("Module created successfully with ID: {}", module.getModuleId());
        return ResponseEntity.status(HttpStatus.CREATED).body(module);
    }

    @GetMapping("/courses/{courseId}")
    public ResponseEntity<Page<ModuleModel>> getAllModules(@PathVariable UUID courseId,
                                                           SpecificationTemplate.ModuleSpec spec,
                                                           Pageable pageable) {
        log.debug("Getting modules for course ID: {} with pageable: {}", courseId, pageable);
        Page<ModuleModel> modules = moduleService.getAllModulesIntoCourse(SpecificationTemplate.moduleCourseId(courseId).and(spec), pageable);
        log.debug("Retrieved {} modules for course", modules.getTotalElements());
        return ResponseEntity.ok(modules);
    }

    @GetMapping("/{moduleId}")
    public ResponseEntity<ModuleModel> getModuleById(@PathVariable UUID moduleId) {
        log.debug("Getting module by ID: {}", moduleId);
        ModuleModel module = moduleService.getById(moduleId);
        log.debug("Module found: {}", module.getTitle());
        return ResponseEntity.ok(module);
    }

    @PutMapping("/{moduleId}")
    public ResponseEntity<ModuleModel> updateModule(@PathVariable UUID moduleId, @RequestBody @Valid ModuleRecordDto dto) {
        log.info("Updating module ID: {} with title: {}", moduleId, dto.title());
        ModuleModel module = moduleService.updateById(moduleId, dto);
        log.info("Module updated successfully: {}", module.getTitle());
        return ResponseEntity.ok(module);
    }

    @DeleteMapping("/{moduleId}")
    public ResponseEntity<Void> deleteModule(@PathVariable UUID moduleId) {
        log.info("Deleting module ID: {}", moduleId);
        moduleService.deleteById(moduleId);
        log.info("Module deleted successfully");
        return ResponseEntity.noContent().build();
    }
}
