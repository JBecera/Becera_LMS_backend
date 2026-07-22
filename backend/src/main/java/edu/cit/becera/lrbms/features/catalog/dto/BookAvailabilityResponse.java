package edu.cit.becera.lrbms.features.catalog.dto;

import edu.cit.becera.lrbms.entities.Book;

/**
 * A catalog title alongside how many copies are free on a specific requested date, so the member's
 * date-filtered catalog can badge and gate booking per date rather than by current shelf count.
 */
public class BookAvailabilityResponse {
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String category;
    private String description;
    private String coverImage;
    private Integer totalCopies;
    private Integer availableCopies;
    private int availableOnDate;

    public static BookAvailabilityResponse from(Book book, int availableOnDate) {
        BookAvailabilityResponse response = new BookAvailabilityResponse();
        response.id = book.getId();
        response.title = book.getTitle();
        response.author = book.getAuthor();
        response.isbn = book.getIsbn();
        response.category = book.getCategory();
        response.description = book.getDescription();
        response.coverImage = book.getCoverImage();
        response.totalCopies = book.getTotalCopies();
        response.availableCopies = book.getAvailableCopies();
        response.availableOnDate = availableOnDate;
        return response;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getIsbn() { return isbn; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getCoverImage() { return coverImage; }
    public Integer getTotalCopies() { return totalCopies; }
    public Integer getAvailableCopies() { return availableCopies; }
    public int getAvailableOnDate() { return availableOnDate; }
}
