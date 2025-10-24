package ma.emsi.gounidimeryem.tp2_meryemgounidi.tests;


import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.CosineSimilarity;

import java.time.Duration;

public class Test3 {
    public static void main(String[] args) {
        String cle = System.getenv("GeminiKey");

        EmbeddingModel modele = GoogleAiEmbeddingModel.builder()
                .apiKey(cle)
                .modelName("text-embedding-004")
                .taskType(GoogleAiEmbeddingModel.TaskType.SEMANTIC_SIMILARITY)
                .outputDimensionality(300)
                .timeout(Duration.ofSeconds(2))
                .build();

        String phrase1 = "Bonjour, comment allez-vous ?";
        String phrase2 = "Salut, quoi de neuf ?";
        String phrase3 = "Le chat mange une souris.";

        Response<Embedding> reponse1 = modele.embed(phrase1);
        Response<Embedding> reponse2 = modele.embed(phrase2);
        Response<Embedding> reponse3 = modele.embed(phrase3);

        Embedding emb1 = reponse1.content();
        Embedding emb2 = reponse2.content();
        Embedding emb3 = reponse3.content();

        // Calcul de similarité cosinus
        double similarite12 = CosineSimilarity.between(emb1, emb2);
        double similarite13 = CosineSimilarity.between(emb1, emb3);
        double similarite23 = CosineSimilarity.between(emb2, emb3);

        System.out.println("Similarité entre phrase 1 et 2 : " + similarite12);
        System.out.println("Similarité entre phrase 1 et 3 : " + similarite13);
        System.out.println("Similarité entre phrase 2 et 3 : " + similarite23);
    }
}