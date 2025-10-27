package ma.emsi.gounidimeryem.tp2_meryemgounidi.tools;

import dev.langchain4j.agent.tool.Tool;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.util.Scanner;
import java.net.URI;

public class MeteoTool {
    @Tool("Donne la météo d'une ville")
    public String donneMeteo(String ville) {
        try {
            // Construire l'URL de l'API météo avec la ville demandée
            String apiUri = "https://wttr.in/" + ville + "?format=3";

            // Ouvrir une connexion HTTP
            HttpURLConnection connection = (HttpURLConnection) new URI(apiUri).toURL().openConnection();
            connection.setRequestMethod("GET");

            // Lire la réponse
            Scanner scanner = new Scanner(connection.getInputStream());
            String response = scanner.useDelimiter("\\A").next();
            scanner.close();

            return "Météo actuelle à " + ville + " : " + response;
        } catch (IOException e) {
            return "Erreur lors de la récupération de la météo pour " + ville + " : " + e.getMessage();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}