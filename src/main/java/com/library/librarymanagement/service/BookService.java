package com.library.librarymanagement.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.library.librarymanagement.entity.Book;
import com.library.librarymanagement.repository.BookRepository;

@Service
public class BookService {

    @Autowired
    private BookRepository repository;

    public void saveBook(Book book) {
        repository.save(book);
    }

    public List<Book> getAllBooks() {
        return repository.findAll();
    }

    public Book getBookById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public void deleteBook(Long id) {
        repository.deleteById(id);
    }
}