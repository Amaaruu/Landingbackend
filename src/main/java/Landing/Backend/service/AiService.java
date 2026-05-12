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
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(60_000);
        factory.setReadTimeout(180_000);

        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .baseUrl(apiUrl)
                .build();
    }

    public AiResponseDTO requestLandingGeneration(LandingProject project, String userPlan) {

        Map<String, Object> requestPayload = new HashMap<>();

        requestPayload.put("projectId",    project.getProjectId());
        requestPayload.put("userPlan",     userPlan);
        requestPayload.put("projectName",  project.getProjectName());
        requestPayload.put("projectIdea",  project.getProjectIdea());
        requestPayload.put("callToAction", project.getCallToAction());

        requestPayload.put("businessSector",    project.getBusinessSector()    != null ? project.getBusinessSector()    : "");
        requestPayload.put("communicationTone", project.getCommunicationTone() != null ? project.getCommunicationTone() : "");

        if (project.getDesignPreferences() != null) {
            Map<String, Object> prefs = project.getDesignPreferences();
            requestPayload.put("colorPalette",   prefs.getOrDefault("colorPalette",   "Modern Blue"));
            requestPayload.put("visualStyle",    prefs.getOrDefault("visualStyle",    "minimalist"));
            requestPayload.put("animationLevel", prefs.getOrDefault("animationLevel", "medium"));
            requestPayload.put("customPrompt",   prefs.getOrDefault("customPrompt",   ""));
        } else {
            requestPayload.put("colorPalette",   "Modern Blue");
            requestPayload.put("visualStyle",    "minimalist");
            requestPayload.put("animationLevel", "medium");
            requestPayload.put("customPrompt",   "");
        }

        System.out.println("Enviando solicitud a Python AI | Proyecto: "
                + project.getProjectName() + " | Plan: " + userPlan
                + " | ProjectId: " + project.getProjectId());

        return restClient.post()
                .uri("/api/v1/ai/generate")
                .body(requestPayload)
                .retrieve()
                .body(AiResponseDTO.class);
    }
}