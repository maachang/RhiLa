package rhila.scriptable;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhila.RhilaException;
import rhila.lib.ArrayMap;
import rhila.lib.ByteArrayBuffer;
import rhila.lib.NumberUtil;

/**
 * Binary用Scriptable.
 */
public class BinaryScriptable implements RhinoScriptable<byte[]> {
    // lambda snapStart CRaC用.
    protected static final BinaryScriptable LOAD_CRAC = new BinaryScriptable();
	
	// instance可能なScriptable.
	private static final ArrayMap<String, Scriptable> instanceList;
	
	// 格納バイナリ.
	private byte[] binary;
	
	// メソッド名群(sort済み).
	private static final String[] FUNCTION_NAMES = new String[] {
		"copy"
		,"isGzip"
		,"toBase64"
		,"toGzip"
		,"toString"
		,"toUnGzip"
	};
	
	static {
		// 配列で直接追加.
		final int len = FUNCTION_NAMES.length * 2;
		Object[] list = new Object[len];
		for(int i = 0, j = 0; i < len; i += 2, j ++) {
			list[i] = FUNCTION_NAMES[j];
			list[i + 1] = new FunctionList(j);
		}
		instanceList = new ArrayMap<String, Scriptable>(list);
	}
	
	// objectインスタンスリスト.
	private final ArrayMap<String, Object> objInsList =
		new ArrayMap<String, Object>();
	
	protected BinaryScriptable() {}
	
	// コンストラクタ.
	public BinaryScriptable(byte[] binary) {
		if(binary == null) {
			throw new RhilaException("Argument not valid");
		}
		this.binary = binary;
	}
	
	// コンストラクタ.
	public BinaryScriptable(BinaryScriptable b) {
		if(b == null) {
			throw new RhilaException("Argument not valid");
		}
		byte[] s = b.getRaw();
		int len = s.length;
		byte[] n = new byte[len];
		System.arraycopy(s, 0, n, 0, len);
		this.binary = n;
	}
	
	// コンストラクタ.
	public BinaryScriptable(int len) {
		if(len < 0) {
			throw new RhilaException("Argument not valid");
		}
		this.binary = new byte[len];
	}
	
	// コンストラクタ.
	public BinaryScriptable(String value) {
		this(value, null);
	}
	
	// コンストラクタ.
	public BinaryScriptable(String value, String charset) {
		if(value == null) {
			throw new RhilaException("Argument not valid");
		}
		try {
			if(charset == null) {
				charset = "UTF8";
			}
			this.binary = value.getBytes(charset);
		} catch(Exception e) {
			throw new RhilaException(e);
		}
	}
	
	// 元情報を取得.
	public byte[] getRaw() {
		return binary;
	}
	
	// バイナリを取得.
	public byte[] toByteArray() {
		return binary;
	}
	
	// バイナリサイズを取得.
	public int size() {
		return binary.length;
	}
	
	@Override
	public Object get(int arg0, Scriptable arg1) {
		if(arg0 < 0 || arg0 >= binary.length) {
			return Undefined.instance;
		}
		return (binary[arg0] & 0x0ff);
	}

	@Override
	public boolean has(int arg0, Scriptable arg1) {
		return !(arg0 < 0 || arg0 >= binary.length);
	}

	@Override
	public void put(int arg0, Scriptable arg1, Object arg2) {
		if(arg0 < 0 || arg0 >= binary.length ||
			arg2 == null || !(arg2 instanceof Number)) {
			return;
		}
		binary[arg0] = (byte)(((Number)arg2).intValue() & 0x0ff);
	}
	
	// base64変換.
	public Object toBase64() {
		return rhila.lib.Base64.encode(this.getRaw());
	}
	
	// 文字変換.
	public String convertString() {
		return convertString("UTF8");
	}
	
	// charset指定の文字変換.
	public String convertString(String charset) {
		try {
			return new String(binary, charset);
		} catch(Exception e) {
			throw new RhilaException(e);
		}
	}
	
