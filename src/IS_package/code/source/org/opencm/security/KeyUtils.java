package org.opencm.security;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.opencm.util.Cache;

public class KeyUtils {

    public static String OPENCM_MASTER_PASSWORD_GLOBAL_VARIABLE 		= "OPENCM_MASTER_PASSWORD";

	private static String SECRET_SALT 			= "J6%jh0lasX84lK<*2f";
	private static int SECRET_ITERATION 		= 40000;
	private static int SECRET_KEY_LENGTH		= 128;
	
	public static String getMasterPassword() {
		return Cache.getInstance().get(OPENCM_MASTER_PASSWORD_GLOBAL_VARIABLE);
	}

	public static String encrypt(String stValue) {
		String masterPwd = getMasterPassword();
        String encryptedValue = null;
        try {
	        SecretKeySpec key = createSecretKey(masterPwd.toCharArray(), SECRET_SALT.getBytes(), SECRET_ITERATION, SECRET_KEY_LENGTH);
	        encryptedValue = encrypt(stValue, key);
        } catch (Exception ex) {
        	System.out.println("OpenCM :: KeyUtils.encrypt() :: Exception: " + ex.toString());
        }
        return encryptedValue;
	}
	
	public static String decrypt(String stValue) {
		String masterPwd = getMasterPassword();
        String decryptedValue = null;
        try {
	        SecretKeySpec key = createSecretKey(masterPwd.toCharArray(), SECRET_SALT.getBytes(), SECRET_ITERATION, SECRET_KEY_LENGTH);
	        decryptedValue = decrypt(stValue, key);
        } catch (Exception ex) {
        	System.out.println("OpenCM :: KeyUtils.decrypt() :: Exception: " + ex.toString());
        }
        return decryptedValue;
	}
	 
    private static SecretKeySpec createSecretKey(char[] password, byte[] salt, int iterationCount, int keyLength) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        PBEKeySpec keySpec = new PBEKeySpec(password, salt, iterationCount, keyLength);
        SecretKey keyTmp = keyFactory.generateSecret(keySpec);
        return new SecretKeySpec(keyTmp.getEncoded(), "AES");
    }
 
    private static String encrypt(String property, SecretKeySpec key) throws GeneralSecurityException, UnsupportedEncodingException {
        Cipher pbeCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        pbeCipher.init(Cipher.ENCRYPT_MODE, key);
        AlgorithmParameters parameters = pbeCipher.getParameters();
        IvParameterSpec ivParameterSpec = parameters.getParameterSpec(IvParameterSpec.class);
        byte[] cryptoText = pbeCipher.doFinal(property.getBytes("UTF-8"));
        byte[] iv = ivParameterSpec.getIV();
        return base64Encode(iv) + ":" + base64Encode(cryptoText);
    }
 
    private static String base64Encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }
 
    private static String decrypt(String string, SecretKeySpec key) throws GeneralSecurityException, IOException {
        String iv = string.split(":")[0];
        String property = string.split(":")[1];
        Cipher pbeCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        pbeCipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(base64Decode(iv)));
        return new String(pbeCipher.doFinal(base64Decode(property)), "UTF-8");
    }
	 
    private static byte[] base64Decode(String property) throws IOException {
        return Base64.getDecoder().decode(property);
    }
	
}
