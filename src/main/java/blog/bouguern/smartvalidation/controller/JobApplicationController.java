package blog.bouguern.smartvalidation.controller;

import blog.bouguern.smartvalidation.dto.JobApplicationDTO;
import blog.bouguern.smartvalidation.service.JobApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    @PostMapping
    public ResponseEntity<Map<String, String>> submitApplication(
            @Valid @RequestBody JobApplicationDTO dto,
            HttpServletRequest request) {

        String applicationId = jobApplicationService.processApplication(dto);
        URI location = URI.create(request.getRequestURI() + "/" + applicationId);

        return ResponseEntity
                .created(location)
                .body(Map.of(
                        "status",        "success",
                        "applicationId", applicationId,
                        "message",       "Your application has been received"
                ));
    }
}