	// バイナリがGzipのオブジェクトか取得.
	public boolean isGzip() {
		try {
			toUnGzip();
			return true;
		} catch(Exception e) {
			return false;
		}
	}
	
	// gzip圧縮して、このオブジェクトに反映する.
	public byte[] toGzip() {
		// 管理されてる内容をGZIP圧縮.
		try {
			ByteArrayBuffer buf = new ByteArrayBuffer(4096);
			GZIPOutputStream gzip = new GZIPOutputStream(buf, 4096);
			gzip.write(binary);
			gzip.flush();
			gzip.finish();
			binary = buf.toByteArray();
			gzip.close();
		} catch(Exception e) {
			throw new RhilaException(e);
		}
		return binary;
	}
	
	// gzipを解凍して、このオブジェクトに反映する.
	public byte[] toUnGzip() {
		return toUnGzip(true);
	}
	
	// gzipを解凍して、このオブジェクトに反映する.
	public byte[] toUnGzip(boolean update) {
		// 管理されてる内容をGZIP圧縮.
		try {
			ByteArrayBuffer buf = new ByteArrayBuffer(4096);
			GZIPInputStream gzip = new GZIPInputStream(
				new ByteArrayInputStream(binary), 4096);
			int len;
			byte[] b = new byte[4096];
			while((len = gzip.read(b)) != -1) {
				buf.write(b, 0, len);
			}
			gzip.close();
			// 対象バイナリの更新を行う場合は更新.
			if(update) {
				binary = buf.toByteArray();
			}
			buf.close();
		} catch(Exception e) {
			throw new RhilaException(e);
		}
		return binary;
	}
	
	// 文字列変換処理.
	private Object toString(Object[] args) {
		if(args == null || args.length == 0) {
			return this.convertString();
		}
		return this.convertString(String.valueOf(args[1]));
	}
	
	// copy.
	private final Object copy(Object[] args) {
		// 引数(destBin, srcPos, destPos, len).
		// 戻り値: 実際にコピーされた長さ.
		if(args.length < 1 || !(args[0] instanceof BinaryScriptable)) {
			return -1;
		}
		int len = -1;
		try {
			byte[] srcBin = this.getRaw();
			byte[] bin = ((BinaryScriptable)args[0]).getRaw();
			len = bin.length;
			int srcPos = 0, destPos = 0;
			// 存在するパラメータで処理する.
			srcPos = (args.length >= 2 && args[1] instanceof Number) ?
					 ((Number)args[1]).intValue() : srcPos;
			destPos = (args.length >= 3 && args[2] instanceof Number) ?
					 ((Number)args[2]).intValue() : destPos;
			len = (args.length >= 4 && args[3] instanceof Number) ?
					 ((Number)args[3]).intValue() : len;
			// コピー先の条件がコピー先のバイナリ容量を超えてる場合.
			if(destPos + len > bin.length) {
				len = bin.length - destPos;
				// コピー対象が存在しない.
				if(len <= 0) {
					return -1;
				}
			}
			// コピー元の条件がコピー元のバイナリ容量を超えてる場合.
			if(srcPos + len > srcBin.length) {
				len = srcBin.length - srcPos;
				// コピー対象が存在しない.
				if(len <= 0) {
					return -1;
				}
			}
			System.arraycopy(srcBin, srcPos, bin, destPos, len);
		} catch(Exception e) {
			return -1;
		}
		return len;
	}
	
	@Override
	public Object get(String arg0, Scriptable arg1) {
		return getFunction(arg0); 
	}
	
	// function取得.
	private final Object getFunction(String name) {
		if("length".equals(name)) {
			return size();
		}
		// オブジェクト管理の生成Functionを取得.
		Object ret = objInsList.get(name);
		// 存在しない場合.
		if(ret == null) {
			// static管理のオブジェクトを取得.
			ret = instanceList.get(name);
			// 存在する場合.
			if(ret != null) {
				// オブジェクト管理の生成Functionとして管理.
				ret = ((AbstractRhinoFunctionInstance)ret)
					.getInstance(this);
				objInsList.put(name, ret);
			}
		}
		return ret;
	}
	
