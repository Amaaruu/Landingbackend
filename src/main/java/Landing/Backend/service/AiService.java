package Landing.Backend.service;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import Landing.Backend.dto.AiResponseDTO;
import Landing.Backend.model.LandingProject;

@Service
public class AiService {

    private final RestClient restClient;

    public AiService(@Value("${python.api.url}") String apiUrl) {
        // Blindaje contra cuelgues infinitos si Python falla
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000); // 10 segundos para conectar
        factory.setReadTimeout(60000);    // 60 segundos esperando respuesta de Claude/GPT

        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .baseUrl(apiUrl)
                .build();
    }

    public AiResponseDTO requestLandingGeneration(LandingProject project, String userPlan) {
        
        Map<String, Object> requestPayload = new HashMap<>();
        
        requestPayload.put("projectId", project.getProjectId());
        requestPayload.put("userPlan", userPlan);
        requestPayload.put("projectName", project.getProjectName());
        requestPayload.put("projectIdea", project.getProjectIdea());
        requestPayload.put("callToAction", project.getCallToAction());
        requestPayload.put("businessSector", project.getBusinessSector());
        requestPayload.put("communicationTone", project.getCommunicationTone());

        if (project.getDesignPreferences() != null) {
            Map<String, Object> prefs = project.getDesignPreferences();
            // Prevención de NullPointerExceptions hacia FastAPI
            requestPayload.put("colorPalette", prefs.getOrDefault("colorPalette", "default"));
            requestPayload.put("visualStyle", prefs.getOrDefault("visualStyle", "minimalist"));
            requestPayload.put("animationLevel", prefs.getOrDefault("animationLevel", "medium"));
            requestPayload.put("customPrompt", prefs.getOrDefault("customPrompt", ""));
        }

        System.out.println("🚀 Solicitando IA a QA Server para: " + project.getProjectName() + " | Plan: " + userPlan);
        
        return restClient.post()
                .uri("") 
                .body(requestPayload)
                .retrieve()
                .body(AiResponseDTO.class);
    }
}