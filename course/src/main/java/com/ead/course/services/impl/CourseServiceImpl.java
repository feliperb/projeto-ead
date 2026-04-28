package com.ead.course.services.impl;

import com.ead.course.models.CourseModel;
import com.ead.course.models.LessonModel;
import com.ead.course.models.ModuleModel;
import com.ead.course.repositories.CourseRepository;
import com.ead.course.repositories.LessonRepository;
import com.ead.course.repositories.ModuleRepository;
import com.ead.course.services.CourseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
}
