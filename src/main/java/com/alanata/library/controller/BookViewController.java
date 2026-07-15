package com.alanata.library.controller;

import com.alanata.library.dto.request.BookCreateRequest;
import com.alanata.library.dto.response.BookPageResponse;
import com.alanata.library.service.BookService;
import com.alanata.library.web.BookForm;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class BookViewController {

    private final BookService bookService;

    public BookViewController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/books";
    }

    @GetMapping("/books")
    public String listBooks(
            @PageableDefault(sort = "title", direction = Direction.ASC) Pageable pageable,
            Model model
    ) {
        BookPageResponse booksPage = bookService.getBooks(pageable);
        model.addAttribute("booksPage", booksPage);
        return "books/list";
    }

    @GetMapping("/books/new")
    public String newBookForm(Model model) {
        if (!model.containsAttribute("bookForm")) {
            model.addAttribute("bookForm", new BookForm());
        }
        return "books/create";
    }

    @PostMapping("/books")
    public String createBook(
            @Valid @ModelAttribute("bookForm") BookForm bookForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "books/create";
        }

        try {
            bookService.createBook(new BookCreateRequest(
                    bookForm.getTitle(),
                    bookForm.getAuthor(),
                    bookForm.getIsbn(),
                    bookForm.getPublishedYear()
            ));
        } catch (RuntimeException exception) {
            bindingResult.reject("bookCreate", exception.getMessage());
            return "books/create";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Book was created.");
        return "redirect:/books";
    }
}
