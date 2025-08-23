package com.planup.planup.domain.global.message;

import com.planup.planup.validation.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/encourage")
public class EncourageMessageController {

    private final EncouragementService service;

    public EncourageMessageController(EncouragementService service) {
        this.service = service;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<MessageResponse> create(@Parameter(hidden = true) @CurrentUser Long userId) {
        return service.generate(userId);
    }
}
