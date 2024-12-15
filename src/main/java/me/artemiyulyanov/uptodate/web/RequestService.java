package me.artemiyulyanov.uptodate.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

@Service
public class RequestService {
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.findAndRegisterModules();

        return objectMapper;
    }

    public String mapToJson(Map<String, Object> params) {
        try {
            return objectMapper().writeValueAsString(params);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ResponseEntity<ServerResponse> executeError(HttpStatus status, int code, String error) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ErrorResponse
                        .builder()
                        .code(code)
                        .error(error)
                        .build()
                );
    }

    @Deprecated
    public ResponseEntity<ServerResponse> executeCustomTemplate(HttpStatus status, int code, String message, Map<String, Object> template) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(CustomResponse
                        .builder()
                        .code(code)
                        .message(message)
                        .response(template)
                        .build()
                );
    }

    public ResponseEntity<ServerResponse> executeEntity(HttpStatus status, int code, String message, Object entity) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ServerResponse
                        .builder()
                        .code(code)
                        .message(message)
                        .response(entity)
                        .build()
                );
    }

    public <T> ResponseEntity<ServerResponse> executePaginatedEntity(HttpStatus status, int code, Page<T> entity) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(PaginatedResponse
                        .builder()
                        .totalPages(entity.getTotalPages())
                        .page(entity.getNumber())
                        .size(entity.getSize())
                        .totalElements(entity.getTotalElements())
                        .last(entity.isLast())
                        .code(code)
                        .response(entity.getContent())
                        .build()
                );
    }

    public ResponseEntity<ServerResponse> executeMessage(HttpStatus status, int code, String message) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(MessageResponse
                        .builder()
                        .code(code)
                        .message(message)
                        .build()
                );
    }

    public ResponseEntity<ServerResponse> executeTemplate(HttpStatus status, int code, String message, Map<String, Object> response) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ServerResponse
                        .builder()
                        .code(code)
                        .message(message)
                        .response(response)
                        .build()
                );
    }

    public ResponseEntity<byte[]> executeImage(HttpStatus status, MediaType mediaType, byte[] image) {
        return ResponseEntity.status(status)
                .contentType(mediaType)
                .contentLength(image.length)
                .body(image);
    }
}