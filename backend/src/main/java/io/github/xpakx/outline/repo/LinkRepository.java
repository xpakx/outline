package io.github.xpakx.outline.repo;

import io.github.xpakx.outline.entity.Link;
import io.github.xpakx.outline.entity.dto.LinkDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LinkRepository extends JpaRepository<Link, Long> {
    Optional<LinkDto> getProjectedById(Long id);
}
