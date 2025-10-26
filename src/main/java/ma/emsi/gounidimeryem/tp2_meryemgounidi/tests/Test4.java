package ma.emsi.gounidimeryem.tp2_meryemgounidi.tests;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.data.embedding.Embedding;
import java.util.List;
/**
 * Le RAG facile !
 */
public class Test4 {

    // Assistant conversationnel
    interface Assistant {
        // Prend un message de l'utilisateur et retourne une réponse du LLM.
        String chat(String userMessage);
    }

    public static void main(String[] args) {
        String llmKey = System.getenv("GeminiKey");
        // Mettre une température qui ne dépasse pas 0,3.
        // Le RAG sert à mieux contrôler l'exactitude des informations données par le LLM
        // et il est donc logique de diminuer la température.
        ChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey(llmKey)
                .modelName("gemini-2.5-flash")
                .temperature(0.7)
                .build();

        // Chargement du document, sous la forme d'embeddings, dans une base vectorielle en mémoire
        String nomDocument = "src/main/resources/infos.txt";
        Document document = FileSystemDocumentLoader.loadDocument(nomDocument);
        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

        // Créer le modèle d'embedding
        EmbeddingModel embeddingModel = new EmbeddingModel() {
            @Override
            public dev.langchain4j.model.output.Response<Embedding> embed(String text) {
                // Simple embedding basé sur le hash du texte (pour le test)
                float[] vector = new float[384]; // Dimension standard
                int hash = text.hashCode();
                for (int i = 0; i < vector.length; i++) {
                    vector[i] = ((hash >> (i % 32)) & 1) * 0.1f + (float) Math.sin(i * 0.1 + hash) * 0.5f;
                }
                return dev.langchain4j.model.output.Response.from(new Embedding(vector));
            }

            @Override
            public dev.langchain4j.model.output.Response<List<Embedding>> embedAll(List<TextSegment> segments) {
                List<Embedding> embeddings = segments.stream().map(segment -> embed(segment.text()).content()).toList();
                return dev.langchain4j.model.output.Response.from(embeddings);
            }
        };

        // Calcule les embeddings et les enregistre dans la base vectorielle
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
        ingestor.ingest(document);

        // Création de l'assistant conversationnel, avec une mémoire.
        // L'implémentation de Assistant est faite par LangChain4j.
        // L'assistant gardera en mémoire les 10 derniers messages.
        // La base vectorielle en mémoire est utilisée pour retrouver les embeddings.
        Assistant assistant =
                AiServices.builder(Assistant.class)
                        .chatModel(model)
                        .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                        .contentRetriever(EmbeddingStoreContentRetriever.builder()
                                .embeddingStore(embeddingStore)
                                .embeddingModel(embeddingModel)
                                .build())
                        .build();

        // Le LLM va utiliser l'information du fichier infos.txt pour répondre à la question.
        String question = "Comment s'appelle le chat de Pierre ?";
        // L'assistant recherche dans la base vectorielle les informations les plus pertinentes
        // pour répondre à la question, en comparant les embeddings de la base et celui de la question.
        // Ces informations sont ajoutées à la question et le tout est envoyé au LLM.
        String reponse = assistant.chat(question);
        // Affiche la réponse du LLM.
        System.out.println(reponse);
    }

}
