package com.learn.oauth2flow;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

@RestController
public class HomeController {

    @GetMapping
    public String home(OAuth2AuthenticationToken authentication,HttpServletRequest request) {

        // Check if user is logged in
        if (authentication == null) {
            // Show login button for non-authenticated users
            return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>ECOM - Home</title>
                    <style>
                        body {
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, sans-serif;
                            max-width: 800px;
                            margin: 50px auto;
                            padding: 20px;
                            background: #f5f5f5;
                        }
                        .container {
                            background: white;
                            padding: 30px;
                            border-radius: 8px;
                            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                            text-align: center;
                        }
                        h1 {
                            color: #333;
                            margin-top: 0;
                        }
                        .welcome-text {
                            color: #666;
                            margin: 20px 0;
                            line-height: 1.6;
                        }
                        .btn-primary {
                            padding: 12px 24px;
                            background: #007bff;
                            color: white;
                            border: none;
                            border-radius: 4px;
                            cursor: pointer;
                            font-size: 16px;
                            text-decoration: none;
                            display: inline-block;
                        }
                        .btn-primary:hover {
                            background: #0056b3;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h1>Welcome to ECOM 🛒</h1>
                        <p class="welcome-text">
                            Your one-stop shop for everything you need. 
                            Please login to access your account and start shopping.
                        </p>
                        <center>OAuth 2.0</center>
                        <a href="/oauth2/authorization/oauth2-code-flow" class="btn-primary">LogIn with Keycloak</a>
                    </div>
                </body>
                </html>
                """;
        }

        // User is logged in - show their data
        var attributes = authentication.getPrincipal().getAttributes();

        // Get CSRF token
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        String token = csrfToken != null ? csrfToken.getToken() : "";
        String tokenName = csrfToken != null ? csrfToken.getParameterName() : "_csrf";

        String html = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>ECOM - User Profile</title>
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, sans-serif;
                        max-width: 600px;
                        margin: 50px auto;
                        padding: 20px;
                        background: #f5f5f5;
                    }
                    .container {
                        background: white;
                        padding: 30px;
                        border-radius: 8px;
                        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                    }
                    h2 {
                        color: #333;
                        margin-top: 0;
                    }
                    .info-row {
                        display: flex;
                        padding: 12px 0;
                        border-bottom: 1px solid #eee;
                    }
                    .info-row:last-of-type {
                        border-bottom: none;
                    }
                    .label {
                        font-weight: 600;
                        color: #666;
                        width: 120px;
                    }
                    .value {
                        color: #333;
                    }
                    .logout-btn {
                        margin-top: 20px;
                        padding: 10px 20px;
                        background: #dc3545;
                        color: white;
                        border: none;
                        border-radius: 4px;
                        cursor: pointer;
                        font-size: 14px;
                    }
                    .logout-btn:hover {
                        background: #c82333;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <h2>Welcome, %s 👋</h2>
                    
                    <div class="info-row">
                        <span class="label">Username:</span>
                        <span class="value">%s</span>
                    </div>
                    <div class="info-row">
                        <span class="label">Full Name:</span>
                        <span class="value">%s</span>
                    </div>
                    <div class="info-row">
                        <span class="label">Email:</span>
                        <span class="value">%s</span>
                    </div>
                    <div class="info-row">
                        <span class="label">Provider:</span>
                        <span class="value">%s</span>
                    </div>

                    <form action="/logout" method="post">
                        <input type="hidden" name="%s" value="%s" />
                        <button type="submit" class="logout-btn">Logout</button>
                    </form>
                </div>
            </body>
            </html>
            """
                .formatted(
                        attributes.get("preferred_username"),
                        attributes.get("preferred_username"),
                        attributes.get("name"),
                        attributes.get("email"),
                        authentication.getAuthorizedClientRegistrationId(),
                        tokenName,
                        token
                );


        return html;
    }


    @PostMapping("/logout")
    public RedirectView logout(@AuthenticationPrincipal OidcUser oidcUser,
                               HttpServletRequest request,
                               HttpServletResponse response,
                               Authentication authentication) {

        // Get ID token before clearing session
        String idToken = oidcUser.getIdToken().getTokenValue();

        // Invalidate session
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // Clear security context
        SecurityContextHolder.clearContext();

        // Clear authentication
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }

        // Delete all cookies
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }

        // Build Keycloak logout URL
        String keycloakLogoutUrl =
                "http://127.0.0.1:8089/realms/oauth2_learn/protocol/openid-connect/logout"
                        + "?id_token_hint=" + idToken
                        + "&post_logout_redirect_uri=http://localhost:8080/";

        // Return RedirectView object
        return new RedirectView(keycloakLogoutUrl);
    }

}