package rhila.lib;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import rhila.RhilaException;
import rhila.scriptable.AbstractRhinoFunction;
import rhila.scriptable.AbstractRhinoFunctionInstance;

/**
 * 精度の高く高速なランダム発生装置.
 */
public final class Xor128 {
    // lambda snapStart CRaC用.
    protected static final Xor128 LOAD_CRAC = new Xor128();
    
	// instance可能なScriptable.
	protected static final ArrayMap<String, Scriptable> instanceList;
	
	static {
		instanceList = new ArrayMap<String, Scriptable>(
			"setSeed", SetSeed.LOAD_CRAC
			,"nextInt", NextInt.LOAD_CRAC
			,"nextString", NextString.LOAD_CRAC		
		);
	}
	
   	private int a = 123456789;
	private int b = 362436069;
	private int c = 521288629;
	private int d = 88675123;
	
	// objectインスタンスリスト.
	private final ArrayMap<String, Object> objInsList =
		new ArrayMap<String, Object>();

	/**
	 * コンストラクタ.
	 */
	public Xor128() {
	}
	
	/**
	 * コンストラクタ.
	 */
	public Xor128(Number n) {
		if(n != null) {
			setSeed(n);
		}
	}
	
	/**
	 * ランダム係数を設定.
	 *
	 * @param ss
	 *            ランダム係数を設定します.
	 */
	public final void setSeed(Number ss) {
		long ls = ss.longValue();
		int s = (int) (ls & 0x00000000ffffffffL);
		a = s = 1812433253 * (s ^ (s >> 30)) + 1;
		b = s = 1812433253 * (s ^ (s >> 30)) + 2;
		s = ((int)(ls & 0xffffffff00000000L >> 32L) ^ s);
		c = s = 1812433253 * (s ^ (s >> 30)) + 3;
		d = s = 1812433253 * (s ^ (s >> 30)) + 4;
	}

	/**
	 * 32ビット乱数を取得.
	 *
	 * @return int 32ビット乱数が返されます.
	 */
	public final int nextInt() {
		int t, r;
		t = a;
		r = t;
		t <<= 11;
		t ^= r;
		r = t;
		r >>= 8;
		t ^= r;
		r = b;
		a = r;
		r = c;
		b = r;
		r = d;
		c = r;
		t ^= r;
		r >>= 19;
		r ^= t;
		d = r;
		return r;
	}
	
	/**
	 * ランダム文字列を出力.
	 * @param size 生成文字列を設定します.
	 * @return String 文字列が返却されます.
	 */
	public final String nextString(int size) {
		if(size <= 0) {
			return "";
		}
		int i, p, n;
		final int len4 = size >> 2;
		final int lenEtc = size - (len4 << 2);
		final int len = (int)((((len4) * 3) + lenEtc) * 1.25);
		final byte[] bin = new byte[len];
		for(i = 0, p = 0; i < len4; i ++) {
			n = nextInt();
			bin[p] = (byte)(n & 0x0ff);
			bin[p ++] = (byte)((n & 0x0ff00) >> 8);
			bin[p ++] = (byte)((n & 0x0ff0000) >> 16);
			bin[p ++] = (byte)((n & 0x0ff000000) >> 24);
		}
		for(i = 0; i < lenEtc; i ++) {
			bin[p] = (byte)(nextInt() & 0x0ff);
		}
		try {
			return Base64.encode(bin).substring(0, size);
		} catch(Exception e) {
			throw new RhilaException(e);
		}
	}
	
	// setSeed function
	private static final class SetSeed extends AbstractRhinoFunctionInstance {
	    // lambda snapStart CRaC用.
	    protected static final SetSeed LOAD_CRAC = new SetSeed();
		private Xor128 src;
		
		@Override
		public Scriptable getInstance(Object... args) {
			SetSeed ret = new SetSeed();
			ret.src = (Xor128)args[0];
			return ret;
		}
		
		@Override
		public String getName() {
			return "setSeed";
		}
		
		@Override
		public Object function(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if(args != null && args.length >= 1) {
				src.setSeed(NumberUtil.parseLong(args[0]));
			}
			return null;
		}
	}
	
	// nextInt function
	private static final class NextInt extends AbstractRhinoFunctionInstance {
	    // lambda snapStart CRaC用.
	    protected static final NextInt LOAD_CRAC = new NextInt();
		private Xor128 src;
		
		@Override
		public Scriptable getInstance(Object... args) {
			NextInt ret = new NextInt();
			ret.src = (Xor128)args[0];
			return ret;
		}
		
		@Override
		public String getName() {
			return "nextInt";
		}
		
		@Override
		public Object function(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			return src.nextInt();
		}
	}
	
	// nextString function
	private static final class NextString extends AbstractRhinoFunctionInstance {
	    // lambda snapStart CRaC用.
	    protected static final NextString LOAD_CRAC = new NextString();
		private Xor128 src;
		
		@Override
		public Scriptable getInstance(Object... args) {
			NextString ret = new NextString();
			ret.src = (Xor128)args[0];
			return ret;
		}
		
		@Override
		public String getName() {
			return "nextString";
		}
		
		@Override
		public Object function(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if(args != null && args.length >= 1) {
				return src.nextString(NumberUtil.parseInt(args[0]));
			}
			return "";

		}
	}
	
	// RandomScriptableのオブジェクト利用.
	public static final class RandomScriptableObject extends AbstractRhinoFunction {
	    // lambda snapStart CRaC用.
	    protected static final RandomScriptableObject LOAD_CRAC = new RandomScriptableObject();
		private Xor128 src;
		private boolean staticFlag = true;
		
		@Override
		public Scriptable newInstance(Context arg0, Scriptable arg1, Object[] arg2) {
			RandomScriptableObject ret = new RandomScriptableObject();
			if(arg2 == null || arg2.length == 0) {
				ret.src = new Xor128();
			} else {
				Object o = arg2[0];
				if(o instanceof Number) {
					ret.src = new Xor128((Number)o);
				} else if(o instanceof String) {
					ret.src = new Xor128(NumberUtil.parseLong(o));
				} else {
					ret.src = new Xor128();
				}
			}
			ret.staticFlag = false;
			return ret;
		}
		
		public static final RandomScriptableObject getInstance(Xor128 src) {
			RandomScriptableObject ret = new RandomScriptableObject();
			ret.src = src;
			ret.staticFlag = false;
			return ret;
		}
		
		@Override
		public String getName() {
			return "random";
		}
		@Override
		public String toString() {
			if(staticFlag) {
				return "[Random]";
			}
			return String.valueOf(src.nextInt());
		}
		
		@Override
		public Object get(String arg0, Scriptable arg1) {
			return getFunction(arg0);
		}
		
		// function取得.
		private final Object getFunction(String name) {
			// staticの場合.
			if(staticFlag) {
				return null;
			}
			// オブジェクト管理の生成Functionを取得.
			Object ret = src.objInsList.get(name);
			// 存在しない場合.
			if(ret == null) {
				// static管理のオブジェクトを取得.
				ret = instanceList.get(name);
				// 存在する場合.
				if(ret != null) {
					// オブジェクト管理の生成Functionとして管理.
					ret = ((AbstractRhinoFunctionInstance)ret)
						.getInstance(src);
					src.objInsList.put(name, ret);
				}
			}
			return ret;
		}
	}
}

