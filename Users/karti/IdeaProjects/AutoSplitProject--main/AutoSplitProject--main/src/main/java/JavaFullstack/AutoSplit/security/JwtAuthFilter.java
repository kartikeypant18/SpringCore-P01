package JavaFullstack.AutoSplit.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    public JwtAuthFilter(JwtUtils jwtUtils, UserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        System.out.println("\n================ JWT FILTER START ================");
        System.out.println("Request URI: " + request.getRequestURI());

        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            System.out.println("JWT Filter: Raw Token = " + token);

            try {
                username = jwtUtils.getUsernameFromToken(token);
                System.out.println("JWT Filter: Username from Token = " + username);
            } catch (Exception e) {
                System.out.println("JWT Filter: ❌ Token parsing error: " + e.getMessage());
            }
        } else {
            System.out.println("JWT Filter: ❌ No Bearer token found in request");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                System.out.println("JWT Filter: UserDetails loaded for " + username);

                if (jwtUtils.validateToken(token)) {
                    System.out.println("JWT Filter: ✅ Token valid");
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    System.out.println("JWT Filter: ❌ Token invalid");
                }
            } catch (Exception e) {
                System.out.println("JWT Filter: ❌ Error loading user - " + e.getMessage());
            }
        }

        System.out.println("================ JWT FILTER END ================\n");

        filterChain.doFilter(request, response);
    }
}