	// [js]HttpStatusFunctions.
	private static final class FunctionList
		extends AbstractRhinoFunctionInstance {
	    // lambda snapStart CRaC用.
		@SuppressWarnings("unused")
		protected static final FunctionList LOAD_CRAC =
			new FunctionList();
		
		private int type;
		private String typeString;
		private BinaryScriptable object;
		
		// コンストラクタ.
		private FunctionList() {}
		
		// コンストラクタ.
		private FunctionList(int type) {
			this.type = type;
			this.typeString = FUNCTION_NAMES[type];
		}
		
		// 新しいインスタンスを生成.
		public final Scriptable getInstance(Object... args) {
			FunctionList ret = new FunctionList(type);
			ret.object = (BinaryScriptable)args[0];
			return ret;
		}
		
		@Override
		public String getName() {
			return typeString;
		}
		
		// メソッド実行.
		public Object function(
			Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			switch(type) {
			case 0: //"copy"
				return object.copy(args);
			case 1: //"isGzip"
				return object.isGzip();
			case 2: //"toBase64"
				return object.toBase64();
			case 3: //"toGzip"
				object.toGzip();
			case 4: //"toString"
				return object.toString(args);
			case 5: //"toUnGzip"
				object.toUnGzip();
			}
			// プログラムの不具合以外にここに来ることは無い.
			throw new RhilaException(
				"An unspecified error (type: " + type + ") occurred");
		}
	}
	
	// BinaryScriptableのオブジェクト利用.
	public static final class BinaryScriptableObject
		extends AbstractRhinoFunction {
	    // lambda snapStart CRaC用.
	    protected static final BinaryScriptableObject LOAD_CRAC =
	    	new BinaryScriptableObject();
	    
		@Override
		@SuppressWarnings("rawtypes")
		public Scriptable newInstance(
			Context arg0, Scriptable arg1, Object[] arg2) {
			if(arg2 == null || arg2.length == 0) {
				throw new RhilaException("Argument not valid");
			}
			// バイナリ生成オプション群.
			int len = arg2.length;
			if(len == 1) {
				Object o = arg2[0];
				if(o instanceof byte[]) {
					return new BinaryScriptable((byte[])o);
				} else if(o instanceof BinaryScriptable) {
					return new BinaryScriptable((BinaryScriptable)o);
				} else if(o instanceof Number) {
					return new BinaryScriptable(((Number)o).intValue());
				} else if(o instanceof String) {
					return new BinaryScriptable((String)o);
				} else if(o instanceof List) {
					List lst = (List)o;
					len = lst.size();
					byte[] bin = new byte[len];
					for(int i = 0; i < len; i ++) {
						bin[i] = NumberUtil.parseInt(lst.get(i)).byteValue();
					}
					return new BinaryScriptable(bin);					
				}
			} else if(len == 2) {
				if(arg2[0] instanceof String && arg2[1] instanceof String) {
					return new BinaryScriptable((String)arg2[0], (String)arg2[1]);
				}
			} else {
				byte[] bin = new byte[len];
				for(int i = 0; i < len; i ++) {
					bin[i] = NumberUtil.parseInt(arg2[i]).byteValue();
				}
				return new BinaryScriptable(bin);
			}
			throw new RhilaException("Argument not valid");		
		}
		
		public static final BinaryScriptable newInstance(Number src) {
			return new BinaryScriptable(src.intValue());
		}
		
		public static final BinaryScriptable newInstance(String src) {
			return new BinaryScriptable(src);
		}
		
		public static final BinaryScriptable newInstance(byte[] src) {
			return new BinaryScriptable(src);
		}
		
		@Override
		public String getName() {
			return "Binary";
		}
		@Override
		public String toString() {
			return "[Binary]";
		}
	}
}
