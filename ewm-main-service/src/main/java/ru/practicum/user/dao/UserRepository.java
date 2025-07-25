package ru.practicum.user.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.user.model.User;

import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserRepository extends JpaRepository<User,Long> {

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE :ids IS NULL OR u.id IN :ids")
    List<User> findAll(@Param("ids") List<Long> ids, Pageable pageable);
}
