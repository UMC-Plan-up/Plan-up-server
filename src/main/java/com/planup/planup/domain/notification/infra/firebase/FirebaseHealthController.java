package com.planup.planup.domain.notification.infra.firebase;

import com.google.firebase.FirebaseApp;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FirebaseHealthController {

    @GetMapping("/internal/firebase")
    public String firebaseHealth() {
        return FirebaseApp.getApps().isEmpty() ? "NOT_INITIALIZED" : "OK";
    }
}
