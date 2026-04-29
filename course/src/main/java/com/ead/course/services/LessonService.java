package com.ead.course.services;

import com.ead.course.models.LessonModel;

import java.util.UUID;

public interface LessonService {

    void delete(LessonModel lessonModel);

    void deleteById(UUID lessonId);
}


