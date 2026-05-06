package com.judy.secureprojecthub.controller.graphql;

import com.judy.secureprojecthub.dto.TokenResponseDto;
import com.judy.secureprojecthub.graphql.payload.TokenRequestInput;
import com.judy.secureprojecthub.service.JwtTokenService;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
public class AuthGraphQLController {


    // TODO: Replace Object jwtTokenGenerator with your actual JWT utility/service class.
    // For example: private final JwtTokenService jwtTokenService;
    private final  JwtTokenService  jwtTokenService;

    public AuthGraphQLController(JwtTokenService  jwtTokenService) {
   
        this.jwtTokenService = jwtTokenService;
    }

    @MutationMapping
    public TokenResponseDto generateToken(@Argument TokenRequestInput input) {
    	return jwtTokenService.generateToken(input.username(), input.password());
    }
}
