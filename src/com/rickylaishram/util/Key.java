package com.rickylaishram.util;

import android.util.Base64;

public class Key {
	private String APP_LICENSE_BASE_64_KEY_ENCODED	= "";
	
	public Key(){}
	
	public String getKey() {
		byte[] temp1 	= Base64.decode(APP_LICENSE_BASE_64_KEY_ENCODED, Base64.DEFAULT);
		String temp2	= new String(temp1);
		String temp3	= "";
		
		for (int i = 0; i < temp2.length(); i++) {
            char c = temp2.charAt(i);
            if       (c >= 'a' && c <= 'm') c += 13;
            else if  (c >= 'A' && c <= 'M') c += 13;
            else if  (c >= 'n' && c <= 'z') c -= 13;
            else if  (c >= 'N' && c <= 'Z') c -= 13;
            temp3	= temp3 + c;
        }
		
		return temp3;
	}
}
