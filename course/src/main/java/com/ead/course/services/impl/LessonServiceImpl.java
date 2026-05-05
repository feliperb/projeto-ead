package com.ead.course.services.impl;

import com.ead.course.dtos.LessonRecordDto;
import com.ead.course.exceptions.ConflictException;
import com.ead.course.exceptions.NotFoundException;
import com.ead.course.models.LessonModel;
import com.ead.course.models.ModuleModel;
import com.ead.course.repositories.LessonRepository;
import com.ead.course.services.LessonService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
public class LessonServiceImpl implements LessonService {

    final LessonRepository lessonRepository;

    public LessonServiceImpl(LessonRepository lessonRepository) {
        this.lessonRepository = lessonRepository;
    }

    @Override
    public LessonModel create(LessonRecordDto lessonRecordDto, ModuleModel moduleModel) {
        validateNameAvailability(lessonRecordDto.title());
        var lessonModel = mapToEntity(lessonRecordDto, moduleModel);
        setAuditFields(lessonModel);
        return lessonRepository.save(lessonModel);
    }

    @Override
    public List<LessonModel> getAllLessons(UUID moduleId) {
        List<LessonModel> lessons = lessonRepository.findAllLessonsIntoModule(moduleId);
        return lessons != null ? lessons : List.of();
    }

    @Override
    public LessonModel getById(UUID lessonId) {
        return lessonRepository.findByLessonId(lessonId)
                .orElseThrow(() -> new NotFoundException("Lesson not found with id: " + lessonId));
    }

    @Transactional
    @Override
    public LessonModel updateById(UUID lessonId, LessonRecordDto lessonRecordeDto) {
        var lessonModel = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new NotFoundException("Lesson not found with id: " + lessonId));
        return update(lessonRecordeDto, lessonModel);
    }

    @Transactional
    @Override
    public void deleteById(UUID lessonId) {
        var lessonModel = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new NotFoundException("Lesson not found with id: " + lessonId));
        delete(lessonModel);
    }



    // PRIVATE METHODS

    private void validateNameAvailability(String name) {
        if (lessonRepository.existsByTitle(name)) {
            throw new ConflictException("Lesson title is already taken: " + name);
        }
    }

    private LessonModel mapToEntity(LessonRecordDto lessonRecordDto, ModuleModel moduleModel) {
        var lessonModel = new LessonModel();
        BeanUtils.copyProperties(lessonRecordDto, lessonModel);
        lessonModel.setModule(moduleModel);
        return lessonModel;
    }

    private void setAuditFields(LessonModel lessonModel) {
        var now = LocalDateTime.now(ZoneId.of("UTC"));
        lessonModel.setCreationDate(now);
    }

    private LessonModel update(LessonRecordDto lessonRecordDto, LessonModel lessonModel) {
        BeanUtils.copyProperties(lessonRecordDto, lessonModel);
        return lessonRepository.save(lessonModel);
    }

    @Transactional
    protected void delete(LessonModel lessonModel) {
        if (lessonModel == null) {  throw new NotFoundException("lessonModel não pode ser null");    }
        lessonRepository.delete(lessonModel);
    }
}


