/*
 * Copyright 2013 the original author or authors.
 * Copyright 2013 Paxxis Technology LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paxxis.cornerstone.encryption;

import java.io.FileReader;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

import org.apache.commons.codec.binary.Base64;

import com.paxxis.cornerstone.common.EncryptionHandler;

public class TripleDESEncryptionHandler implements EncryptionHandler {
    //private static final String DEFAULT_KEY = "123456789012345678901234";
    private static final String UNICODE_FORMAT = "UTF8";
    private static final String DESEDE_ENCRYPTION_SCHEME = "DESede";
    private Cipher cipher = null;
    private SecretKey key = null;
    private String encryptionKey = null;
    private String encryptionKeyFile = null;
    
    public TripleDESEncryptionHandler() {
    }

    public void setEncryptionKey(String key) {
        this.encryptionKey = key;
    }
    
    public void setEncryptionKeyFile(String fileName) {
        this.encryptionKeyFile = fileName;
    }

    public void initialize() {
        if (encryptionKeyFile != null) {
            try {
                FileReader fr = new FileReader(encryptionKeyFile);
                int cnt;
                char[] cbuf = new char[256];
                StringBuilder builder = new StringBuilder();
                while (-1 != (cnt = fr.read(cbuf, 0, 256))) {
                    String s = new String(cbuf, 0, cnt);
                    builder.append(s);
                }
                
                encryptionKey = builder.toString();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } 
        
        if (encryptionKey == null) {
            throw new RuntimeException("No Encryption Key");
        }
        
        try {
            byte[] arrayBytes = encryptionKey.getBytes(UNICODE_FORMAT);
            KeySpec ks = new DESedeKeySpec(arrayBytes);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(DESEDE_ENCRYPTION_SCHEME);
            cipher = Cipher.getInstance(DESEDE_ENCRYPTION_SCHEME);
            key = skf.generateSecret(ks);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public String encrypt(String unencryptedString) {
        String encryptedString = null;
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] plainText = unencryptedString.getBytes(UNICODE_FORMAT);
            byte[] encryptedText = cipher.doFinal(plainText);
            encryptedString = new String(Base64.encodeBase64(encryptedText));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptedString;
    }


    public String decrypt(String encryptedString) {
        String decryptedText=null;
        try {
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] encryptedText = Base64.decodeBase64(encryptedString);
            byte[] plainText = cipher.doFinal(encryptedText);
            decryptedText= new String(plainText);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decryptedText;
    }

    @Override
    @Deprecated
    public String encryptPassword(String password) {
        return decrypt(password);
    }
}
