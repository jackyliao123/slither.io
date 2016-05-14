package tk.jackyliao123.base64;

public class Base64 {

	public static int[] charToVal = new int[128];
	public static char[] valToChar = new char[65];

	static {
		for(int i = 0; i < 26; ++i) {
			charToVal['A' + i] = i;
			charToVal['a' + i] = i + 26;
			valToChar[i] = (char)('A' + i);
			valToChar[i + 26] = (char)('a' + i);
		}
		for(int i = 0; i < 10; ++i) {
			charToVal['0' + i] = i + 52;
			valToChar[i + 52] = (char)('0' + i);
		}
		charToVal['+'] = 62;
		charToVal['/'] = 63;
		valToChar[62] = '+';
		valToChar[63] = '/';
		charToVal['='] = -1;
		valToChar[64] = '=';
	}

	public static String encode(byte[] array) {
		StringBuilder builder = new StringBuilder((array.length * 4 + 2) / 3);

		for(int i = 0; i < array.length; i += 3) {

			int c1 = 0;
			int c2 = 0;
			int c3 = 64;
			int c4 = 64;

			c1 |= (array[i] & 0xFC) >>> 2;
			c2 |= (array[i] & 0x3) << 4;

			if(i + 1 < array.length) {
				c2 |= (array[i + 1] & 0xF0) >>> 4;
				c3 = (array[i + 1] & 0x0F) << 2;
			}
			if(i + 2 < array.length) {
				c3 |= (array[i + 2] & 0xC0) >>> 6;
				c4 = (array[i + 2] & 0x3F);
			}

			builder.append(valToChar[c1]);
			builder.append(valToChar[c2]);
			builder.append(valToChar[c3]);
			builder.append(valToChar[c4]);

		}

		return builder.toString();

	}


	public static byte[] decode(String encoded) {
		int encodedLength = encoded.length();

		int paddingCount = 0;

		for(int i = encodedLength - 1; i >= 0; --i) {
			if(encoded.charAt(i) == '=') {
				++paddingCount;
			} else {
				break;
			}
		}

		if(paddingCount > 2) {
			throw new RuntimeException("Too much padding");
		}
		if(encodedLength % 4 != 0) {
			throw new RuntimeException("Invalid length: " + encodedLength);
		}

		byte[] output = new byte[encodedLength * 3 / 4 - paddingCount];

		for(int i = 0; i < encodedLength; i += 4) {
			int c1 = charToVal[encoded.charAt(i)];
			int c2 = charToVal[encoded.charAt(i + 1)];
			int c3 = charToVal[encoded.charAt(i + 2)];
			int c4 = charToVal[encoded.charAt(i + 3)];

			int ind = i * 3 / 4;

			output[ind] = (byte)((c1 << 2) | (c2 >>> 4));
			if(c3 != -1)
				output[ind + 1] = (byte)((c2 << 4) | (c3 >>> 2));
			if(c4 != -1)
				output[ind + 2] = (byte)((c3 << 6) | c4);
		}

		return output;
	}

}
