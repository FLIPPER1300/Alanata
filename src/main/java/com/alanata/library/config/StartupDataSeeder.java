package com.alanata.library.config;

import com.alanata.library.entity.Book;
import com.alanata.library.entity.BookCopy;
import com.alanata.library.repository.BookRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(name = "app.seed-books", havingValue = "true", matchIfMissing = true)
public class StartupDataSeeder implements CommandLineRunner {

    private final BookRepository bookRepository;

    public StartupDataSeeder(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (bookRepository.count() > 0) {
            return;
        }

        List<Book> books = List.of(
                createBookWithCopies("Clean Code", "Robert C. Martin", "978-0132350884", 2008, 3),
                createBookWithCopies("Effective Java", "Joshua Bloch", "978-0134685991", 2018, 2),
                createBookWithCopies("Domain-Driven Design", "Eric Evans", "978-0321125217", 2003, 2),
                createBookWithCopies("Refactoring", "Martin Fowler", "978-0201485677", 1999, 1),
                createBookWithCopies("Design Patterns", "Erich Gamma", "978-0201633610", 1994, 2)
        );

        bookRepository.saveAll(books);
    }

    private Book createBookWithCopies(String title, String author, String isbn, Integer publishedYear, int copyCount) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setIsbn(isbn);
        book.setPublishedYear(publishedYear);

        List<BookCopy> copies = new ArrayList<>();
        for (int i = 0; i < copyCount; i++) {
            BookCopy copy = new BookCopy();
            copy.setBook(book);
            copy.setAvailable(true);
            copies.add(copy);
        }
        book.setCopies(copies);
        return book;
    }
}
