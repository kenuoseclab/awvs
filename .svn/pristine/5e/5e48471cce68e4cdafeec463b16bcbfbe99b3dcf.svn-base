package msl.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncryptUtils {

	/**
	 * 传入文本内容，返回 SHA-256 串
	 * 
	 * @param strText
	 * @return
	 */
	public static String getSHA256(final String strText) {
		return SHA(strText, "SHA-256");
	}
	public static String getSHA256(final File file) {
		return SHA(file, "SHA-256");
	}

	/**
	 * 传入文本内容，返回 SHA-512 串
	 * 
	 * @param strText
	 * @return
	 */
	public static String getSHA512(final String strText) {
		return SHA(strText, "SHA-512");
	}

	/**
	 * 字符串 SHA 加密
	 * 
	 * @param strSourceText
	 * @return
	 */
	private static String SHA(final String strText, final String strType) {
		// 返回值
		String strResult = null;

		// 是否是有效字符串
		if (strText != null && strText.length() > 0) {
			try {
				// SHA 加密开始
				// 创建加密对象 并傳入加密類型
				MessageDigest messageDigest = MessageDigest.getInstance(strType);
				// 传入要加密的字符串
				messageDigest.update(strText.getBytes());
				// 得到 byte 類型结果
				byte byteBuffer[] = messageDigest.digest();

				// 將 byte 轉換爲 string
				StringBuffer strHexString = new StringBuffer();
				// 遍歷 byte buffer
				for (int i = 0; i < byteBuffer.length; i++) {
					String hex = Integer.toHexString(0xff & byteBuffer[i]);		// & 与运算 0xff == B11111111 ==d255
					if (hex.length() == 1) {									// 如果只有一个hexstring返回，那么添加0，保证都是2位
						strHexString.append('0');
					}
					strHexString.append(hex);
				}
				// 得到返回結果
				strResult = strHexString.toString();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		}

		return strResult;
	}

	/**
	 * 计算文件的sha256值
	 * @param file
	 * @param strType
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public static String SHA(final File file, final String strType){
		// 接收返回的结果
		StringBuffer strHexString = new StringBuffer();
		try {
			FileInputStream is = new FileInputStream(file);
			MessageDigest messageDigest = MessageDigest.getInstance(strType);
			
			// 传入数据，最大只能一次读取2009999999字节‬
			byte [] bys = new byte[2009999999];
			int len = 0;
			while((len = is.read(bys))!=-1) {
				messageDigest.update(bys, 0, len);
			}
			is.close();
			
			// 获取计算的结果
			byte[] byteBuffer = messageDigest.digest();
			
			// 处理结果
			for (int i = 0; i < byteBuffer.length; i++) {
				String hex = Integer.toHexString(0xff & byteBuffer[i]);		// & 与运算
				if (hex.length() == 1) {
					strHexString.append('0');
				}
				strHexString.append(hex);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return strHexString.toString();
	}
}