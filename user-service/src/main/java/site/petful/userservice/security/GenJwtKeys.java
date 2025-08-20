package site.petful.userservice.security;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Encoders;

public class GenJwtKeys {
    public static void main(String[] args) {
        var access  = Keys.secretKeyFor(SignatureAlgorithm.HS256); // ≥32B
        var refresh = Keys.secretKeyFor(SignatureAlgorithm.HS512); // ≥64B
        System.out.println("ACCESS_B64="  + Encoders.BASE64.encode(access.getEncoded()));
        System.out.println("REFRESH_B64=" + Encoders.BASE64.encode(refresh.getEncoded()));
    }
}
