package ma.emsi.gounidimeryem.tp2_meryemgounidi.jsf;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import ma.emsi.gounidimeryem.tp2_meryemgounidi.llm.LlmClient;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Backing bean pour la page JSF index.xhtml.
 * Portée view pour conserver l'état de la conversation qui dure pendant plusieurs requêtes HTTP.
 * La portée view nécessite l'implémentation de Serializable.
 * 
 * TP2 - Utilisation de LangChain4j pour les interactions avec le LLM
 */
@Named("bb")
@ViewScoped
public class Bb implements Serializable {

    private static final long serialVersionUID = 1L;

    private String roleSysteme;
    private boolean roleSystemeChangeable = true;
    private List<SelectItem> listeRolesSysteme;
    private String question;
    private String reponse;
    private StringBuilder conversation = new StringBuilder();
    private LlmClient llmClient;

    @Inject
    private FacesContext facesContext;

    public Bb() {
    }

    @PostConstruct
    public void init() {
        try {
            this.llmClient = new LlmClient();
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation de LlmClient : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String getRoleSysteme() {
        return roleSysteme;
    }

    public void setRoleSysteme(String roleSysteme) {
        this.roleSysteme = roleSysteme;
    }

    public boolean isRoleSystemeChangeable() {
        return roleSystemeChangeable;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getReponse() {
        return reponse;
    }

    public void setReponse(String reponse) {
        this.reponse = reponse;
    }

    public String getConversation() {
        return conversation.toString();
    }

    public void setConversation(String conversation) {
        this.conversation = new StringBuilder(conversation);
    }

    public List<SelectItem> getRolesSysteme() {
        if (this.listeRolesSysteme == null) {
            this.listeRolesSysteme = new ArrayList<>();
            
            String role = "You are a helpful assistant. You help the user to find the information they need. If the user type a question, you answer it.";
            this.listeRolesSysteme.add(new SelectItem(role, "Assistant"));

            role = "You are an interpreter. You translate from English to French and from French to English. If the user type a French text, you translate it into English. If the user type an English text, you translate it into French. If the text contains only one to three words, give some examples of usage of these words in English.";
            this.listeRolesSysteme.add(new SelectItem(role, "Traducteur Anglais-Français"));

            role = "Your are a travel guide. If the user type the name of a country or of a town, you tell them what are the main places to visit in the country or the town are you tell them the average price of a meal.";
            this.listeRolesSysteme.add(new SelectItem(role, "Guide touristique"));

            role = "You are Chef Simplissimo, inventor of '3-word cooking'. For each recipe: 1. Start with 'SIMPLISSIME:' 2. Give title in 3 words maximum 3. List max 5 ingredients 4. Describe steps in 3 sentences maximum 5. End with one tip in 4 words Use ultra-simple language, no technical terms. If given specific ingredients, suggest one quick recipe using them.";
            this.listeRolesSysteme.add(new SelectItem(role, "Chef Simplissimo"));
        }

        return this.listeRolesSysteme;
    }

    public String envoyer() {
        if (question == null || question.isBlank()) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Texte question vide", "Il manque le texte de la question");
            facesContext.addMessage(null, message);
            return null;
        }

        try {
            if (this.conversation.isEmpty()) {
                llmClient.setSystemRole(roleSysteme);
                this.roleSystemeChangeable = false;
            }

            this.reponse = llmClient.askQuestion(question);

        } catch (Exception e) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Problème de connexion avec l'API du LLM", 
                    "Erreur : " + e.getMessage());
            facesContext.addMessage(null, message);
            return null;
        }

        afficherConversation();
        return null;
    }

    public String nouveauChat() {
        return "index";
    }

    private void afficherConversation() {
        this.conversation.append("== User:\n").append(question).append("\n== IA:\n").append(reponse).append("\n\n");
    }
}
