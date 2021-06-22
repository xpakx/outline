package io.github.xpakx.outline.repo;

import io.github.xpakx.outline.entity.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LinkRepository extends JpaRepository<Link, Integer> {
}
