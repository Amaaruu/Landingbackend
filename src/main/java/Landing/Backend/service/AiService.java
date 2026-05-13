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
        requestPayload.put("businessSector",   safe(project.getBusinessSector()));
        requestPayload.put("communicationTone", safe(project.getCommunicationTone()));

        // ── Campos nuevos — Plan Básico ───────────────────────────────────────
        requestPayload.put("landingGoal",      safe(project.getLandingGoal()));
        requestPayload.put("targetAudience",   safe(project.getTargetAudience()));
        requestPayload.put("brandPositioning", safe(project.getBrandPositioning()));
        requestPayload.put("brandStage",       safe(project.getBrandStage()));
        requestPayload.put("valueProposition", safe(project.getValueProposition()));

        // ── Campo nuevo — Plan Intermedio ─────────────────────────────────────
        requestPayload.put("formalityLevel", safe(project.getFormalityLevel()));

        Map<String, Object> prefs = project.getDesignPreferences() != null
                ? project.getDesignPreferences()
                : new HashMap<>();

        // Intermedio+
        requestPayload.put("primaryColor",        safeMap(prefs, "primaryColor",        "azul-marino"));
        requestPayload.put("secondaryColor",       safeMap(prefs, "secondaryColor",      "blanco"));
        requestPayload.put("baseMode",             safeMap(prefs, "baseMode",            "claro"));
        requestPayload.put("contrastLevel",        safeMap(prefs, "contrastLevel",       "estandar"));
        requestPayload.put("visualStyle",          safeMap(prefs, "visualStyle",         "moderno"));
        requestPayload.put("typographyHierarchy",  safeMap(prefs, "typographyHierarchy", "equilibrada"));
        requestPayload.put("visualDensity",        safeMap(prefs, "visualDensity",       "equilibrado"));
        requestPayload.put("sectionDividers",      safeMap(prefs, "sectionDividers",     "limpia"));
        requestPayload.put("sections",             safeMap(prefs, "sections",            "hero,features,footer"));

        // Premium
        requestPayload.put("typographyStyle",  safeMap(prefs, "typographyStyle",  "sans-humanista"));
        requestPayload.put("buttonShape",      safeMap(prefs, "buttonShape",      "redondeado"));
        requestPayload.put("buttonStyle",      safeMap(prefs, "buttonStyle",      "solido"));
        requestPayload.put("iconStyle",        safeMap(prefs, "iconStyle",        "outline"));
        requestPayload.put("layoutType",       safeMap(prefs, "layoutType",       "centrado"));
        requestPayload.put("creativityLevel",  safeMap(prefs, "creativityLevel",  "equilibrada"));
        requestPayload.put("animationLevel",   safeMap(prefs, "animationLevel",   "sutil"));
        requestPayload.put("scrollEffect",     safeMap(prefs, "scrollEffect",     "fade-in"));
        requestPayload.put("heroEffect",       safeMap(prefs, "heroEffect",       "ninguno"));
        requestPayload.put("hoverIntensity",   safeMap(prefs, "hoverIntensity",   "sutil"));
        requestPayload.put("contentDensity",   safeMap(prefs, "contentDensity",   "equilibrado"));

        System.out.println("[AiService] Enviando a Python AI | Proyecto: "
                + project.getProjectName() + " | Plan: " + userPlan
                + " | ProjectId: " + project.getProjectId());


        return restClient.post()
                .uri("/api/v1/ai/generate")
                .body(requestPayload)
                .retrieve()
                .body(AiResponseDTO.class);
    }

    private String safe(String value) {
        return value != null ? value : "";
    }

    private String safeMap(Map<String, Object> map, String key, String defaultValue) {
        Object val = map.get(key);
        return (val != null && !val.toString().isBlank()) ? val.toString() : defaultValue;
    }
}