package com.ead.course.services.impl;

import com.ead.course.dtos.CourseRecordDto;
import com.ead.course.exceptions.BusinessException;
import com.ead.course.exceptions.ConflictException;
import com.ead.course.exceptions.NotFoundException;
import com.ead.course.models.CourseModel;
import com.ead.course.repositories.CourseRepository;
import com.ead.course.repositories.ModuleRepository;
import com.ead.course.services.CourseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;

    public CourseServiceImpl(CourseRepository courseRepository, ModuleRepository moduleRepository) {
        this.courseRepository = courseRepository;
        this.moduleRepository = moduleRepository;
    }

    @Transactional
    @Override
    public CourseModel create(CourseRecordDto dto) {
        log.info("Creating course with name: {}", dto.name());
        validateDto(dto);
        validateUniqueName(dto.name());
        CourseModel course = buildEntity(dto);
        CourseModel saved = courseRepository.save(course);
        log.info("Course created successfully with ID: {}", saved.getCourseId());
        return saved;
    }

    @Override
    public Page<CourseModel> getAllCourses(Specification<CourseModel> spec, Pageable pageable) {
        if (pageable == null) {
            throw new BusinessException("pageable cannot be null");
        }
        log.debug("Getting courses with pageable: {}", pageable);
        Page<CourseModel> courses = courseRepository.findAll(spec, pageable);
        log.debug("Retrieved {} courses", courses.getTotalElements());
        return courses;
    }

    @Override
    public CourseModel getById(UUID courseId) {
        log.debug("Getting course by ID: {}", courseId);
        CourseModel course = findCourseOrThrow(courseId);
        log.debug("Course found: {}", course.getName());
        return course;
    }

    @Transactional
    @Override
    public CourseModel updateById(UUID courseId, CourseRecordDto dto) {
        log.info("Updating course ID: {} with name: {}", courseId, dto.name());
        validateDto(dto);
        CourseModel course = findCourseOrThrow(courseId);
        validateUniqueNameOnUpdate(dto.name(), course);
        applyUpdates(course, dto);
        course.setLastUpdateDate(now());
        CourseModel updated = courseRepository.save(course);
        log.info("Course updated successfully: {}", updated.getName());
        return updated;
    }

    @Transactional
    @Override
    public void deleteById(UUID courseId) {
        log.info("Deleting course ID: {}", courseId);
        CourseModel course = findCourseOrThrow(courseId);
        validateCourseDeletion(courseId);
        courseRepository.delete(course);
        log.info("Course deleted successfully");
    }

    // =========================
    // PRIVATE METHODS
    // =========================

    private void validateId(UUID id) {
        if (id == null) {
            throw new BusinessException("courseId" + " cannot be null");
        }
    }

    private void validateDto(CourseRecordDto dto) {
        if (dto == null) {
            throw new BusinessException("Course dto cannot be null");
        }
    }

    private CourseModel findCourseOrThrow(UUID courseId) {
        validateId(courseId);
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
    }

    private void validateUniqueName(String name) {
        if (courseRepository.existsByName(name)) {
            throw new ConflictException("Course name is already taken: " + name);
        }
    }

    private void validateUniqueNameOnUpdate(String newName, CourseModel course) {
        boolean nameChanged = !Objects.equals(course.getName(), newName);
        if (nameChanged && courseRepository.existsByName(newName)) {throw new ConflictException("Course name is already taken: " + newName);}
    }

    private void validateCourseDeletion(UUID courseId) {
        boolean hasModules = moduleRepository.existsByCourse_CourseId(courseId);
        if (hasModules) {
            throw new ConflictException("Course cannot be deleted because it has modules");
        }
    }

    private CourseModel buildEntity(CourseRecordDto dto) {
        LocalDateTime now = now();

        CourseModel course = new CourseModel();
        course.setName(dto.name());
        course.setDescription(dto.description());
        course.setCourseStatus(dto.courseStatus());
        course.setCourseLevel(dto.courseLevel());
        course.setUserInstructor(dto.userInstructor());
        course.setImageUrl(dto.imageUrl());
        course.setCreationDate(now);
        course.setLastUpdateDate(now);

        return course;
    }

    private void applyUpdates(CourseModel course, CourseRecordDto dto) {
        course.setName(dto.name());
        course.setDescription(dto.description());
        course.setCourseStatus(dto.courseStatus());
        course.setCourseLevel(dto.courseLevel());
        course.setUserInstructor(dto.userInstructor());
        course.setImageUrl(dto.imageUrl());
    }

    private LocalDateTime now() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }
}