package ma.emsi.gounidimeryem.tp2_meryemgounidi.tests;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import ma.emsi.gounidimeryem.tp2_meryemgounidi.tools.AssistantMeteo;
import ma.emsi.gounidimeryem.tp2_meryemgounidi.tools.MeteoTool;

/**
 * Test 6 – démonstration de l'utilisation d'un outil (tool calling) avec LangChain4j.
 */
public final class Test6 {

    private Test6() {
        // Utilité : empêcher l'instanciation.
    }

    public static void main(String[] args) {
    String apiKey = resolveGeminiKey();

    ChatModel model = GoogleAiGeminiChatModel.builder()
        .apiKey(apiKey)
        .modelName("gemini-2.5-flash")
        .temperature(0.2)
        .logRequestsAndResponses(true)
        .build();

        AssistantMeteo assistant = AiServices.builder(AssistantMeteo.class)
                .chatModel(model)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .tools(new MeteoTool())
                .build();

    poserQuestion(assistant,
        "J'ai prévu d'aller aujourd'hui à Safi. Est-ce que tu me conseilles de prendre un parapluie ?");
    poserQuestion(assistant, "Quel temps fait-il à Zagoura ?");
    poserQuestion(assistant, "Qui a écrit La Boîte à merveilles?");
    }

    private static void poserQuestion(AssistantMeteo assistant, String question) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("Question : " + question);
        try {
            String reponse = assistant.chat(question);
            System.out.println("Réponse : " + reponse);
        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    private static String resolveGeminiKey() {
        String[] candidates = { "GeminiKey" };
        for (String name : candidates) {
            String value = System.getenv(name);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        throw new IllegalStateException(
                "Aucune variable d'environnement pour la clé Gemini ");
    }
}