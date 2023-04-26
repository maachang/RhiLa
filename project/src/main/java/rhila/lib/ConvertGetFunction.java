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
		switch(name) {
		case "isNull":
			if(ISNULL == null) {
				ISNULL = new IsNull();
			}
			return ISNULL;
		case "useString":
			if(USESTRING == null) {
				USESTRING = new UseString();
			}
			return USESTRING;
		case "isBool":
		case "isBoolean":
			if(ISBOOLEAN == null) {
				ISBOOLEAN = new IsBoolean();
			}
			return ISBOOLEAN;
		case "parseBool":
		case "parseBoolean":
			if(PARSEBOOLEAN == null) {
				PARSEBOOLEAN = new ParseBoolean();
			}
			return PARSEBOOLEAN;
		case "isNumber":
		case "isNumeric":
			if(ISNUMERIC == null) {
				ISNUMERIC = new IsNumeric();
			}
			return ISNUMERIC;
		case "isFloat":
			if(ISFLOAT == null) {
				ISFLOAT = new IsFloat();
			}
			return ISFLOAT;
		case "parseInt32":
			if(PARSEINT32 == null) {
				PARSEINT32 = new ParseInt32();
			}
			return PARSEINT32;
		}
		return null;
	}
	
	private final class IsNull extends AbstractRhinoFunction {
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
	private IsNull ISNULL = null;
	
	private final class UseString extends AbstractRhinoFunction {
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
	private UseString USESTRING = null;

	private final class IsBoolean extends AbstractRhinoFunction {
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
	private IsBoolean ISBOOLEAN = null;
	
	private final class ParseBoolean extends AbstractRhinoFunction {
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
	private ParseBoolean PARSEBOOLEAN = null;
	
	private final class IsNumeric extends AbstractRhinoFunction {
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
	private IsNumeric ISNUMERIC = null;
	
	private final class IsFloat extends AbstractRhinoFunction {
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
	private IsFloat ISFLOAT = null;

	private final class ParseInt32 extends AbstractRhinoFunction {
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
	private ParseInt32 PARSEINT32 = null;

}
