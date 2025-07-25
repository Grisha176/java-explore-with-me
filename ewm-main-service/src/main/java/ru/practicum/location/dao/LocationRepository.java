package ru.practicum.location.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.location.Location;

public interface LocationRepository extends JpaRepository<Location, Long> {
}
