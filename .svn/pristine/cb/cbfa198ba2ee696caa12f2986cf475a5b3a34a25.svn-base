package msl.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class FileUtils {

	/**
	 * 按行获取数据
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static Set<String> getListByLine(File file) throws IOException {
		Set<String> set  = new HashSet<String>();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		String line = null;
		while((line = br.readLine())!=null) {
			set.add(line);
		}
		br.close();
		return set;
	}
}
