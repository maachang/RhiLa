package rhila.lib;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import rhila.RhilaException;
import rhila.lib.Xor128.RandomScriptableObject;
import rhila.scriptable.AbstractRhinoFunction;
import rhila.scriptable.RhinoGetFunction;

/**
 * lib用function.
 */
public class LibGetFunction implements RhinoGetFunction {
	protected LibGetFunction() {}
    // lambda snapStart CRaC用.
	public static final LibGetFunction LOAD_CRAC = new LibGetFunction();
	// functionインスタンス管理用.
	private static final ArrayMap<String, Scriptable> instanceList;
		
	// 初期設定.
	static {
		instanceList = new ArrayMap<String, Scriptable>(
			"Random", RandomScriptableObject.LOAD_CRAC
			,"isNull", IsNull.LOAD_CRAC
			,"useString", UseString.LOAD_CRAC
			,"isBool", IsBoolean.LOAD_CRAC
			,"isBoolean", IsBoolean.LOAD_CRAC
			,"parseBool", ParseBoolean.LOAD_CRAC
			,"parseBoolean", ParseBoolean.LOAD_CRAC
			,"isNumber", IsNumeric.LOAD_CRAC
			,"isNumeric", IsNumeric.LOAD_CRAC
			,"isFloat", IsFloat.LOAD_CRAC
			,"parseInt32", ParseInt32.LOAD_CRAC
			,"parseInt64", ParseInt64.LOAD_CRAC
			,"encodeURIComponent", EncodeURIComponent.LOAD_CRAC
			,"decodeURIComponent", DecodeURIComponent.LOAD_CRAC
		);
		
		// lambda snapStart CRaC用.
		ArrayMap.LOAD_CRAC.getClass();
		ByteArrayBuffer.LOAD_CRAC.getClass();
		ObjectList.LOAD_CRAC.getClass();
		Xor128.LOAD_CRAC.getClass();
		Base64.LOAD_CRAC.getClass();
		BooleanUtil.LOAD_CRAC.getClass();
		DateUtil.LOAD_CRAC.getClass();
		Json.LOAD_CRAC.getClass();
		NumberUtil.LOAD_CRAC.getClass();
		ObjectUtil.LOAD_CRAC.getClass();
	};
	
	// オブジェクトを取得.
	public static final LibGetFunction getInstance() {
		return LOAD_CRAC;
	}

    // Functionを取得.
	@Override
	public Scriptable getFunction(String name) {
		return instanceList.get(name);
	}
	
	private static final class IsNull extends AbstractRhinoFunction {
	    // lambda snapStart CRaC用.
		protected static final IsNull LOAD_CRAC = new IsNull();
		// コンストラクタ.
		protected IsNull() {}
		@Override
		public String getName() {
			return "isNull";
		}

		@Override
		public Object function(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if(args == null || args.length == 0) {
				return true;
			}
			return ObjectUtil.isNull(args[0]);
		}
	}
	
	private static final class UseString extends AbstractRhinoFunction {
	    // lambda snapStart CRaC用.
		protected static final UseString LOAD_CRAC = new UseString();
		// コンストラクタ.
		protected UseString() {}
		@Override
		public String getName() {
			return "useString";
		}

		@Override
		public Object function(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if(args == null || args.length == 0) {
				return false;
			}
			return ObjectUtil.useString(args[0]);
		}
	}

	private static final class IsBoolean extends AbstractRhinoFunction {
	    // lambda snapStart CRaC用.
		protected static final IsBoolean LOAD_CRAC = new IsBoolean();
		// コンストラクタ.
		protected IsBoolean() {}
		@Override
		public String getName() {
			return "isBoolean";
		}

		@Override
		public Object function(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if(args == null || args.length == 0) {
				return false;
			}
			return BooleanUtil.isBoolean(args[0]);
		}
	}
	
	private static final class ParseBoolean extends AbstractRhinoFunction {
	    // lambda snapStart CRaC用.
		protected static final ParseBoolean LOAD_CRAC = new ParseBoolean();
		// コンストラクタ.
		protected ParseBoolean() {}
		@Override
		public String getName() {
			return "parseBoolean";
		}

		@Override
		public Object function(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if(args == null || args.length == 0) {
				throw new RhilaException("Argument not set.");
			}
			return BooleanUtil.parseBoolean(args[0]);
		}
	}
	
	private static final class IsNumeric extends AbstractRhinoFunction {
	    // lambda snapStart CRaC用.
		protected static final IsNumeric LOAD_CRAC = new IsNumeric();
		// コンストラクタ.
		protected IsNumeric() {}
		@Override
		public String getName() {
			return "isNumeric";
		}

		@Override
		public Object function(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if(args == null || args.length == 0) {
				return false;
			}
			return NumberUtil.isNumeric(args[0]);
		}
	}
	
	private static final class IsFloat extends AbstractRhinoFunction {
	    // lambda snapStart CRaC用.
		protected static final IsFloat LOAD_CRAC = new IsFloat();
		// コンストラクタ.
		protected IsFloat() {}
		@Override
		public String getName() {
			return "isFloat";
		}

		@Override
		public Object function(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if(args == null || args.length == 0) {
				return false;
			}
			return NumberUtil.isFloat(args[0]);
		}
	}

	private static final class ParseInt32 extends AbstractRhinoFunction {
	    // lambda snapStart CRaC用.
		protected static final ParseInt32 LOAD_CRAC = new ParseInt32();
		// コンストラクタ.
		protected ParseInt32() {}
		@Override
		public String getName() {
			return "parseInt32";
		}

		@Override
		public Object function(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if(args == null || args.length == 0) {
				throw new RhilaException("Argument not set.");
			}
			return NumberUtil.parseInt(args[0]);
		}
	}
	
	private static final class ParseInt64 extends AbstractRhinoFunction {
	    // lambda snapStart CRaC用.
		protected static final ParseInt64 LOAD_CRAC = new ParseInt64();
		// コンストラクタ.
		protected ParseInt64() {}
		@Override
		public String getName() {
			return "parseInt64";
		}

		@Override
		public Object function(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if(args == null || args.length == 0) {
				throw new RhilaException("Argument not set.");
			}
			return NumberUtil.parseLong(args[0]);
		}
	}
	
	private static final class EncodeURIComponent extends AbstractRhinoFunction {
	    // lambda snapStart CRaC用.
		protected static final EncodeURIComponent LOAD_CRAC = new EncodeURIComponent();
		// コンストラクタ.
		protected EncodeURIComponent() {}
		@Override
		public String getName() {
			return "encodeURIComponent";
		}

		@Override
		public Object function(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if(args == null || args.length == 0) {
				throw new RhilaException("Argument not set.");
			}
			return ObjectUtil.encodeURIComponent(String.valueOf(args[0]));
		}
	}
	
	private static final class DecodeURIComponent extends AbstractRhinoFunction {
	    // lambda snapStart CRaC用.
		protected static final DecodeURIComponent LOAD_CRAC = new DecodeURIComponent();
		// コンストラクタ.
		protected DecodeURIComponent() {}
		@Override
		public String getName() {
			return "decodeURIComponent";
		}

		@Override
		public Object function(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if(args == null || args.length == 0) {
				throw new RhilaException("Argument not set.");
			}
			return ObjectUtil.decodeURIComponent(String.valueOf(args[0]));
		}
	}
}
