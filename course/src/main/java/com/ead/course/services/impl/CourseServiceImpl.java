package com.ead.course.services.impl;

import com.ead.course.dtos.CourseRecordDto;
import com.ead.course.models.CourseModel;
import com.ead.course.models.LessonModel;
import com.ead.course.models.ModuleModel;
import com.ead.course.repositories.CourseRepository;
import com.ead.course.repositories.LessonRepository;
import com.ead.course.repositories.ModuleRepository;
import com.ead.course.services.CourseService;
import jakarta.validation.Valid;
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
    public void delete(CourseModel courseModel) {
        if (courseModel == null) {  throw new IllegalArgumentException("courseModel não pode ser null");    }
        List<ModuleModel> moduleModels = moduleRepository.findAllModulesIntoCourse(courseModel.getCourseId());
        if (moduleModels != null && !moduleModels.isEmpty()) {
            List<UUID> moduleIds = moduleModels.stream().map(ModuleModel::getModuleId).toList();
            List<LessonModel> lessons = lessonRepository.findAllLessonsIntoModules(moduleIds);
            if (lessons != null && !lessons.isEmpty()) {
                lessonRepository.deleteAll(lessons);
            }
            moduleRepository.deleteAll(moduleModels);
        }
        courseRepository.delete(courseModel);
    }

    @Transactional
    @Override
    public void deleteById(UUID courseId) {
        var courseModel = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with id: " + courseId));
        delete(courseModel);
    }

    @Override
    public CourseModel save(CourseRecordDto courseRecordDto) {
        var courseModel = new CourseModel();
        BeanUtils.copyProperties(courseRecordDto, courseModel);
        courseModel.setCreationDate(LocalDateTime.now(ZoneId.of("UTC")));
        courseModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
        return courseRepository.save(courseModel);
    }

    @Transactional
    @Override
    public CourseModel saveIfNotExists(@Valid CourseRecordDto courseRecordDto) {
        if (existsByName(courseRecordDto.name())) {
            throw new IllegalArgumentException("Course name is already taken: " + courseRecordDto.name());
        }
        return save(courseRecordDto);
    }

    @Override
    public boolean existsByName(String name) {
        return courseRepository.existsByName(name);
    }

    @Override
    public List<CourseModel> findAll() {
        return courseRepository.findAll();
    }

    @Override
    public Optional<CourseModel> findById(UUID courseId) {
        return courseRepository.findById(courseId);
    }

    @Override
    public CourseModel getByIdOrThrow(UUID courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with id: " + courseId));
    }

    @Override
    public CourseModel update(CourseRecordDto courseRecordDto, CourseModel courseModel) {
        BeanUtils.copyProperties(courseRecordDto, courseModel);
        courseModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
        return courseRepository.save(courseModel);
    }

    @Transactional
    @Override
    public CourseModel updateById(@Valid CourseRecordDto courseRecordDto, UUID courseId) {
        var courseModel = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with id: " + courseId));
        return update(courseRecordDto, courseModel);
    }
}
