package fr.exentop.exserver;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class ExHandshakeEncoder {

	private static final String base64code = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

	private static final int splitLinesAt = 76;

	public static byte[] zeroPad(int length, byte[] bytes) {
		byte[] padded = new byte[length]; // initialized to zero by JVM
		System.arraycopy(bytes, 0, padded, 0, bytes.length);
		return padded;
	}

	public static String encryptHandshake(Map<String, String> mDatas) {
		int version = Integer.parseInt(mDatas.get("sec-websocket-version"));
		String retour = "";

		if (version == 13) retour = sha1Encode(mDatas.get("sec-websocket-key"));

		return retour;
	}

	public static String sha1Encode(String str) {
		String sha1 = "";
		str += "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
		try {
			MessageDigest crypt = MessageDigest.getInstance("SHA-1");
			crypt.reset();
			crypt.update(str.getBytes("UTF-8"));
			sha1 = encode(crypt.digest());
		}
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return sha1;
	}

	public static String encode(byte[] stringArray) {

		String encoded = "";
		// determine how many padding bytes to add to the output
		int paddingCount = (3 - (stringArray.length % 3)) % 3;
		// add any necessary padding to the input
		stringArray = zeroPad(stringArray.length + paddingCount, stringArray);
		// process 3 bytes at a time, churning out 4 output bytes
		// worry about CRLF insertions later
		for (int i = 0; i < stringArray.length; i += 3) {
			int j = ((stringArray[i] & 0xff) << 16) + ((stringArray[i + 1] & 0xff) << 8) + (stringArray[i + 2] & 0xff);
			encoded = encoded + base64code.charAt((j >> 18) & 0x3f) + base64code.charAt((j >> 12) & 0x3f)
					+ base64code.charAt((j >> 6) & 0x3f) + base64code.charAt(j & 0x3f);
		}
		// replace encoded padding nulls with "="
		return splitLines(encoded.substring(0, encoded.length() - paddingCount) + "==".substring(0, paddingCount));

	}

	public static String splitLines(String string) {

		String lines = "";
		for (int i = 0; i < string.length(); i += splitLinesAt) {

			lines += string.substring(i, Math.min(string.length(), i + splitLinesAt));

		}
		return lines;

	}

}