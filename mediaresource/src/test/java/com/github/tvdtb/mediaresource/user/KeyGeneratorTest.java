package com.github.tvdtb.mediaresource.user;

import java.security.SecureRandom;
import java.util.Base64;

import org.junit.Test;

public class KeyGeneratorTest {

	@Test
	public void generateRandomBytes() {
		SecureRandom random = new SecureRandom();
        random.nextBytes(new byte[64]);
        
        byte[] bytes = new byte[64];
        random.nextBytes(bytes);
        
		String base64 = Base64.getEncoder().encodeToString(bytes);
		System.out.println("Base64="+base64);
		
	}
}
