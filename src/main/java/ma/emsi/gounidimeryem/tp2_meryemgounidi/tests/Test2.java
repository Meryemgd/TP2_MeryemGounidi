package ma.emsi.gounidimeryem.tp2_meryemgounidi.tests;


import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;

import java.util.Map;

public class Test2 {
    public static void main(String[] args) {
        String cle = System.getenv("GeminiKey");

        ChatModel modele = GoogleAiGeminiChatModel.builder()
                .apiKey(cle)
                .modelName("gemini-2.5-flash")
                .temperature(0.7)
                .build();

        // Création du template
        PromptTemplate template = PromptTemplate.from(
                "Traduis le texte suivant en anglais : {{texte}}"
        );

        // Application du template
        Prompt prompt = template.apply(Map.of("texte", "Bonjour, comment allez-vous ?"));

        // Envoi au modèle
        String reponse = modele.chat(prompt.text());
        System.out.println(reponse);
    }
}
