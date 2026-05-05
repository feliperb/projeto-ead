package com.ead.course.services.impl;

import com.ead.course.dtos.CourseRecordDto;
import com.ead.course.exceptions.ConflictException;
import com.ead.course.exceptions.NotFoundException;
import com.ead.course.models.CourseModel;
import com.ead.course.repositories.CourseRepository;
import com.ead.course.repositories.LessonRepository;
import com.ead.course.repositories.ModuleRepository;
import com.ead.course.services.CourseService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CourseServiceImpl implements CourseService {

    final CourseRepository courseRepository;
    final ModuleRepository moduleRepository;
    final LessonRepository lessonRepository;

    public CourseServiceImpl(CourseRepository courseRepository, ModuleRepository moduleRepository, LessonRepository lessonRepository) {
        this.courseRepository = courseRepository;
        this.moduleRepository = moduleRepository;
        this.lessonRepository = lessonRepository;
    }

    @Transactional
    @Override
    public CourseModel create(CourseRecordDto courseRecordDto) {
        validateNameAvailability(courseRecordDto);
        var courseModel = mapToEntity(courseRecordDto);
        setAuditFields(courseModel);
        return courseRepository.save(courseModel);
    }

    @Override
    public List<CourseModel> getAllCourses() {
        return Optional.of(courseRepository.findAll())
                .orElse(List.of());
    }

    @Override
    public CourseModel getById(UUID courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
    }

    @Transactional
    @Override
    public CourseModel updateById(UUID courseId, CourseRecordDto courseRecordDto) {
        var courseModel = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        return update(courseRecordDto, courseModel);
    }

    @Transactional
    @Override
    public void deleteById(UUID courseId) {
        CourseModel course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        boolean hasModules = moduleRepository.existsByCourse_CourseId(courseId);
        if (hasModules) {   throw new ConflictException("Cannot delete course because it has associated modules");  }
        courseRepository.delete(course);
    }


    // PRIVATES METHODS

    private void validateNameAvailability(CourseRecordDto courseRecordDto) {
        if (courseRepository.existsByName(courseRecordDto.name())) {
            throw new ConflictException("Course name is already taken: " + courseRecordDto.name());
        }
    }

    private CourseModel mapToEntity(CourseRecordDto courseRecordDto) {
        var courseModel = new CourseModel();
        BeanUtils.copyProperties(courseRecordDto, courseModel);
        return courseModel;
    }

    private void setAuditFields(CourseModel courseModel) {
        var now = LocalDateTime.now(ZoneId.of("UTC"));
        courseModel.setCreationDate(now);
        courseModel.setLastUpdateDate(now);
    }

    private CourseModel update(CourseRecordDto courseRecordDto, CourseModel courseModel) {
        BeanUtils.copyProperties(courseRecordDto, courseModel);
        courseModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
        return courseRepository.save(courseModel);
    }
}
