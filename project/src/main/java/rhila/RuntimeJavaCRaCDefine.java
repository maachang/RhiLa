package rhila;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 既存JavaランタイムのCRaC定義.
 */
@SuppressWarnings("unused")
public class RuntimeJavaCRaCDefine {
	protected RuntimeJavaCRaCDefine() {}
	
	// このオブジェクト自体の定義..
	public static final RuntimeJavaCRaCDefine LOAD_CRAC = new RuntimeJavaCRaCDefine();
	
	static {
		
		// GZIP圧縮.
		byte[] b = null;
		try {
			// 解凍用のバイナリを作成.
			ByteArrayOutputStream o;
			GZIPOutputStream gzipOutput =
				new GZIPOutputStream(
					o = new ByteArrayOutputStream(1));
			gzipOutput.write(0);
			gzipOutput.flush();
			gzipOutput.finish();
			gzipOutput.close();
			gzipOutput = null;
			b = o.toByteArray();
			o.close();
		} catch(Exception e ) {
		}
		
		// GZIP解凍.
		try {
			// 圧縮結果の解凍バイナリで処理.
			GZIPInputStream gzipInput = new GZIPInputStream(
				new ByteArrayInputStream(b));
			gzipInput.close();
		} catch(Exception e ) {
		}
		
		// map.
		Map<?,?> map = new HashMap<Object, Object>();
		map.keySet().iterator();
		map.entrySet().iterator();
		map = null;
		
		// list.
		List<?> list = new ArrayList<Object>();
		list = null;
		
		// date.
		Date date = new Date();
		date = null;
		
		// StringBuilder.
		StringBuilder builder = new StringBuilder();
		builder = null;
		
		
		
	};


}
