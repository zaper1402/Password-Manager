package com.example.passwordmanager;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Encryption {

    public SecretKey generateKey(String password) throws InvalidKeySpecException, NoSuchAlgorithmException {
        final int iterations = 10000;
        final int outputKeyLength = 128;
        final byte[] salt = {40, 12, 56, 71, 19, 37, 49, 87};

        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, iterations, outputKeyLength);
        SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
        return secretKey;
    }

    public String encryptData(String data, SecretKey secretKey) throws NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeyException, UnsupportedEncodingException, InvalidAlgorithmParameterException, InvalidKeyException, UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException {

        byte[] ivBytes = {76, 87, 32, 64, 89, 6, 89, 34, 69, 83, 2, 64, 98, 16, 4, 98};
        IvParameterSpec ivParameterSpec = new IvParameterSpec(ivBytes);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
        String encryptedData = Base64.encodeToString(cipher.doFinal(data.getBytes("UTF-8")), Base64.DEFAULT);
        return encryptedData;
    }

    public String decryptData(String data, SecretKey secretKey) throws NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeyException, UnsupportedEncodingException {

        byte[] ivBytes = {76, 87, 32, 64, 89, 6, 89, 34, 69, 83, 2, 64, 98, 16, 4, 98};
        IvParameterSpec ivParameterSpec = new IvParameterSpec(ivBytes);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
        byte[] cipherBytes = Base64.decode(data, Base64.DEFAULT);
        String decryptedData = new String(cipher.doFinal(cipherBytes));
        return decryptedData;
    }
}
