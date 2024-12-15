package org.example;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {
    private final Map<String, Document> storage = new HashMap<>();

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        if (document == null) {
            throw new IllegalArgumentException("Document cannot be null");
        }
        if (document.getId() == null) {
            String id;
            do {
                id = UUID.randomUUID().toString();
            } while (storage.containsKey(id));
            document.setId(id);
        }
        storage.put(document.getId(), document);
        return document;
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        if (request == null) {
            return new ArrayList<>(storage.values());
        }
        return storage.values().stream()
                .filter(doc -> isDocumentMatchingRequest(doc, request))
                .toList();
    }

    private boolean isDocumentMatchingRequest(Document document, SearchRequest request) {
        return isTitleMatchingPrefixes(document, request)
                && isContentMatching(document, request)
                && isAuthorIdsMatching(document, request)
                && isCreatedInRange(document, request);
    }

    private boolean isTitleMatchingPrefixes(Document document, SearchRequest request) {
        if (request.getTitlePrefixes() == null || request.getTitlePrefixes().isEmpty()) {
            return true;
        }
        if (document.getTitle() == null) {
            return false;
        }
        return request.getTitlePrefixes().stream()
                .map(String::toLowerCase)
                .anyMatch(prefix -> document.getTitle().toLowerCase().startsWith(prefix));
    }

    private boolean isContentMatching(Document document, SearchRequest request) {
        if (request.getContainsContents() == null || request.getContainsContents().isEmpty()) {
            return true;
        }
        if (document.getContent() == null) {
            return false;
        }
        return request.getContainsContents().stream()
                .map(String::toLowerCase)
                .anyMatch(content -> document.getContent().toLowerCase().contains(content));
    }

    private boolean isAuthorIdsMatching(Document document, SearchRequest request) {
        List<String> authorIds = request.getAuthorIds();
        if (authorIds == null || authorIds.isEmpty()) {
            return true;
        }
        if (document.getAuthor() == null || document.getAuthor().getId() == null) {
            return false;
        }
        return authorIds.contains(document.getAuthor().getId());
    }

    private boolean isCreatedInRange(Document document, SearchRequest request) {
        Instant createdFrom = request.getCreatedFrom();
        Instant createdTo = request.getCreatedTo();
        if (document.getCreated() == null) {
            return false;
        }
        return (createdFrom == null || !document.getCreated().isBefore(createdFrom))
                && (createdTo == null || !document.getCreated().isAfter(createdTo));
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        return id == null ? Optional.empty() : Optional.ofNullable(storage.get(id));
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}
