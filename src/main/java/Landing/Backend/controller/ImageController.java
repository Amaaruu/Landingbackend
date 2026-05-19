package Landing.Backend.controller;

import Landing.Backend.dto.ImageUploadResponseDTO;
import Landing.Backend.service.ImageUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

        String userContext = authentication.getName().split("@")[0] + "_" + context;
        String imageUrl    = imageUploadService.uploadImage(file, userContext);

        return ResponseEntity.ok(new ImageUploadResponseDTO(
            imageUrl,
            null,
            "Imagen subida exitosamente"
        ));
    }
}