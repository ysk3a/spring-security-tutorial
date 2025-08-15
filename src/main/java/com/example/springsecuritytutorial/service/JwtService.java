package com.example.springsecuritytutorial.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtService {

    public static final String DUMMY_SECRET = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        // stackoverflow.com/questions/78805779/issue-with-parserbuilder-method-in-jjwt-library-for-jwt-token-validation
        SecretKey key = (SecretKey) getSignKey();
        return Jwts
                .parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }


    public String generateToken(String userName){
        Map<String,Object> claims=new HashMap<>();
        return createToken(claims,userName);
    }

    private String createToken(Map<String, Object> claims, String userName) {
        // stackoverflow.com/questions/78150968/setclaimsjava-util-mapjava-lang-string-is-deprecated
        return Jwts.builder()
                .claims(claims)
                .subject(userName)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis()+1000*60*2))
                .signWith(getSignKey()).compact();
        // signWith method Signs the constructed JWT with the specified key using the key's recommended signature algorithm as defined below (in the doc), producing a JWS.
    }

    private Key getSignKey() {
        byte[] keyBytes= Decoders.BASE64.decode(DUMMY_SECRET);
        System.out.println(Keys.hmacShaKeyFor((keyBytes)).getAlgorithm().equals("HmacSHA256"));
        System.out.println(Keys.hmacShaKeyFor((keyBytes)).getAlgorithm().equals("HmacSHA384"));
        System.out.println(Keys.hmacShaKeyFor((keyBytes)).getAlgorithm().equals("HmacSHA512"));
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private static Key getKeyFromKeyGenerator(int keySize) throws NoSuchAlgorithmException {
        // baeldung.com/java-secure-aes-key
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(keySize);
        return keyGenerator.generateKey();
    }

    private static Key getPasswordBasedKey(int keySize, char[] password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // baeldung.com/java-secure-aes-key
        byte[] salt = new byte[100];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        PBEKeySpec pbeKeySpec = new PBEKeySpec(password, salt, 1000, keySize);
        SecretKey pbeKey = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(pbeKeySpec);
        return new SecretKeySpec(pbeKey.getEncoded(), "AES");
    }
}
