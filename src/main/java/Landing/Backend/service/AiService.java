package Landing.Backend.service;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import Landing.Backend.dto.AiResponseDTO;
import Landing.Backend.model.LandingProject;

@Service
public class AiService {

    private final RestClient restClient;

    public AiService(@Value("${python.api.url}") String apiUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(apiUrl)
                .build();
    }

    public AiResponseDTO requestLandingGeneration(LandingProject project, String userPlan) {
        
        Map<String, Object> requestPayload = new HashMap<>();
        
        //Campos Base
        requestPayload.put("projectId", project.getProjectId());
        requestPayload.put("userPlan", userPlan);
        requestPayload.put("projectName", project.getProjectName());
        requestPayload.put("projectIdea", project.getProjectIdea());
        requestPayload.put("callToAction", project.getCallToAction());
        requestPayload.put("businessSector", project.getBusinessSector());
        requestPayload.put("communicationTone", project.getCommunicationTone());

        //Desempaquetar los campos Premium desde el Map de diseño
        if (project.getDesignPreferences() != null) {
            Map<String, Object> prefs = project.getDesignPreferences();
            requestPayload.put("colorPalette", prefs.get("colorPalette"));
            requestPayload.put("visualStyle", prefs.get("visualStyle"));
            requestPayload.put("animationLevel", prefs.get("animationLevel"));
            requestPayload.put("customPrompt", prefs.get("customPrompt"));
        }

        System.out.println("Solicitando IA para: " + project.getProjectName() + " | Plan: " + userPlan);
        
        return restClient.post()
                .uri("") 
                .body(requestPayload)
                .retrieve()
                .body(AiResponseDTO.class);
    }
}