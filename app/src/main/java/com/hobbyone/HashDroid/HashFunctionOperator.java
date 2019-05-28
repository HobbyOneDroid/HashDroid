/* HashFunctionOperator.java -- 
   Copyright (C) 2010 Christophe Bouyer (Hobby One)

This file is part of Hash Droid.

Hash Droid is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Hash Droid is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Hash Droid. If not, see <http://www.gnu.org/licenses/>.
 */

package com.hobbyone.HashDroid;

import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.Adler32;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class HashFunctionOperator {
	private String _sAlgo = "md5";

	public void SetAlgorithm(String sAlgo) {
		_sAlgo = sAlgo;
	}

	private String PrependValue(String iStr, int NbDigits) {
		String sReturnedStr = iStr;
		while (sReturnedStr.length() < NbDigits)
			sReturnedStr = "0" + sReturnedStr;
		return sReturnedStr;
	}

	private String CreateHashString(byte messageDigest[]) {
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < messageDigest.length; i++) {
			String h = Integer.toHexString(0xFF & messageDigest[i]);
			String StrComplete = PrependValue(h, 2);
			hexString.append(StrComplete);
		}
		return hexString.toString();
	}

	public String StringToHash(String s) {
		String sReturnedStr = "";
		if (_sAlgo.equals("CRC-32") || _sAlgo.equals("Adler-32")) {
			long value = 0;
			Checksum checksumv = null;
			if (_sAlgo.equals("CRC-32")) {
				CRC32 crc32v = new CRC32();
				crc32v.update(s.getBytes());
				checksumv = crc32v;
			} else if (_sAlgo.equals("Adler-32")) {
				Adler32 adler32v = new Adler32();
				adler32v.update(s.getBytes());
				checksumv = adler32v;
			}
			if (checksumv != null) {
				value = checksumv.getValue();
				String StrHex = Long.toHexString(value);
				sReturnedStr = PrependValue(StrHex, 8); // 32 bits (8 digits in
														// hexadecimal)
			}
		} else {
			IMessageDigest MessageDig = HashFactory.getInstance(_sAlgo);
			if (MessageDig != null) {
				byte[] in = s.getBytes();
				MessageDig.update(in, 0, in.length);
				byte messageDigest[] = MessageDig.digest();
				sReturnedStr = CreateHashString(messageDigest);
			}
		}

		return sReturnedStr;
	}

	public String FileToHash(InputStream inputstream) {
		String sReturnedStr = "";
		if(null != inputstream) {
			if (_sAlgo.equals("CRC-32") || _sAlgo.equals("Adler-32")) {
				try {
					byte[] databytes = new byte[1024];
					int nread = 0;
					long value = 0;
					Checksum checksumv = null;
					if (_sAlgo.equals("CRC-32")) {
						CRC32 crc32v = new CRC32();
						checksumv = crc32v;
					} else if (_sAlgo.equals("Adler-32")) {
						Adler32 adler32v = new Adler32();
						checksumv = adler32v;
					}
					if (checksumv != null) {
						while ((nread = inputstream.read(databytes)) > 0) {
							checksumv.update(databytes, 0, nread);
						}
						value = checksumv.getValue();
						String StrHex = Long.toHexString(value);
						sReturnedStr = PrependValue(StrHex, 8); // 32 bits (8 digits
						// in hexadecimal)
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				try {
					IMessageDigest MessageDig = HashFactory.getInstance(_sAlgo);
					if (MessageDig != null) {
						byte[] databytes = new byte[1024];
						int nread = 0;
						while ((nread = inputstream.read(databytes)) != -1) {
							MessageDig.update(databytes, 0, nread);
						}
						byte messageDigest[] = MessageDig.digest();
						sReturnedStr = CreateHashString(messageDigest);
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return sReturnedStr;
	}
}
