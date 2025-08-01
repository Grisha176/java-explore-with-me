package ru.practicum.compilation.dao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.compilation.model.Compilation;


import java.util.List;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {


    @Query("SELECT c FROM Compilation c WHERE :pinned IS NULL OR c.pinned = :pinned")
    List<Compilation> findByPinned(@Param("pinned") Boolean pinned, Pageable pageable);
}
