package com.planup.planup.domain.user.service.util;

public class EmailTemplateUtil {

    public static String createSuccessHtml(String email, String deepLinkUrl) {
        return """
        <!DOCTYPE html>
        <html lang="ko">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Plan-Up 이메일 인증</title>
            <style>
                body {
                    margin: 0;
                    padding: 20px;
                    font-family: Arial, sans-serif;
                    background-color: #f5f5f5;
                }
                .container {
                    max-width: 600px;
                    margin: 0 auto;
                    padding: 20px;
                    background: white;
                    border-radius: 8px;
                    box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                }
                .header {
                    text-align: center;
                    margin-bottom: 30px;
                }
                .header h1 {
                    color: #4285f4;
                    font-size: 2rem;
                    margin: 0;
                }
                .content {
                    text-align: center;
                }
                h2 {
                    color: #333;
                    margin-bottom: 20px;
                    font-size: 1.5rem;
                }
                p {
                    color: #666;
                    line-height: 1.6;
                    margin-bottom: 20px;
                    font-size: 1rem;
                }
                .footer {
                    margin-top: 40px;
                    padding-top: 20px;
                    border-top: 1px solid #eee;
                }
                .footer p {
                    color: #999;
                    font-size: 14px;
                    margin: 5px 0;
                }
            </style>
            <script>
                // 3초 후 자동으로 앱으로 리다이렉트
                console.log("딥링크 URL:", "%s");
                setTimeout(function() {
                    console.log("딥링크 실행 시도...");
                    window.location.href = "%s";
                }, 3000);
            </script>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>Plan-Up</h1>
                </div>
                <div class="content">
                    <h2>이메일 인증 완료</h2>
                    <p>
                        인증이 성공하였습니다!<br>
                        잠시 후 Plan-Up 앱으로 자동 이동합니다.
                    </p>
                </div>
                <div class="footer">
                    <p>* 자동으로 앱이 열리지 않는다면 Plan-Up 앱을 직접 실행해주세요.</p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(deepLinkUrl, deepLinkUrl);
    }

    public static String createFailureHtml() {
        return """
        <!DOCTYPE html>
        <html lang="ko">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Plan-Up 이메일 인증</title>
            <style>
                body {
                    margin: 0;
                    padding: 20px;
                    font-family: Arial, sans-serif;
                    background-color: #f5f5f5;
                }
                .container {
                    max-width: 600px;
                    margin: 0 auto;
                    padding: 20px;
                    background: white;
                    border-radius: 8px;
                    box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                }
                .header {
                    text-align: center;
                    margin-bottom: 30px;
                }
                .header h1 {
                    color: #4285f4;
                    font-size: 2rem;
                    margin: 0;
                }
                .content {
                    text-align: center;
                }
                h2 {
                    color: #333;
                    margin-bottom: 20px;
                    font-size: 1.5rem;
                }
                p {
                    color: #666;
                    line-height: 1.6;
                    margin-bottom: 20px;
                    font-size: 1rem;
                }
                .footer {
                    margin-top: 40px;
                    padding-top: 20px;
                    border-top: 1px solid #eee;
                }
                .footer p {
                    color: #999;
                    font-size: 14px;
                    margin: 5px 0;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>Plan-Up</h1>
                </div>
                <div class="content">
                    <h2>이메일 인증 실패</h2>
                    <p>
                        인증에 실패하였습니다.<br>
                        Plan-Up 앱에서 다시 시도해주세요.
                    </p>
                </div>
                <div class="footer">
                    <p>* 링크가 만료되었거나 올바르지 않습니다.</p>
                    <p>* 앱에서 인증 메일을 다시 요청해주세요.</p>
                </div>
            </div>
        </body>
        </html>
        """;
    }
}
