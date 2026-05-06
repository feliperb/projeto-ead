package com.ead.course.services.impl;

import com.ead.course.dtos.CourseRecordDto;
import com.ead.course.exceptions.ConflictException;
import com.ead.course.exceptions.NotFoundException;
import com.ead.course.models.CourseModel;
import com.ead.course.repositories.CourseRepository;
import com.ead.course.services.CourseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    public CourseServiceImpl(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Transactional
    @Override
    public CourseModel create(CourseRecordDto dto) {
        validateUniqueName(dto.name());
        CourseModel course = buildEntity(dto);
        return courseRepository.save(course);
    }

    @Override
    public List<CourseModel> getAllCourses() {
        return courseRepository.findAll(); // considerar paginação futuramente
    }

    @Override
    public CourseModel getById(UUID courseId) {
        return findCourseOrThrow(courseId);
    }

    @Transactional
    @Override
    public CourseModel updateById(UUID courseId, CourseRecordDto dto) {
        CourseModel course = findCourseOrThrow(courseId);
        validateUniqueNameOnUpdate(dto.name(), course);
        applyUpdates(course, dto);
        course.setLastUpdateDate(now());
        return courseRepository.save(course);
    }

    @Transactional
    @Override
    public void deleteById(UUID courseId) {
        CourseModel course = findCourseOrThrow(courseId);
        validateCourseDeletion(courseId);
        courseRepository.delete(course);
    }

    // =========================
    // PRIVATE METHODS
    // =========================

    private CourseModel findCourseOrThrow(UUID courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
    }

    private void validateUniqueName(String name) {
        if (courseRepository.existsByName(name)) {
            throw new ConflictException("Course name is already taken: " + name);
        }
    }

    private void validateUniqueNameOnUpdate(String newName, CourseModel course) {
        if (!course.getName().equals(newName) &&
                courseRepository.existsByName(newName)) {
            throw new ConflictException("Course name is already taken: " + newName);
        }
    }

    private void validateCourseDeletion(UUID courseId) {
        boolean hasModules = courseRepository.existsByCourseId(courseId);
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