package com.planup.planup.domain.notification.infra.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Configuration
public class FireBaseConfig {

    @Bean
    public FirebaseApp firebaseApp(
            @Value("${firebase.credentials-json:}") String credentialsJson,
            @Value("${firebase.credentials-path:}") String credentialsPath
    ) throws IOException {

        // 이미 초기화되어 있으면 그대로 사용
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        try (InputStream credsStream = resolveCredentialsStream(credentialsJson, credentialsPath)) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(credsStream);
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();
            return FirebaseApp.initializeApp(options);
        }
    }

    private InputStream resolveCredentialsStream(String credentialsJson, String credentialsPath) throws IOException {
        // 1) 환경변수/설정으로 JSON 문자열이 들어온 경우 (우선)
        if (credentialsJson != null && !credentialsJson.isBlank()) {
            return new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8));
        }

        // 2) 파일 경로로 주는 경우
        if (credentialsPath != null && !credentialsPath.isBlank()) {
            File f = new File(credentialsPath);
            if (!f.exists()) {
                throw new FileNotFoundException("Firebase credentials file not found: " + credentialsPath);
            }
            return new FileInputStream(f);
        }

        // 둘 다 없으면 명확하게 실패
        throw new IllegalStateException(
                "Firebase credentials not configured. " +
                        "Set either 'firebase.credentials-json' or 'firebase.credentials-path'."
        );
    }
}
