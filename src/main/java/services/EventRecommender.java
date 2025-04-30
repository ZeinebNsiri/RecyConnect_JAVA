package services;

import entities.Event;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class EventRecommender {

    public List<Event> recommendEvents(List<Event> allEvents, List<Integer> userEventIds, int topK) throws IOException {
        // Create in-memory index
        ByteBuffersDirectory directory = new ByteBuffersDirectory();
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(directory, config);

        // Enable term vector storage
        FieldType textWithTermVectors = new FieldType(TextField.TYPE_STORED);
        textWithTermVectors.setStoreTermVectors(true);

        // Index all events
        for (Event event : allEvents) {
            Document doc = new Document();
            String fullText = event.getName() + " " + event.getDescription() + " " + event.getLocation();
            doc.add(new Field("content", fullText, textWithTermVectors));
            doc.add(new StringField("eventId", String.valueOf(event.getId()), Field.Store.YES));
            writer.addDocument(doc);
        }
        writer.close();

        // Calculate similarities
        IndexReader reader = DirectoryReader.open(directory);
        Map<Integer, Double> similarityScores = new HashMap<>();

        for (int userEventId : userEventIds) {
            for (int i = 0; i < reader.maxDoc(); i++) {
                String docEventId = reader.document(i).get("eventId");
                if (docEventId.equals(String.valueOf(userEventId))) {
                    Terms userTerms = reader.getTermVector(i, "content");
                    if (userTerms == null) continue;

                    for (int j = 0; j < reader.maxDoc(); j++) {
                        String otherEventId = reader.document(j).get("eventId");
                        if (userEventIds.contains(Integer.parseInt(otherEventId)) || otherEventId.equals(docEventId)) continue;

                        Terms otherTerms = reader.getTermVector(j, "content");
                        if (otherTerms == null) continue;

                        double similarity = computeCosineSimilarity(userTerms, otherTerms);
                        similarityScores.merge(Integer.parseInt(otherEventId), similarity, Double::sum);
                    }
                    break;
                }
            }
        }

        reader.close();
        directory.close();

        return allEvents.stream()
                .filter(e -> similarityScores.containsKey(e.getId()))
                .sorted((e1, e2) -> Double.compare(similarityScores.get(e2.getId()), similarityScores.get(e1.getId())))
                .limit(topK)
                .collect(Collectors.toList());
    }

    private double computeCosineSimilarity(Terms terms1, Terms terms2) throws IOException {
        Map<String, Integer> termFreq1 = getTermFrequencies(terms1);
        Map<String, Integer> termFreq2 = getTermFrequencies(terms2);

        Set<String> commonTerms = new HashSet<>(termFreq1.keySet());
        commonTerms.retainAll(termFreq2.keySet());

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (String term : commonTerms) {
            dotProduct += termFreq1.get(term) * termFreq2.get(term);
        }
        for (int freq : termFreq1.values()) {
            norm1 += freq * freq;
        }
        for (int freq : termFreq2.values()) {
            norm2 += freq * freq;
        }

        return (norm1 == 0 || norm2 == 0) ? 0 : dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    private Map<String, Integer> getTermFrequencies(Terms terms) throws IOException {
        Map<String, Integer> frequencies = new HashMap<>();
        TermsEnum termsEnum = terms.iterator();
        BytesRef term;

        while ((term = termsEnum.next()) != null) {
            frequencies.put(term.utf8ToString(), (int) termsEnum.totalTermFreq());
        }

        return frequencies;
    }
}
