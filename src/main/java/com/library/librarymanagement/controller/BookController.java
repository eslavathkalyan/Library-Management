package com.library.librarymanagement.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.library.librarymanagement.entity.Book;
import com.library.librarymanagement.service.BookService;

@Controller
public class BookController {

    @Autowired
    private BookService service;

    // ── Home / Dashboard ──────────────────────────────────────────────────
    @GetMapping("/")
    public String home(Model model) {
        List<Book> allBooks = service.getAllBooks();

        long totalBooks     = allBooks.size();
        long availableBooks = allBooks.stream().filter(b -> "Available".equalsIgnoreCase(b.getStatus())).count();
        long issuedBooks    = allBooks.stream().filter(b -> "Issued".equalsIgnoreCase(b.getStatus())).count();
        long overdueBooks   = allBooks.stream().filter(b -> "Overdue".equalsIgnoreCase(b.getStatus())).count();

        // Recent 5 books for the dashboard table
        List<Book> recentBooks = allBooks.stream()
                .sorted((a, b) -> {
                    if (a.getId() == null) return 1;
                    if (b.getId() == null) return -1;
                    return b.getId().compareTo(a.getId());
                })
                .limit(5)
                .collect(Collectors.toList());

        // Category counts from DB
        Map<String, Long> categoryCounts = new LinkedHashMap<>();
        allBooks.stream()
                .filter(b -> b.getCategory() != null && !b.getCategory().trim().isEmpty())
                .forEach(b -> categoryCounts.merge(
                        capitalize(b.getCategory()), 1L, Long::sum));

        model.addAttribute("totalBooks",     totalBooks);
        model.addAttribute("availableBooks", availableBooks);
        model.addAttribute("issuedBooks",    issuedBooks);
        model.addAttribute("overdueBooks",   overdueBooks);
        model.addAttribute("recentBooks",    recentBooks);
        model.addAttribute("categoryCounts", categoryCounts);

        return "index";
    }

    // ── Show Add Book Form ────────────────────────────────────────────────
    @GetMapping("/addBook")
    public String addBookForm(Model model) {
        model.addAttribute("book", new Book());
        return "addBook";
    }

    // ── Save Book ─────────────────────────────────────────────────────────
    @PostMapping("/books/add")
    public String saveBook(@ModelAttribute Book book) {
        if (book.getAvailableCopies() == null && book.getTotalCopies() != null) {
            book.setAvailableCopies(book.getTotalCopies());
        }
        if (book.getDateAdded() == null) {
            book.setDateAdded(LocalDate.now());
        }
        if (book.getStatus() == null || book.getStatus().trim().isEmpty()) {
            book.setStatus("Available");
        }
        service.saveBook(book);
        return "redirect:/books";
    }

    // ── Show Edit Book Form ───────────────────────────────────────────────
    @GetMapping("/books/edit/{id}")
    public String editBookForm(@PathVariable Long id, Model model) {
        Book book = service.getBookById(id);
        if (book == null) return "redirect:/books";
        model.addAttribute("book", book);
        return "editBook";
    }

    // ── Update Book ───────────────────────────────────────────────────────
    @PostMapping("/books/update/{id}")
    public String updateBook(@PathVariable Long id, @ModelAttribute Book book) {
        book.setId(id);
        if (book.getDateAdded() == null) {
            book.setDateAdded(LocalDate.now());
        }
        if (book.getStatus() == null || book.getStatus().trim().isEmpty()) {
            book.setStatus("Available");
        }
        service.saveBook(book);
        return "redirect:/books";
    }

    // ── View All Books ────────────────────────────────────────────────────
    @GetMapping("/books")
    public String viewBooks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String category,
            Model model) {

        List<Book> allBooks = service.getAllBooks();

        long totalBooks     = allBooks.size();
        long availableBooks = allBooks.stream().filter(b -> "Available".equalsIgnoreCase(b.getStatus())).count();
        long issuedBooks    = allBooks.stream().filter(b -> "Issued".equalsIgnoreCase(b.getStatus())).count();
        long overdueBooks   = allBooks.stream().filter(b -> "Overdue".equalsIgnoreCase(b.getStatus())).count();

        int pageSize   = 10;
        int totalPages = (int) Math.max(1, Math.ceil((double) totalBooks / pageSize));

        model.addAttribute("bookList",        allBooks);
        model.addAttribute("totalBooks",      totalBooks);
        model.addAttribute("availableBooks",  availableBooks);
        model.addAttribute("issuedBooks",     issuedBooks);
        model.addAttribute("overdueBooks",    overdueBooks);
        model.addAttribute("currentPage",     page);
        model.addAttribute("pageSize",        pageSize);
        model.addAttribute("totalPages",      totalPages);
        model.addAttribute("searchQuery",     search);
        model.addAttribute("selectedCategory", category);

        return "books";
    }

    // ── Delete Book ───────────────────────────────────────────────────────
    @GetMapping("/books/delete/{id}")
    public String deleteBook(@PathVariable Long id) {
        service.deleteBook(id);
        return "redirect:/books";
    }

    // ── Management: Issued Books ──────────────────────────────────────────
    @GetMapping("/management/issued")
    public String issuedBooks(Model model) {
        List<Book> issued = service.getAllBooks().stream()
                .filter(b -> "Issued".equalsIgnoreCase(b.getStatus()))
                .collect(Collectors.toList());
        model.addAttribute("issuedList", issued);
        return "issuedBooks";
    }

    // ── Management: Overdue Books ─────────────────────────────────────────
    @GetMapping("/management/overdue")
    public String overdueBooks(Model model) {
        List<Book> overdue = service.getAllBooks().stream()
                .filter(b -> "Overdue".equalsIgnoreCase(b.getStatus()))
                .collect(Collectors.toList());
        model.addAttribute("overdueList", overdue);
        return "overdueBooks";
    }

    // ── Management: Categories ────────────────────────────────────────────
    @GetMapping("/management/categories")
    public String categories(Model model) {
        List<Book> allBooks = service.getAllBooks();
        Map<String, Long> categoryCounts = new LinkedHashMap<>();
        allBooks.stream()
                .filter(b -> b.getCategory() != null && !b.getCategory().trim().isEmpty())
                .forEach(b -> categoryCounts.merge(
                        capitalize(b.getCategory()), 1L, Long::sum));
        model.addAttribute("categoryCounts", categoryCounts);
        model.addAttribute("totalBooks", (long) allBooks.size());
        return "categories";
    }

    // ── helper ─────────────────────────────────────────────────────────────
    private String capitalize(String s) {
        if (s == null || s.trim().isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }
}