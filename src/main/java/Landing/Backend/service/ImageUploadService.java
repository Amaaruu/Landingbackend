package Landing.Backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import Landing.Backend.exception.BusinessLogicException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageUploadService {

    private final Cloudinary cloudinary;

    @Value("${cloudinary.upload-folder}")
    private String uploadFolder;

    @Value("${cloudinary.max-file-size-mb:5}")
    private int maxFileSizeMb;

    private static final List<String> ALLOWED_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif"
    );

    public String uploadImage(MultipartFile file, String context) {
        validateFile(file);

        try {
            String publicId = buildPublicId(context);
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadOptions = ObjectUtils.asMap(
                "public_id",    publicId,
                "folder",       uploadFolder,
                "overwrite",    false,
                "quality",      "auto:good",
                "fetch_format", "auto",
                "resource_type","image"
            );

            log.info("[ImageUploadService] Iniciando upload — context: {}, publicId: {}", context, publicId);

            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) cloudinary.uploader().upload(
                file.getBytes(),
                uploadOptions
            );

            String secureUrl = (String) result.get("secure_url");

            if (secureUrl == null || secureUrl.isBlank()) {
                log.error("[ImageUploadService] Cloudinary no devolvió secure_url. Respuesta completa: {}", result);
                throw new BusinessLogicException(
                    "El CDN no devolvió una URL válida. Intenta de nuevo.",
                    HttpStatus.INTERNAL_SERVER_ERROR
                );
            }

            log.info("[ImageUploadService] Upload exitoso — url: {}", secureUrl);
            return secureUrl;

        } catch (BusinessLogicException e) {
            throw e;

        } catch (IOException e) {
            log.error("[ImageUploadService] IOException — mensaje: {}", e.getMessage(), e);
            throw new BusinessLogicException(
                "Error de conexión al subir la imagen. Intenta de nuevo.",
                HttpStatus.INTERNAL_SERVER_ERROR
            );

        } catch (Exception e) {
            log.error("[ImageUploadService] Error inesperado — tipo: {}, mensaje: {}",
                e.getClass().getSimpleName(), e.getMessage(), e);
            if (e.getCause() != null) {
                log.error("[ImageUploadService] Causa raíz: {}", e.getCause().getMessage());
            }
            throw new BusinessLogicException(
                "Error al procesar la imagen. Verifica la configuración del CDN.",
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    public void deleteImageByUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return;
        try {
            String publicId = extractPublicIdFromUrl(imageUrl);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("[ImageUploadService] Imagen eliminada del CDN: {}", publicId);
        } catch (Exception e) {
            log.warn("[ImageUploadService] No se pudo eliminar imagen: {} — {}", imageUrl, e.getMessage());
        }
    }

    // ── Validaciones ──────────────────────────────────────────────────────────

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessLogicException(
                "El archivo de imagen está vacío.",
                HttpStatus.BAD_REQUEST
            );
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessLogicException(
                "Tipo de archivo no permitido. Solo se aceptan: JPG, PNG, WEBP, GIF.",
                HttpStatus.BAD_REQUEST
            );
        }

        long maxBytes = (long) maxFileSizeMb * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new BusinessLogicException(
                "El archivo supera el tamaño máximo de " + maxFileSizeMb + "MB.",
                HttpStatus.BAD_REQUEST
            );
        }
    }

    private String buildPublicId(String context) {
        String safeContext = (context != null ? context : "general")
            .replaceAll("[^a-zA-Z0-9_-]", "_")
            .toLowerCase();
        return safeContext + "_" + UUID.randomUUID().toString().replace("-", "");
    }

    private String extractPublicIdFromUrl(String url) {
        int uploadIdx = url.indexOf("/upload/");
        if (uploadIdx == -1) return url;
        String path = url.substring(uploadIdx + 8);
        path = path.replaceFirst("^v\\d+/", "");
        int dotIdx = path.lastIndexOf('.');
        if (dotIdx > 0) path = path.substring(0, dotIdx);
        return path;
    }
}