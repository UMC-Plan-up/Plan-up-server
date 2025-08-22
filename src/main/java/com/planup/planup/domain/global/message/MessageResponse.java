package com.planup.planup.domain.global.message;

import lombok.Getter;
import org.springframework.web.bind.annotation.GetMapping;

public record MessageResponse (String message) {
}
