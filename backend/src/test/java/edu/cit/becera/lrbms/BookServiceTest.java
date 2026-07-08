package edu.cit.becera.lrbms;

import edu.cit.becera.lrbms.entities.Book;
import edu.cit.becera.lrbms.repositories.BookRepository;
import edu.cit.becera.lrbms.services.BookService;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BookServiceTest {

    @Test
    void shouldReserveBookWhenCopiesAvailable() {
        BookRepository repository = mock(BookRepository.class);
        Book book = new Book();
        book.setId(1L);
        book.setAvailableCopies(2);
        when(repository.findById(1L)).thenReturn(Optional.of(book));
        when(repository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookService service = new BookService(repository);
        Book updatedBook = service.reserveBook(1L);

        assertNotNull(updatedBook);
        assertEquals(1, updatedBook.getAvailableCopies());
    }

    @Test
    void shouldRejectReservationWhenNoCopiesAvailable() {
        BookRepository repository = mock(BookRepository.class);
        Book book = new Book();
        book.setId(2L);
        book.setAvailableCopies(0);
        when(repository.findById(2L)).thenReturn(Optional.of(book));

        BookService service = new BookService(repository);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> service.reserveBook(2L));
        assertEquals("No copies available for reservation", exception.getMessage());
    }
}
