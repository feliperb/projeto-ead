package com.ead.course.services.impl;

import com.ead.course.models.LessonModel;
import com.ead.course.repositories.LessonRepository;
import com.ead.course.services.LessonService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class LessonServiceImpl implements LessonService {

    final LessonRepository lessonRepository;

    public LessonServiceImpl(LessonRepository lessonRepository) {
        this.lessonRepository = lessonRepository;
    }

    @Transactional
    @Override
    public void delete(LessonModel lessonModel) {
        if (lessonModel == null) {
            throw new IllegalArgumentException("lessonModel não pode ser null");
        }
        lessonRepository.delete(lessonModel);
    }

    @Transactional
    @Override
    public void deleteById(UUID lessonId) {
        var lessonModel = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found with id: " + lessonId));
        delete(lessonModel);
    }
}


