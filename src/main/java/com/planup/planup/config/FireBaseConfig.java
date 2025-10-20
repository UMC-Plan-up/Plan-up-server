package com.planup.planup.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FireBaseConfig {

    @Bean
    public FirebaseApp firebaseApp(@Value("${firebase.credentials-json:}") String credentialsJsonEnv) throws IOException {
        InputStream credsStream;

        if (!credentialsJsonEnv.isBlank()) {
            // 환경변수/Config에 문자열로 들어온 JSON
            credsStream = new ByteArrayInputStream(credentialsJsonEnv.getBytes(StandardCharsets.UTF_8));
        } else {
            // 또는 파일 경로를 쓰고 싶다면: -DFIREBASE_CREDENTIALS_PATH=/path/creds.json
            String path = System.getProperty("FIREBASE_CREDENTIALS_PATH", "");
            credsStream = new FileInputStream(path);
        }

        GoogleCredentials credentials = GoogleCredentials.fromStream(credsStream);
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build();

        // 중복 초기화 방지
        if (FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.initializeApp(options);
        }
        return FirebaseApp.getInstance();
    }
}
