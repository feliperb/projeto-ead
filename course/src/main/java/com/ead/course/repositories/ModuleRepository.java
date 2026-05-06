package com.ead.course.repositories;

import com.ead.course.models.ModuleModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ModuleRepository extends JpaRepository<ModuleModel, UUID> {

//    @EntityGraph(attributePaths = "course") // Carregar os course com se nao fosse LAZY, mas sim EAGER (Carregamento dinamico = em tempo de execucao)
//    ModuleModel findByTitle(String title);

    //@Modifying // Para UPDATE e DELETE
    @Query(value="SELECT * FROM tb_modules WHERE course_course_id = :courseId", nativeQuery = true)
    List<ModuleModel> findAllModulesIntoCourse(@Param("courseId") UUID courseId);

    boolean existsByTitle(String title);

    boolean existsByCourse_CourseId(UUID courseId);

    boolean existsByTitleAndCourse_CourseId(String title, UUID courseId);

    boolean existsByTitleAndCourse_CourseIdAndModuleIdNot(String title, UUID courseId, UUID moduleId);
}
