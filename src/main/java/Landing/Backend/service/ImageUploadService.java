package Landing.Backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import Landing.Backend.exception.BusinessLogicException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
            Map<String, Object> result = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                    "public_id",       publicId,
                    "folder",          uploadFolder,
                    "overwrite",       false,
                    "eager",           "w_1920,h_1080,c_limit,f_webp,q_auto:good",
                    "eager_async",     false,
                    "quality",         "auto:good",
                    "fetch_format",    "auto"
                )
            );

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> eager = (List<Map<String, Object>>) result.get("eager");
            if (eager != null && !eager.isEmpty()) {
                return (String) eager.get(0).get("secure_url");
            }

            return (String) result.get("secure_url");

        } catch (IOException e) {
            throw new BusinessLogicException(
                "Error al subir la imagen. Intenta de nuevo.",
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    public void deleteImageByUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return;
        try {
            String publicId = extractPublicIdFromUrl(imageUrl);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            System.err.println("[ImageUploadService] Advertencia: no se pudo eliminar imagen: " + imageUrl);
        }
    }

    // ── Validaciones
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessLogicException("El archivo de imagen está vacío.", HttpStatus.BAD_REQUEST);
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
        // Contexto sanitizado + UUID para evitar colisiones y path traversal
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