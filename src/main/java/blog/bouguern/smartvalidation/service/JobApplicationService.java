package blog.bouguern.smartvalidation.service;

import blog.bouguern.smartvalidation.dto.JobApplicationDTO;
import blog.bouguern.smartvalidation.exception.AiValidationException;
import blog.bouguern.smartvalidation.validation.AiValidationResponse;
import blog.bouguern.smartvalidation.validation.AiValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobApplicationService {

    private final AiValidator aiValidator;

    public String processApplication(JobApplicationDTO dto) {
        log.info("Processing application for: {}", dto.getFullName());

        AiValidationResponse response = aiValidator.validate(dto);

        if (!response.isValid()) {
            log.warn("AI validation failed for: {}", dto.getEmail());
            throw new AiValidationException(response);
        }

        log.info("Application from {} passed all validations.", dto.getEmail());

        return UUID.randomUUID().toString();
    }
}