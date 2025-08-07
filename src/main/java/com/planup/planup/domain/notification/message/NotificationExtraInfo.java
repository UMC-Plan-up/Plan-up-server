package com.planup.planup.domain.notification.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class NotificationExtraInfo {

    String targetName;
    List<String> updatedPart;
}
