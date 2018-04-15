package com.webauthn4j.test.token;

import com.webauthn4j.test.CoreTestData;
import com.webauthn4j.util.KeyPairUtil;
import com.webauthn4j.util.MACUtil;
import com.webauthn4j.util.SignatureUtil;
import com.webauthn4j.util.UnsignedNumberUtil;

import java.nio.ByteBuffer;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Arrays;

public class FIDOU2FSoftwareToken {

    private PrivateKey attestationPrivateKey;
    private X509Certificate attestationPublicKeyCertificate;

    private long counter;
    private boolean countUpEnabled;

    public FIDOU2FSoftwareToken(PrivateKey attestationPrivateKey, X509Certificate attestationPublicKeyCertificate, int counter){
        this.attestationPrivateKey = attestationPrivateKey;
        this.attestationPublicKeyCertificate = attestationPublicKeyCertificate;
        this.counter = counter;
    }

    public FIDOU2FSoftwareToken(){
        this(CoreTestData.AUTHENTICATOR_PRIVATE_KEY, CoreTestData.AUTHENTICATOR_ATTESTATION_CERTIFICATE, 0);
    }

    public RegisterResponse register(RegisterRequest registerRequest){

        byte[] challengeParameter = registerRequest.getChallengeParameter();
        byte[] applicationParameter = registerRequest.getApplicationParameter();

        SecureRandom secureRandom = new SecureRandom();
        byte[] nonce = new byte[32];
        secureRandom.nextBytes(nonce);
        KeyPair keyPair = getKeyPair(applicationParameter, nonce);

        byte[] rpPrivateKey = keyPair.getPrivate().getEncoded();

        byte[] message = ByteBuffer.allocate(32 + rpPrivateKey.length)
                .put(applicationParameter).put(rpPrivateKey).array();
        byte[] mac = MACUtil.caclucalteHMAC(message, attestationPrivateKey.getEncoded());
        byte[] keyHandle = ByteBuffer.allocate(64).put(nonce).put(mac).array();

        byte[] userPublicKey = keyPair.getPublic().getEncoded();

        byte rfu = 0x00;

        byte[] signedData = ByteBuffer.allocate(1 + 32 + 32 + keyHandle.length + 65).put(rfu).put(applicationParameter).put(challengeParameter).put(keyHandle).put(userPublicKey).array();

        byte[] signature = calculateSignature(attestationPrivateKey, signedData);

        return new RegisterResponse(userPublicKey, keyHandle, attestationPublicKeyCertificate, signature);
    }

    public AuthenticateResponse authenticate(AuthenticateRequest authenticateRequest){

        byte control = authenticateRequest.getControl();
        byte[] applicationParameter = authenticateRequest.getApplicationParameter();
        byte[] challenge = authenticateRequest.getChallenge();
        byte[] keyHandle = authenticateRequest.getKeyHandle();

        byte[] nonce = Arrays.copyOf(keyHandle, 32);
        KeyPair keyPair = getKeyPair(applicationParameter, nonce);
        countUp();
        byte userPresence = 0x01; //present
        byte[] signedData = ByteBuffer.allocate(32 + 1 + 4 + 32).put(applicationParameter).put(userPresence).put(getCounterBytes()).put(challenge).array();
        byte[] signature = calculateSignature(keyPair.getPrivate(), signedData);
        return new AuthenticateResponse(userPresence, getCounterBytes(), signature);
    }

    private KeyPair getKeyPair(byte[] applicationParameter, byte[] nonce) {
        byte[] seed = ByteBuffer.allocate(64).put(applicationParameter).put(nonce).array();
        return KeyPairUtil.createKeyPair(seed);
    }

    private byte[] calculateSignature(PrivateKey privateKey, byte[] signedData){
        Signature signature = SignatureUtil.createSignature("SHA256withECDSA");

        try {
            signature.initSign(privateKey);
            signature.update(signedData);
            return signature.sign();
        } catch (InvalidKeyException | SignatureException e) {
            throw new FIDOU2FException("Signature calculation error", e);
        }
    }

    private void countUp(){
        if(isCountUpEnabled()){
            counter++;
        }
    }

    private byte[] getCounterBytes(){
        return UnsignedNumberUtil.toBytes(counter);
    }


    public boolean isCountUpEnabled() {
        return countUpEnabled;
    }

    public void setCountUpEnabled(boolean countUpEnabled) {
        this.countUpEnabled = countUpEnabled;
    }
}