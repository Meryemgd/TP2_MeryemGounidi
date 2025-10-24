package ma.emsi.gounidimeryem.tp2_meryemgounidi.llm;

/**
 * Interface pour définir les interactions avec le LLM via LangChain4j.
 * LangChain4j va créer automatiquement une implémentation proxy de cette interface.
 * 
 * TP2 - Utilisation de LangChain4j pour accéder à l'API Gemini
 */
public interface Assistant {
    
    /**
     * Envoie un message (prompt) au LLM et reçoit une réponse.
     * 
     * @param prompt le message/question à envoyer au LLM
     * @return la réponse du LLM
     */
    String chat(String prompt);
}
