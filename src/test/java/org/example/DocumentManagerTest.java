package org.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DocumentManagerTest {
    private DocumentManager manager;

    @BeforeEach
    void setUp() {
        manager = new DocumentManager();
    }

    @Test
    void save_IdIsNull_ShouldSaveNewDocument() {
        DocumentManager.Document document = DocumentManager.Document.builder()
                .title("New title")
                .content("New content")
                .author(DocumentManager.Author.builder().id("1").name("Author").build())
                .created(Instant.now())
                .build();

        DocumentManager.Document saved = manager.save(document);

        assertNotNull(saved.getId());
        assertEquals("New title", saved.getTitle());
        assertEquals("New content", saved.getContent());
    }

    @Test
    void save_IdExists_ShouldUpdateExistingDocument() {
        DocumentManager.Document document = DocumentManager.Document.builder()
                .id("1")
                .title("Title")
                .content("Content")
                .author(DocumentManager.Author.builder().id("1").name("Author").build())
                .created(Instant.now())
                .build();

        manager.save(document);

        DocumentManager.Document updatedDocument = DocumentManager.Document.builder()
                .id("1")
                .title("Updated Title")
                .content("Updated Content")
                .author(DocumentManager.Author.builder().id("1").name("Author").build())
                .created(document.getCreated())
                .build();

        manager.save(updatedDocument);

        Optional<DocumentManager.Document> result = manager.findById("1");
        assertTrue(result.isPresent());
        assertEquals("Updated Title", result.get().getTitle());
        assertEquals("Updated Content", result.get().getContent());
    }

    @Test
    void save_DocumentIsNull_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> manager.save(null));
    }

    @Test
    void findById_IdExists_ShouldReturnDocument() {
        DocumentManager.Document document = DocumentManager.Document.builder()
                .id("1")
                .title("Title")
                .content("Content")
                .author(DocumentManager.Author.builder().id("1").name("Author").build())
                .created(Instant.now())
                .build();

        manager.save(document);

        Optional<DocumentManager.Document> result = manager.findById("1");
        assertTrue(result.isPresent());
        assertEquals("Title", result.get().getTitle());
    }

    @Test
    void findById_IdDoesNotExist_ShouldReturnEmpty() {
        Optional<DocumentManager.Document> result = manager.findById("999");
        assertTrue(result.isEmpty());
    }

    @Test
    void findById_IdIsNull_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> manager.findById(null));
        assertEquals("Id cannot be null", exception.getMessage());
    }

    @Test
    void search_RequestIsNull_ShouldReturnAllDocuments() {
        manager.save(DocumentManager.Document.builder()
                .title("Document 1")
                .content("Content 1")
                .author(DocumentManager.Author.builder().id("1").name("Author 1").build())
                .created(Instant.now())
                .build());
        manager.save(DocumentManager.Document.builder()
                .title("Document 2")
                .content("Content 2")
                .author(DocumentManager.Author.builder().id("2").name("Author 2").build())
                .created(Instant.now())
                .build());

        List<DocumentManager.Document> results = manager.search(null);
        assertEquals(2, results.size());
    }

    @Test
    void search_ShouldFilterByTitlePrefix() {
        manager.save(DocumentManager.Document.builder()
                .title("Title1...")
                .content("Content1")
                .author(DocumentManager.Author.builder().id("1").name("Author1").build())
                .created(Instant.now())
                .build());
        manager.save(DocumentManager.Document.builder()
                .title("Title2...")
                .content("Content2")
                .author(DocumentManager.Author.builder().id("2").name("Author2").build())
                .created(Instant.now())
                .build());

        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .titlePrefixes(List.of("Title1"))
                .build();

        List<DocumentManager.Document> results = manager.search(request);
        assertEquals(1, results.size());
        assertEquals("Title1...", results.get(0).getTitle());
    }

    @Test
    void search_TitlePrefixesEmpty_ShouldReturnAllDocuments() {
        manager.save(DocumentManager.Document.builder()
                .title("Title1")
                .content("Content1")
                .author(DocumentManager.Author.builder().id("1").name("Author1").build())
                .created(Instant.now())
                .build());
        manager.save(DocumentManager.Document.builder()
                .title("Title2")
                .content("Content2")
                .author(DocumentManager.Author.builder().id("2").name("Author2").build())
                .created(Instant.now())
                .build());

        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .titlePrefixes(new ArrayList<>())
                .build();

        List<DocumentManager.Document> results = manager.search(request);
        assertEquals(2, results.size());
    }

    @Test
    void search_PrefixesEmpty_ShouldReturnAllDocuments() {
        manager.save(DocumentManager.Document.builder()
                .title("Title1")
                .content("Content1")
                .author(DocumentManager.Author.builder().id("1").name("Author1").build())
                .created(Instant.now())
                .build());
        manager.save(DocumentManager.Document.builder()
                .title("Title2")
                .content("Content2")
                .author(DocumentManager.Author.builder().id("2").name("Author2").build())
                .created(Instant.now())
                .build());

        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .titlePrefixes(List.of(""))
                .build();

        List<DocumentManager.Document> results = manager.search(request);
        assertEquals(2, results.size());
    }

    @Test
    void search_ShouldFilterByContentKeywords() {
        manager.save(DocumentManager.Document.builder()
                .title("Title1")
                .content("Interesting content")
                .author(DocumentManager.Author.builder().id("1").name("Author1").build())
                .created(Instant.now())
                .build());
        manager.save(DocumentManager.Document.builder()
                .title("Title2")
                .content("Content")
                .author(DocumentManager.Author.builder().id("2").name("Author2").build())
                .created(Instant.now())
                .build());

        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .containsContents(List.of("Interesting"))
                .build();

        List<DocumentManager.Document> results = manager.search(request);
        assertEquals(1, results.size());
        assertEquals("Interesting content", results.get(0).getContent());
    }

    @Test
    void search_ContainsContentsEmpty_ShouldReturnAllDocuments() {
        manager.save(DocumentManager.Document.builder()
                .title("Title1")
                .content("Content1")
                .author(DocumentManager.Author.builder().id("1").name("Author1").build())
                .created(Instant.now())
                .build());
        manager.save(DocumentManager.Document.builder()
                .title("Title2")
                .content("Content2")
                .author(DocumentManager.Author.builder().id("2").name("Author2").build())
                .created(Instant.now())
                .build());

        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .containsContents(new ArrayList<>())
                .build();

        List<DocumentManager.Document> results = manager.search(request);
        assertEquals(2, results.size());
    }

    @Test
    void search_ShouldFilterByContentWithLowCaseSearch() {
        manager.save(DocumentManager.Document.builder()
                .title("Title1")
                .content("Interesting content")
                .author(DocumentManager.Author.builder().id("1").name("Author1").build())
                .created(Instant.now())
                .build());
        manager.save(DocumentManager.Document.builder()
                .title("Title2")
                .content("Content")
                .author(DocumentManager.Author.builder().id("2").name("Author2").build())
                .created(Instant.now())
                .build());

        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .containsContents(List.of("interesting"))
                .build();

        List<DocumentManager.Document> results = manager.search(request);
        assertEquals(1, results.size());
        assertEquals("Interesting content", results.get(0).getContent());
    }

    @Test
    void search_ShouldFilterByAuthorIds() {
        manager.save(DocumentManager.Document.builder()
                .title("Title1")
                .content("Content1")
                .author(DocumentManager.Author.builder().id("1").name("Author1").build())
                .created(Instant.now())
                .build());
        manager.save(DocumentManager.Document.builder()
                .title("Title2")
                .content("Content2")
                .author(DocumentManager.Author.builder().id("2").name("Author2").build())
                .created(Instant.now())
                .build());

        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .authorIds(List.of("1"))
                .build();

        List<DocumentManager.Document> results = manager.search(request);
        assertEquals(1, results.size());
        assertEquals("Title1", results.get(0).getTitle());
    }

    @Test
    void search_ShouldFilterByDateRange() {
        Instant now = Instant.now();
        Instant oneDayAgo = now.minusSeconds(86400);
        Instant oneDayLater = now.plusSeconds(86400);

        manager.save(DocumentManager.Document.builder()
                .title("Title1")
                .content("Content1")
                .author(DocumentManager.Author.builder().id("1").name("Author1").build())
                .created(oneDayAgo)
                .build());
        manager.save(DocumentManager.Document.builder()
                .title("Title2")
                .content("Content2")
                .author(DocumentManager.Author.builder().id("2").name("Author2").build())
                .created(oneDayLater)
                .build());

        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .createdFrom(oneDayAgo)
                .createdTo(oneDayLater)
                .build();

        List<DocumentManager.Document> results = manager.search(request);
        assertEquals(2, results.size());
    }

    @Test
    void search_CreatedFromNull_ShouldReturnDocument() {
        Instant now = Instant.now();
        manager.save(DocumentManager.Document.builder()
                .title("Title1")
                .content("Content1")
                .author(DocumentManager.Author.builder().id("1").name("Author1").build())
                .created(now)
                .build());

        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .createdFrom(null)
                .createdTo(now.plusSeconds(86400))
                .build();

        List<DocumentManager.Document> results = manager.search(request);
        assertEquals(1, results.size());
    }

    @Test
    void search_EmptyRequest_ShouldReturnAllDocuments() {
        manager.save(DocumentManager.Document.builder()
                .title("Title1")
                .content("Content1")
                .author(DocumentManager.Author.builder().id("1").name("Author1").build())
                .created(Instant.now())
                .build());
        manager.save(DocumentManager.Document.builder()
                .title("Title2")
                .content("Content2")
                .author(DocumentManager.Author.builder().id("2").name("Author2").build())
                .created(Instant.now())
                .build());

        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .build();

        List<DocumentManager.Document> results = manager.search(request);
        assertEquals(2, results.size());
    }
}
