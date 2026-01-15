package com.planup.planup.domain.notification.infra.firebase;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FirebaseHealthController {

    @GetMapping("/internal/firebase")
    public String firebaseHealth() {
        return FirebaseApp.getApps().isEmpty() ? "NOT_INITIALIZED" : "OK";
    }

    @PostMapping("/internal/firebase/verify")
    public String verify(@RequestBody String idToken) throws Exception {
        FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(idToken.trim());
        return "uid=" + decoded.getUid();
    }
}
