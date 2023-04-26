package rhila.scriptable;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhila.RhilaException;

/**
 * Binary用Scriptable.
 */
public class BinaryScriptable implements RhinoScriptable<byte[]> {
	// 格納バイナリ.
	private byte[] binary;
	
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
	
	@Override
	public Object get(String arg0, Scriptable arg1) {
		return getFunction(arg0); 
	}
	
	// function取得.
	private final Object getFunction(String name) {
		switch(name) {
		case "length":
			return binary.length;
		case "toString":
			if(TOSTRING == null) {
				TOSTRING = new ToString();
			}
			return TOSTRING;
		case "toBase64":
			if(TOBASE64 == null) {
				TOBASE64 = new ToBase64();
			}
			return TOBASE64;
		case "copy":
			if(COPY == null) {
				COPY = new Copy();
			}
			return COPY;
		}
		return null;
	}
	
	// 文字列変換処理.
	private final class ToString extends AbstractRhinoFunction {
		protected ToString() {}
		@Override
		public String getName() {
			return "toString";
		}

		@Override
		public Object function(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if(args == null || args.length == 0) {
				return convertString();
			}
			return convertString(String.valueOf(args[1]));
		}
	}
	private ToString TOSTRING = null;
	
	// base64変換.
	private final class ToBase64 extends AbstractRhinoFunction {
		protected ToBase64() {}
		@Override
		public String getName() {
			return "toBase64";
		}

		@Override
		public Object function(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			return rhila.lib.Base64.encode(binary);
		}
	}
	private ToBase64 TOBASE64 = null;
	
	// copy.
	private final class Copy extends AbstractRhinoFunction {
		protected Copy() {}
		@Override
		public String getName() {
			return "copy";
		}

		@Override
		public Object function(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			// 引数(destBin, srcPos, destPos, len).
			// 戻り値: 実際にコピーされた長さ.
			if(args.length < 1 || !(args[0] instanceof BinaryScriptable)) {
				return -1;
			}
			int len = -1;
			try {
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
				if(srcPos + len > binary.length) {
					len = binary.length - srcPos;
					// コピー対象が存在しない.
					if(len <= 0) {
						return -1;
					}
				}
				System.arraycopy(binary, srcPos, bin, destPos, len);
			} catch(Exception e) {
				return -1;
			}
			return len;
		}
	}
	private Copy COPY = null;
}
