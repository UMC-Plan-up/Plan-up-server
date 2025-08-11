package com.planup.planup.domain.user.controller;

import com.planup.planup.domain.user.dto.KakaoLoginResponseDTO;
import com.planup.planup.domain.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class KakaoTestController {

    private final UserService userService;

    public KakaoTestController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/kakao/login")
    public String kakaoLoginTestPage() {
        String clientId = "8db3b814026a2886d8323311e2af5a9f";
        String redirectUri = "http://localhost:8080/test/kakao/callback";

        String kakaoAuthUrl = String.format(
                "https://kauth.kakao.com/oauth/authorize?client_id=%s&redirect_uri=%s&response_type=code",
                clientId, redirectUri
        );

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head><title>카카오 로그인 테스트</title></head>
            <body>
                <h1>카카오 로그인 테스트</h1>
                <a href="%s">카카오 로그인</a>
            </body>
            </html>
            """, kakaoAuthUrl);
    }

    @GetMapping("/kakao/callback")
    public ResponseEntity<String> kakaoCallback(@RequestParam String code) {
        try {
            KakaoLoginResponseDTO result = userService.kakaoLogin(code);

            return ResponseEntity.ok(String.format("""
            <html><body>
                <h1>카카오 로그인 결과</h1>
                <p><strong>상태:</strong> %s</p>
                <p><strong>이메일:</strong> %s</p>
                <p><strong>임시토큰:</strong> %s</p>
                <p><strong>액세스토큰:</strong> %s</p>
                <p><strong>닉네임:</strong> %s</p>
            </body></html>
            """,
                    result.getStatus(),
                    result.getEmail(),
                    result.getTempToken(),
                    result.getAccessToken(),
                    result.getNickname()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("오류: " + e.getMessage());
        }
    }

    @GetMapping("/kakao/signup")
    public String kakaoSignupTestPage() {
        return """
        <html>
        <body>
            <h1>카카오 회원가입 테스트</h1>
            <form onsubmit="submitSignup(event)" style="max-width: 400px; margin: 20px;">
                
                <h3>임시 토큰:</h3>
                <textarea id="tempToken" rows="3" style="width: 100%;" required>eyJhbGciOiJIUzUxMiJ9.eyJyb2xlIjoiVEVNUCIsInVzZXJJZCI6MCwic3ViIjoia2RoMDIxMjA2QG5hdmVyLmNvbSIsImlhdCI6MTc1NDg4Mzk4MSwiZXhwIjoxNzU0OTcwMzgxfQ.xzhlzfRAifAi4bEVGJVKxa8E261SGjDU8toCtA9Jvr7UKVWJL-be5pbtol61X03FJrfNAOCWTk-3-JqT9ZCi7A</textarea>
                
                <h3>닉네임:</h3>
                <input id="nickname" placeholder="원하는 닉네임 입력" required style="width: 100%; padding: 8px;">
                
                <br><br>
                <button type="submit" style="padding: 10px 20px; background: #007bff; color: white; border: none;">
                    회원가입 완료
                </button>
            </form>
            
            <div id="result" style="margin-top: 20px;"></div>
            
            <script>
                function submitSignup(event) {
                    event.preventDefault();
                    
                    const data = {
                        tempToken: document.getElementById('tempToken').value,
                        nickname: document.getElementById('nickname').value
                    };
                    
                    fetch('/users/oauth/kakao/signup', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify(data)
                    })
                    .then(response => response.json())
                    .then(data => {
                        document.getElementById('result').innerHTML = 
                            '<h3>결과:</h3><pre>' + JSON.stringify(data, null, 2) + '</pre>';
                    })
                    .catch(error => {
                        document.getElementById('result').innerHTML = 
                            '<h3>오류:</h3>' + error.message;
                    });
                }
            </script>
        </body>
        </html>
        """;
    }
}