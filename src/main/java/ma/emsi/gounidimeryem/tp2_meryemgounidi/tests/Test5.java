package ma.emsi.gounidimeryem.tp2_meryemgounidi.tests;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

public class Test5 {

    interface Assistant {
        String chat(String userMessage);
    }

    // Parser PDF personnalisé utilisant PDFBox directement
    public static String extractTextFromPdf(String filePath) {
        try (PDDocument document = PDDocument.load(new File(filePath))) {
            PDFTextStripper pdfStripper = new PDFTextStripper();

            // Configurer le text stripper
            pdfStripper.setSortByPosition(true);
            pdfStripper.setStartPage(1);
            pdfStripper.setEndPage(document.getNumberOfPages());

            String text = pdfStripper.getText(document);
            System.out.println("✓ PDF parsé avec succès");
            System.out.println("Nombre de pages: " + document.getNumberOfPages());
            System.out.println("Texte extrait: " + text.length() + " caractères");

            // Afficher un échantillon du texte réel
            if (text.length() > 200) {
                System.out.println("Extrait du texte réel:");
                System.out.println("--- DEBUT TEXTE REEL ---");
                String sample = text.substring(0, Math.min(500, text.length()));
                // Nettoyer l'échantillon pour l'affichage
                sample = sample.replaceAll("\\n+", "\n").trim();
                System.out.println(sample);
                System.out.println("--- FIN TEXTE REEL ---");
            }

            return text;
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'extraction du texte PDF", e);
        }
    }

    public static void main(String[] args) {
        String llmKey = System.getenv("GeminiKey");

        if (llmKey == null || llmKey.trim().isEmpty()) {
            System.err.println("Erreur : Clé API Gemini manquante");
            return;
        }

        try {
            // Modèle de chat
            ChatModel chatModel = GoogleAiGeminiChatModel.builder()
                    .apiKey(llmKey)
                    .modelName("gemini-2.0-flash")
                    .temperature(0.1)
                    .maxOutputTokens(1500)
                    .build();

            System.out.println("=== Extraction du texte PDF ===");

            String pdfPath = "src/main/resources/ML.pdf";
            File pdfFile = new File(pdfPath);

            if (!pdfFile.exists()) {
                System.err.println("✗ Fichier PDF non trouvé: " + pdfPath);
                return;
            }

            System.out.println("Fichier PDF trouvé: " + pdfFile.getAbsolutePath());
            System.out.println("Taille du fichier: " + pdfFile.length() + " bytes");

            // Extraire le texte du PDF avec notre méthode personnalisée
            String pdfText = extractTextFromPdf(pdfPath);

            if (pdfText == null || pdfText.trim().isEmpty()) {
                System.err.println("✗ Aucun texte extrait du PDF. Le PDF contient-il du texte lisible ?");
                return;
            }

            // Vérifier si le texte extrait est lisible
            if (pdfText.contains("%PDF") || pdfText.length() < 100) {
                System.err.println("⚠ ATTENTION: Le texte extrait semble corrompu ou trop court");
                System.err.println("Le PDF peut être scanné (image) ou protégé");
            }

            // Créer le document LangChain4j
            Document document = Document.from(pdfText);

            // Modèle d'embedding
            EmbeddingModel embeddingModel = GoogleAiEmbeddingModel.builder()
                    .apiKey(llmKey)
                    .modelName("text-embedding-004")
                    .build();

            EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

            // Splitter le document
            DocumentSplitter splitter = DocumentSplitters.recursive(500, 50);

            EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                    .documentSplitter(splitter)
                    .embeddingModel(embeddingModel)
                    .embeddingStore(embeddingStore)
                    .build();

            System.out.println("=== Calcul des embeddings ===");
            ingestor.ingest(document);
            System.out.println("✓ Embeddings calculés et stockés");

            // Configuration du retriever
            Assistant assistant = AiServices.builder(Assistant.class)
                    .chatModel(chatModel)
                    .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                    .contentRetriever(EmbeddingStoreContentRetriever.builder()
                            .embeddingStore(embeddingStore)
                            .embeddingModel(embeddingModel)
                            .maxResults(4)
                            .minScore(0.5)
                            .build())
                    .build();

            System.out.println("\n=== Système RAG initialisé avec succès ===");
            System.out.println("Document: " + pdfFile.getName());
            System.out.println("Texte extrait: " + pdfText.length() + " caractères");
            System.out.println("Tapez 'fin' pour quitter\n");

            // Boucle de conversation
            try (Scanner scanner = new Scanner(System.in)) {
                while (true) {
                    System.out.print("Question: ");
                    String question = scanner.nextLine().trim();

                    if ("fin".equalsIgnoreCase(question)) break;
                    if (question.isEmpty()) continue;

                    try {
                        System.out.println("Recherche dans le document...");
                        long startTime = System.currentTimeMillis();
                        String reponse = assistant.chat(question);
                        long endTime = System.currentTimeMillis();

                        System.out.println("Assistant: " + reponse);
                        System.out.printf("(Temps: %dms)\n", endTime - startTime);
                        System.out.println("─".repeat(60));
                    } catch (Exception e) {
                        System.err.println("Erreur: " + e.getMessage());
                        System.out.println();
                    }
                }
            }

            System.out.println("Au revoir!");

        } catch (Exception e) {
            System.err.println("Erreur d'initialisation: " + e.getMessage());
            e.printStackTrace();
        }
    }
}