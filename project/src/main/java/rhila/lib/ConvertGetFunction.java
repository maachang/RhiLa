package rhila.lib;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import rhila.RhilaException;
import rhila.scriptable.AbstractRhinoFunction;
import rhila.scriptable.RhinoGetFunction;

/**
 * 変換用定義function.
 */
public class ConvertGetFunction implements RhinoGetFunction {
	protected ConvertGetFunction() {}
	public static ConvertGetFunction SNGL = null;
	private static final ArrayMap<String, Scriptable> instanceList =
		new ArrayMap<String, Scriptable>();
		
	// 初期設定.
	static {
		instanceList.put("isNull", new IsNull());
		instanceList.put("useString", new UseString());
		instanceList.put("isBool", new IsBoolean());
		instanceList.put("isBoolean", instanceList.get("isBool"));
		instanceList.put("parseBool", new ParseBoolean());
		instanceList.put("parseBoolean", instanceList.get("parseBool"));
		instanceList.put("isNumber", new IsNumeric());
		instanceList.put("isNumeric", instanceList.get("isNumber"));
		instanceList.put("isFloat", new IsFloat());
		instanceList.put("parseInt32", new ParseInt32());
		instanceList.put("parseInt64", new ParseInt64());
	};
	
	// オブジェクトを取得.
	public static final ConvertGetFunction getInstance() {
		if(SNGL == null) {
			SNGL = new ConvertGetFunction();
		}
		return SNGL;
	}

    // Functionを取得.
	@Override
	public Scriptable getFunction(String name) {
		return instanceList.get(name);
	}
	
	private static final class IsNull extends AbstractRhinoFunction {
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
}
