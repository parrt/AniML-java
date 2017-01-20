/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;

public class AniIO {
	public static void save(String fileName, String content) {
		save(fileName, content, null);
	}

	public static void save(String fileName, String content, String encoding) {
		OutputStreamWriter osw = null;
		try {
			File f = new File(fileName);
			FileOutputStream fos = new FileOutputStream(f);
			if (encoding != null) {
				osw = new OutputStreamWriter(fos, encoding);
			}
			else {
				osw = new OutputStreamWriter(fos);
			}
			osw.write(content);
		}
		catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
		finally {
			if ( osw!=null ) {
				try {
					osw.close();
				}
				catch (IOException ioe) {
					throw new RuntimeException(ioe);
				}
			}
		}
	}

	public static char[] read(String fileName) {
		return read(fileName, null);
	}

	public static char[] read(String fileName, String encoding) {
		InputStreamReader isr = null;
		char[] data = null;
		try {
			File f = new File(fileName);
			int size = (int)f.length();
			FileInputStream fis = new FileInputStream(fileName);
			if ( encoding!=null ) {
				isr = new InputStreamReader(fis, encoding);
			}
			else {
				isr = new InputStreamReader(fis);
			}
			data = new char[size];
			int n = isr.read(data);
			if (n < data.length) {
				data = Arrays.copyOf(data, n);
			}
		}
		catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
		finally {
			if ( isr!=null ) {
				try {
					isr.close();
				}
				catch (IOException ioe) {
					throw new RuntimeException(ioe);
				}
			}
		}
		return data;
	}
}
