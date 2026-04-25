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
        requestPayload.put("projectId", project.getProjectId());
        requestPayload.put("projectName", project.getProjectName());
        requestPayload.put("businessSector", project.getBusinessSector());
        requestPayload.put("communicationTone", project.getCommunicationTone());
        // Enviamos las preferencias de diseño como texto para que el prompt de Python las analice
        requestPayload.put("colorPalette", project.getDesignPreferences() != null ? project.getDesignPreferences().toString() : "");
        requestPayload.put("userPlan", userPlan);

        System.out.println("🚀 Solicitando IA para: " + project.getProjectName() + " | Plan: " + userPlan);
        
        return restClient.post()
                .uri("") 
                .body(requestPayload)
                .retrieve()
                .body(AiResponseDTO.class);
    }
}