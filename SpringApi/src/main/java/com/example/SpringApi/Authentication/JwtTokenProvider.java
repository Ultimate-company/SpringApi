package com.example.SpringApi.Authentication;

import com.example.SpringApi.DatabaseModels.CentralDatabase.WebTemplateCarrierMapping;
import io.jsonwebtoken.Claims;
import org.example.CommonHelpers.PasswordHelper;
import com.example.SpringApi.DatabaseModels.CentralDatabase.User;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.Map;

import static io.jsonwebtoken.Jwts.*;

@Component
public class JwtTokenProvider {
    public String generateToken(WebTemplateCarrierMapping webTemplateCarrierMapping) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 24 * 60 * 60 * 1000);

        return builder()
                .issuer("https://localhost:4433/")
                .issuedAt(now)
                .audience().add("https://localhost:4433/").and()
                .claim("wildCard", webTemplateCarrierMapping.getWildCard())
                .claim("webTemplateId", webTemplateCarrierMapping.getWebTemplateId())
                .claim("carrierId",webTemplateCarrierMapping.getCarrierId())
                .expiration(expiryDate)
                .signWith(PasswordHelper.getSecretKey(webTemplateCarrierMapping.getApiAccessKey()))
                .compact();
    }

    public String generateToken(User user, Map<Long, Long> carrierPermissionMapping, String apiKey) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 24 * 60 * 60 * 1000);

        return builder()
                .issuer("https://localhost:4433/")
                .issuedAt(now)
                .audience().add("https://localhost:4433/").and()
                .claim("email", user.getLoginName())
                .claim("given_name", user.getFirstName())
                .claim("last_name", user.getLastName())
                .claim("role", user.getRole())
                .claim("permissions", carrierPermissionMapping)
                .expiration(expiryDate)
                .signWith(PasswordHelper.getSecretKey(apiKey))
                .compact();
    }

    public String getUserNameFromToken(String token, String apiKey){
        Claims claims = parser()
                .verifyWith(PasswordHelper.getSecretKey(apiKey))
                .build()
                .parseSignedClaims(token)
                .getBody();

        return claims.get("email").toString();
    }

    public Map<String, Integer> getcarrierPermissionMappingFromToken(String token, String apiKey){
        Claims claims = parser()
                .verifyWith(PasswordHelper.getSecretKey(apiKey))
                .build()
                .parseSignedClaims(token)
                .getBody();

        return (Map<String, Integer>) claims.get("permissions");
    }

    public boolean validateToken(String token, String userName, String apiKey) {
        try {
            parser().verifyWith(PasswordHelper.getSecretKey(apiKey)).build().parseSignedClaims(token);
            if(!userName.equals(getUserNameFromToken(token, apiKey))){
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    public boolean validateTokenForWebTemplate(String token, String wildCard, String apiAccessKey) {
        try {
            parser().verifyWith(PasswordHelper.getSecretKey(apiAccessKey)).build().parseSignedClaims(token);
            Claims claims = parser()
                    .verifyWith(PasswordHelper.getSecretKey(apiAccessKey))
                    .build()
                    .parseSignedClaims(token)
                    .getBody();
            if(!wildCard.equals(claims.get("wildCard").toString())){
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
        return true;
    }
}
