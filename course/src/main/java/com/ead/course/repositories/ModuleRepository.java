package com.ead.course.repositories;

import com.ead.course.models.ModuleModel;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ModuleRepository extends JpaRepository<ModuleModel, UUID> {

//    @EntityGraph(attributePaths = "course") // Carregar os course com se nao fosse LAZY, mas sim EAGER (Carregamento dinamico = em tempo de execucao)
//    ModuleModel findByTitle(String title);

    //@Modifying // Para UPDATE e DELETE
    @Query(value="SELECT * FROM tb_modules WHERE course_course_id = :courseId", nativeQuery = true)
    List<ModuleModel> findAllModulesIntoCourse(@Param("courseId") UUID courseId);

    @Query(value="SELECT * FROM tb_modules WHERE module_id = :moduleId", nativeQuery = true)
    Optional<ModuleModel> findByModuleId(@Param("moduleId") UUID moduleId);

    boolean existsByTitle(@NotBlank String title);
}
