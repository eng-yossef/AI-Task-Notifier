package main.webapp.controller;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.crypto.SecretKey;
import java.io.IOException;

@WebFilter(urlPatterns = "/tasks/*") // Apply filter to all task-related endpoints
public class NewJWTValidationFilter implements Filter {


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Optional initialization logic
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write("{\"message\": \"Missing or invalid Authorization header.\"}");
            return;
        }

        String token = authHeader.substring(7); // Remove "Bearer " prefix

        try {
            // Secret key for decoding the token
            SecretKey key = Secret.getKey();
//            System.out.println("from filter" +key);

            // Parse the JWT token
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Extract the user ID from the token
            Integer idFromToken = claims.get("id", Integer.class);

            // Get the user ID from the request (assumed as a path or query parameter)
            String requestedIdStr = httpRequest.getParameter("userId");
            if (requestedIdStr == null) {
                // Assuming the ID could be part of the URL path as /tasks/{id}
                requestedIdStr = httpRequest.getRequestURI().split("/")[httpRequest.getRequestURI().split("/").length - 1];
            }

            if (requestedIdStr == null) {
                httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                httpResponse.getWriter().write("{\"message\": \"Missing user ID in request.\"}");
                return;
            }

            Integer requestedId = Integer.parseInt(requestedIdStr);

            // Validate that the ID in the token matches the requested ID
            if (!idFromToken.equals(requestedId)) {
                httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                httpResponse.getWriter().write("{\"message\": \"Access denied: You can only access your own tasks.\"}");
                return;
            }

            // Set the user ID from the token as a request attribute
            request.setAttribute("id", idFromToken);

            // Proceed with the request
            chain.doFilter(request, response);
        } catch (Exception e) {
            // Handle token errors (expired, invalid, etc.)
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write("{\"message\": \"Invalid token.\"}");
        }
    }

    @Override
    public void destroy() {
        // Optional cleanup logic
    }
}
