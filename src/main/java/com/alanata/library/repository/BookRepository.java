package com.alanata.library.repository;

import com.alanata.library.entity.Book;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    boolean existsByTitle(String title);

    boolean existsByIsbn(String isbn);

    boolean existsByTitleAndIdNot(String title, Long id);

    boolean existsByIsbnAndIdNot(String isbn, Long id);

    @Override
    @EntityGraph(attributePaths = "copies")
    Optional<Book> findById(Long id);
}
