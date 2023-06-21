package rhila.scriptable;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhila.RhilaException;
import rhila.lib.ByteArrayBuffer;
import rhila.lib.NumberUtil;

/**
 * Binary用Scriptable.
 */
public class BinaryScriptable extends
	AbstractRhinoCustomNewInstance<byte[]> {
	
    // lambda snapStart CRaC用.
    protected static final BinaryScriptable LOAD_CRAC =
    	new BinaryScriptable();
    
	// [js]メソッド名群(sort済み).
    // この設定は initFunctions() 実行する行の前に定義が必要.
	private static final String[] FUNCTION_NAMES = new String[] {
		"copy"
		,"isGzip"
		,"toBase64"
		,"toGzip"
		,"toString"
		,"toUnGzip"
	};
	
	// 初期化.
    static {
    	// function初期化.
    	LOAD_CRAC.initFunctions();
    }
    
	// 格納バイナリ.
	private byte[] binary;
    
    // コンストラクタ.
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
	
	// function名群を返却.
	// ここでの返却値はstaticで名前はソート済みで設定します.
	@Override
	protected String[] getFunctionNames() {
		return FUNCTION_NAMES;
	}
	
	// function実行.
	// type＝実行対象のgetFunctionNames()のIndex値が設定されます.
	@Override
	protected Object callFunction(int type, Object[] args) {
		switch(type) {
		case 0: //"copy"
			return copy(args);
		case 1: //"isGzip"
			return isGzip();
		case 2: //"toBase64"
			return toBase64();
		case 3: //"toGzip"
			toGzip();
			return this;
		case 4: //"toString"
			return toString(args);
		case 5: //"toUnGzip"
			toUnGzip();
			return this;
		}
		// プログラムの不具合以外にここに来ることは無い.
		throw new RhilaException(
			"An unspecified error (type: " + type + ") occurred");
	}
	
	// 名前を返却.
	@Override
	public String getName() {
		return "Binary";
	}
	
	// 元情報を取得.
	@Override
	public byte[] getRaw() {
		return binary;
	}
	
	// function取得.
	@Override
	protected final Object getFunction(String name) {
		// staticFlag条件を含むのでまず読み込む.
		Object ret = super.getFunction(name);
		// 返却時がnullで、返却したいnameのvalueが存在する場合.
		if(ret == null && "length".equals(name)) {
			return size();
		}
		return ret;
	}
	
	// 新しいインスタンスを生成.
	//  > new Binary(...)
	// を設定してnewInstanceを定義します.
	// [js]のパラメータをargsに設定されます.
	@SuppressWarnings("rawtypes")
	@Override
	public Scriptable newInstance(Object[] args) {
		if(args == null || args.length == 0) {
			throw new RhilaException("Argument not valid");
		}
		// バイナリ生成オプション群.
		int len = args.length;
		if(len == 1) {
			Object o = args[0];
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
			if(args[0] instanceof String && args[1] instanceof String) {
				return new BinaryScriptable((String)args[0], (String)args[1]);
			}
		} else {
			byte[] bin = new byte[len];
			for(int i = 0; i < len; i ++) {
				bin[i] = NumberUtil.parseInt(args[i]).byteValue();
			}
			return new BinaryScriptable(bin);
		}
		throw new RhilaException("Argument not valid");		
	}
	
	@Override
	public String resultToString() {
		return (String)toString(null);
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
			new GZIPInputStream(
				new ByteArrayInputStream(binary), 512);
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
}
