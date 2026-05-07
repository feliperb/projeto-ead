package com.ead.course.services.impl;

import com.ead.course.dtos.LessonRecordDto;
import com.ead.course.exceptions.BusinessException;
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
import java.util.Objects;
import java.util.Optional;
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
        validateModule(module);
        validateLessonNameAvailability(dto.title(), module.getModuleId());
        LessonModel lesson = buildEntity(dto, module);
        return lessonRepository.save(lesson);
    }

    @Override
    public List<LessonModel> getAllLessons(UUID moduleId) {
        if (moduleId == null) {
            throw new BusinessException("moduleId cannot be null");
        }
        return Optional.ofNullable(lessonRepository.findByModule_ModuleId(moduleId)).orElse(List.of());
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

    private void validateModule(ModuleModel module) {
        if (module == null) {throw new BusinessException("Module cannot be null");}
        if (module.getModuleId() == null) {throw new BusinessException("Module ID cannot be null");}
        if (module.getCourse() == null || module.getCourse().getCourseId() == null) {throw new BusinessException("Module must be associated with a valid course");}
    }

    private LessonModel findLessonOrThrow(UUID lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new NotFoundException("Lesson not found with id: " + lessonId));
    }

    private void validateLessonNameAvailability(String title, UUID moduleId) {
        if (lessonRepository.existsByTitleAndModule_ModuleId(title, moduleId)) {
            throw new ConflictException("Lesson title is already taken: " + title);
        }
    }

    private void validateNameChange(String newTitle, LessonModel lesson) {
        boolean nameChanged = !Objects.equals(lesson.getTitle(), newTitle);
        if (nameChanged && lessonRepository.existsByTitleAndModule_ModuleId(newTitle, getModuleId(lesson))) {
            throw new ConflictException("Lesson title is already taken: " + newTitle);
        }
    }

    private UUID getModuleId(LessonModel lesson) {
        if (lesson.getModule() == null) {   throw new BusinessException("Lesson must be associated with a module"); }
        return lesson.getModule().getModuleId();
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