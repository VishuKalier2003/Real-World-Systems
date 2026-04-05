package four.auth.config;

import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class RsaKeyProvider {
    
    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public RsaKeyProvider(
        @Value("${jwt.private.key}") Resource privateResource,
        @Value("${jwt.public.key}") Resource publicResource
    ) throws Exception {
        // Load private key
        String privateKeyContent = new String(Files.readAllBytes(privateResource.getFile().toPath()))
                .replaceAll("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] privateBytes = java.util.Base64.getDecoder().decode(privateKeyContent);
        PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(privateBytes);

        // Load public key
        String publicKeyContent = new String(Files.readAllBytes(publicResource.getFile().toPath()))
                .replaceAll("-----BEGIN PUBLIC KEY-----", "")
                .replaceAll("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] publicBytes = java.util.Base64.getDecoder().decode(publicKeyContent);
        X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(publicBytes);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        this.privateKey = keyFactory.generatePrivate(privateSpec);
        this.publicKey = keyFactory.generatePublic(publicSpec);
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
}
