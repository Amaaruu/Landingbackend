package Landing.Backend.service;

import java.util.HashMap;
import java.util.Map;

// Importante: añadir esta línea para poder inyectar variables
import org.springframework.beans.factory.annotation.Value; 
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import Landing.Backend.dto.AiResponseDTO;
import Landing.Backend.model.LandingProject;

@Service
public class AiService {

    private final RestClient restClient;

    // Aquí le decimos a Spring: "Ve a buscar la variable python.api.url a las properties y métela aquí"
    public AiService(@Value("${python.api.url}") String apiUrl) {
        
        // Ahora usamos la variable apiUrl en lugar de la URL quemada
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
        requestPayload.put("colorPalette", project.getColorPalette() != null ? project.getColorPalette().toString() : "");
        requestPayload.put("userPlan", userPlan);

        System.out.println("🚀 Enviando petición a FastAPI para el proyecto: " + project.getProjectName());
        
        return restClient.post()
                // Aquí quitamos el /api/v1/ai/generate porque ya lo pusimos completo en el .env
                .uri("") 
                .body(requestPayload)
                .retrieve()
                .body(AiResponseDTO.class);
    }
}