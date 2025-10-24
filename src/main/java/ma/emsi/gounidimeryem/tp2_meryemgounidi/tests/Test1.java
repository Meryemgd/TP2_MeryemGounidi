package ma.emsi.gounidimeryem.tp2_meryemgounidi.tests;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;

public class Test1 {
    public static void main(String[] args) {
        String cle = System.getenv("GeminiKey");

        // Création du modèle
        ChatModel modele = GoogleAiGeminiChatModel.builder()
                .apiKey(cle)
                .modelName("gemini-2.5-flash")
                .temperature(0.7)
                .build();

        // Pose une question au modèle
        String reponse = modele.chat("Quelle est la capitale du Maroc ?");
        String reponse2 = modele.chat("Quelle est la capitale de France ?");

        // Affiche la réponse du modèle
        System.out.println(reponse);
        System.out.println(reponse2);
    }
}