package com.ead.course.services;

import com.ead.course.dtos.LessonRecordDto;
import com.ead.course.models.LessonModel;
import com.ead.course.models.ModuleModel;

import java.util.List;
import java.util.UUID;

public interface LessonService {
    LessonModel create(LessonRecordDto lessonRecordDto, ModuleModel moduleModel);
    List<LessonModel> getAllLessons(UUID moduleId);
    LessonModel getById(UUID lessonId);
    LessonModel updateById(UUID lessonId, LessonRecordDto lessonRecordDto);
    void deleteById(UUID lessonId);
}