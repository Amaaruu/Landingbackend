package Landing.Backend.controller;

import Landing.Backend.dto.ImageUploadResponseDTO;
import Landing.Backend.exception.BusinessLogicException;
import Landing.Backend.service.ImageUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
@Tag(name = "Images", description = "Gestión de imágenes para proyectos")
public class ImageController {

    private final ImageUploadService imageUploadService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Subir imagen al CDN")
    public ResponseEntity<ImageUploadResponseDTO> uploadImage(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "context", defaultValue = "project") String context,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("[ImageController] Intento de upload sin autenticación válida.");
            throw new BusinessLogicException(
                "Sesión inválida o expirada. Por favor vuelve a iniciar sesión.",
                HttpStatus.UNAUTHORIZED
            );
        }

        String userName;
        try {
            userName = authentication.getName().split("@")[0];
        } catch (Exception e) {
            log.warn("[ImageController] No se pudo extraer nombre del usuario autenticado.");
            userName = "user";
        }

        String userContext = userName + "_" + context;
        log.info("[ImageController] Upload solicitado — user: {}, context: {}", userName, context);

        String imageUrl = imageUploadService.uploadImage(file, userContext);

        return ResponseEntity.ok(new ImageUploadResponseDTO(
            imageUrl,
            null,
            "Imagen subida exitosamente"
        ));
    }
}