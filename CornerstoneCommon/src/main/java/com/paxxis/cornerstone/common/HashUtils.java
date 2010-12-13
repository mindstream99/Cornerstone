/*
 * Copyright 2010 Paxxis Technology LLC
 *
 */

package com.paxxis.cornerstone.common;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author rob
 *
 */
public class HashUtils {
	private static final byte[] HEX_CHAR_TABLE = {
	    (byte)'0', (byte)'1', (byte)'2', (byte)'3',
	    (byte)'4', (byte)'5', (byte)'6', (byte)'7',
	    (byte)'8', (byte)'9', (byte)'a', (byte)'b',
	    (byte)'c', (byte)'d', (byte)'e', (byte)'f'
	};    

    protected HashUtils()
    {}

    /**
     * Generates an encoded hex string from an input and an input salt.
     * 
     */
    public static String hashSaltInput(String input, String saltInput) {
    	byte[] inputBytes = input.getBytes();
    	byte[] saltBytes = saltInput.getBytes();

    	String result = "";
        byte[] saltedInput = new byte[saltBytes.length + inputBytes.length];
        try {
            System.arraycopy(saltBytes, 0, saltedInput, saltBytes.length, 0);
            System.arraycopy(inputBytes, 0, saltedInput, saltBytes.length, inputBytes.length);
            MessageDigest m = MessageDigest.getInstance("MD5");
            byte[] hashedAndSaltedInput = m.digest(saltedInput);
            result = getHexString(hashedAndSaltedInput);
        } catch (NoSuchAlgorithmException ex) {
        	throw new RuntimeException(ex);
        } catch (UnsupportedEncodingException ex) {
        	throw new RuntimeException(ex);
		}
        
        return result;
    }
	
	private static String getHexString(byte[] raw) throws UnsupportedEncodingException {
	    byte[] hex = new byte[2 * raw.length];
	    int index = 0;
	
	    for (byte b : raw) {
	        int v = b & 0xFF;
	        hex[index++] = HEX_CHAR_TABLE[v >>> 4];
	        hex[index++] = HEX_CHAR_TABLE[v & 0xF];
	    }
	    
	    return new String(hex, "ASCII");
	}

}
