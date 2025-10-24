package ma.emsi.gounidimeryem.tp2_meryemgounidi.llm;

public class Test1 {
    public static void main(String[] args) {
        // Créer une instance de LlmClient
        LlmClient client = new LlmClient();
        
        // Définir le rôle système
        client.setSystemRole("Tu es un assistant amical et serviable qui répond de manière concise.");
        
        // Test avec une question simple
        String question = "Quelle est la capitale du Maroc?";
        System.out.println("Question: " + question);
        
        try {
            String reponse = client.askQuestion(question);
            System.out.println("Réponse: " + reponse);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'appel au LLM: " + e.getMessage());
            e.printStackTrace();
        }
    }
}