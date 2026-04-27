package com.inkwell.web.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class MediaClient {

    private final RestTemplate rest;

    @Value("${app.services.media-service.url}")
    private String baseUrl;

    public MediaClient(RestTemplate rest) { this.rest = rest; }

    public List<Map<String, Object>> getMediaByUploaderEmail(String email) {
        return rest.exchange(baseUrl + "/media/uploader/email?email=" + email,
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}).getBody();
    }

    public List<Map<String, Object>> getAllMedia() {
        return rest.exchange(baseUrl + "/media/admin/all",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}).getBody();
    }

    @SuppressWarnings("unchecked")
    public void uploadMedia(MultipartFile file, String uploaderEmail) {
        try {
            byte[] bytes = file.getBytes();
            String originalFilename = file.getOriginalFilename();

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(bytes) {
                @Override
                public String getFilename() {
                    return originalFilename != null ? originalFilename : "upload";
                }
            });

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            rest.postForObject(baseUrl + "/media/upload",
                    new HttpEntity<>(body, headers), Map.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read upload file: " + e.getMessage(), e);
        }
    }

    public void deleteMedia(Integer mediaId, String uploaderEmail) {
        rest.delete(baseUrl + "/media/" + mediaId);
    }

    public void adminDeleteMedia(Integer mediaId) {
        rest.delete(baseUrl + "/media/" + mediaId);
    }
}
