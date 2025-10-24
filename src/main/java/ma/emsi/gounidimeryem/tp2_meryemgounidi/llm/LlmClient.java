package ma.emsi.gounidimeryem.tp2_meryemgounidi.llm;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.inject.Named;

/**
 * Classe métier qui gère l'interaction avec le LLM via LangChain4j.
 * 
 * Cette classe :
 * 1. Récupère la clé API Gemini (variable d'environnement GeminiKey)
 * 2. Crée un ChatModel (instance de GoogleAiGeminiChatModel)
 * 3. Crée un Assistant (implémentation proxy via AiServices)
 * 4. Gère la mémoire de la conversation
 * 
 * Le backing bean utilise cette classe pour envoyer des requêtes au LLM.
 * 
 * TP2 - Utilisation de LangChain4j avec Gemini
 */
@Named
public class LlmClient {

    /**
     * Rôle système pour le LLM
     */
    private String systemRole;

    /**
     * L'assistant créé par LangChain4j (implémentation proxy de l'interface Assistant)
     */
    private Assistant assistant;

    /**
     * La mémoire de la conversation
     */
    private ChatMemory chatMemory;
    
    /**
     * Le ChatModel utilisé directement
     */
    private ChatModel chatModel;

    /**
     * Constructeur : initialise le client LLM
     * 
     * 1. Récupère la clé API Gemini
     * 2. Crée le modèle de chat
     * 3. Crée l'assistant avec sa mémoire
     */
    public LlmClient() {
        // Récupérer la clé secrète depuis les variables d'environnement
        String cle = System.getenv("GeminiKey");
        
        if (cle == null || cle.isBlank()) {
            throw new IllegalStateException(
                "La variable d'environnement GeminiKey n'est pas définie. " +
                "Veuillez définir votre clé API Gemini."
            );
        }

        // Créer le modèle de chat (ChatModel) pour Gemini
        this.chatModel = GoogleAiGeminiChatModel.builder()
                .apiKey(cle)
                .modelName("gemini-2.5-flash")
                .temperature(0.7)
                .build();

        // Initialiser la mémoire de conversation
        // Garde les 10 derniers messages
        this.chatMemory = MessageWindowChatMemory.withMaxMessages(10);

        // Créer l'assistant via LangChain4j
        this.assistant = AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .chatMemory(chatMemory)
                .build();
    }

    /**
     * Définir le rôle système et vider la mémoire
     * 
     * Cela permet au LLM de connaître son rôle dès le début de la conversation.
     * 
     * @param roleSysteme le rôle que l'IA doit jouer
     */
    public void setSystemRole(String roleSysteme) {
        this.systemRole = roleSysteme;
        
        // Vider la mémoire quand on change de rôle
        this.chatMemory.clear();
        
        // Ajouter le rôle système à la mémoire
        this.chatMemory.add(SystemMessage.from(roleSysteme));
    }

    /**
     * Envoyer une requête au LLM et recevoir la réponse
     * 
     * @param prompt la question/message de l'utilisateur
     * @return la réponse du LLM
     */
    public String askQuestion(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("La question ne peut pas être vide");
        }
        
        // Utiliser l'assistant pour envoyer la question au LLM
        return this.assistant.chat(prompt);
    }

    public String getSystemRole() {
        return systemRole;
    }
}