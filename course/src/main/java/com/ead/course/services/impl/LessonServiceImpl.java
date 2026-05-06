package com.ead.course.services.impl;

import com.ead.course.dtos.LessonRecordDto;
import com.ead.course.exceptions.ConflictException;
import com.ead.course.exceptions.NotFoundException;
import com.ead.course.models.LessonModel;
import com.ead.course.models.ModuleModel;
import com.ead.course.repositories.LessonRepository;
import com.ead.course.services.LessonService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;

    public LessonServiceImpl(LessonRepository lessonRepository) {
        this.lessonRepository = lessonRepository;
    }

    @Transactional
    @Override
    public LessonModel create(LessonRecordDto dto, ModuleModel module) {
        validateLessonNameAvailability(dto.title());
        LessonModel lesson = buildEntity(dto, module);
        return lessonRepository.save(lesson);
    }

    @Override
    public List<LessonModel> getAllLessons(UUID moduleId) {
        return lessonRepository.findByModule_ModuleId(moduleId);
    }

    @Override
    public LessonModel getById(UUID lessonId) {
        return findLessonOrThrow(lessonId);
    }

    @Transactional
    @Override
    public LessonModel updateById(UUID lessonId, LessonRecordDto dto) {
        LessonModel lesson = findLessonOrThrow(lessonId);
        validateNameChange(dto.title(), lesson);
        applyUpdates(lesson, dto);
        return lessonRepository.save(lesson);
    }

    @Transactional
    @Override
    public void deleteById(UUID lessonId) {
        LessonModel lesson = findLessonOrThrow(lessonId);
        lessonRepository.delete(lesson);
    }

    // =========================
    // PRIVATE METHODS
    // =========================

    private LessonModel findLessonOrThrow(UUID lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new NotFoundException("Lesson not found with id: " + lessonId));
    }

    private void validateLessonNameAvailability(String title) {
        if (lessonRepository.existsByTitle(title)) {
            throw new ConflictException("Lesson title is already taken: " + title);
        }
    }

    private void validateNameChange(String newTitle, LessonModel lesson) {
        boolean nameChanged = !lesson.getTitle().equals(newTitle);

        if (nameChanged && lessonRepository.existsByTitle(newTitle)) {
            throw new ConflictException("Lesson title is already taken: " + newTitle);
        }
    }

    private LessonModel buildEntity(LessonRecordDto dto, ModuleModel module) {
        LessonModel lesson = new LessonModel();

        lesson.setTitle(dto.title());
        lesson.setDescription(dto.description());
        lesson.setVideoUrl(dto.videoUrl());
        lesson.setModule(module);
        lesson.setCreationDate(LocalDateTime.now(ZoneOffset.UTC));

        return lesson;
    }

    private void applyUpdates(LessonModel lesson, LessonRecordDto dto) {
        lesson.setTitle(dto.title());
        lesson.setDescription(dto.description());
        lesson.setVideoUrl(dto.videoUrl());
    }
}