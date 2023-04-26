package rhila.core;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhila.RhilaException;
import rhila.scriptable.AbstractRhinoFunction;
import rhila.scriptable.Base64Scriptable;
import rhila.scriptable.BinaryScriptable;
import rhila.scriptable.DateScriptable;
import rhila.scriptable.JsonScriptable;
import rhila.scriptable.ListScriptable.ListScriptableObject;
import rhila.scriptable.MapScriptable.MapScriptableObject;
import rhila.scriptable.RhinoGetFunction;

/**
 * common定義function.
 */
public final class CommonGetFunction implements RhinoGetFunction {
	protected CommonGetFunction() {}
	public static CommonGetFunction SNGL = null;
	
	// オブジェクトを取得.
	public static final CommonGetFunction getInstance() {
		if(SNGL == null) {
			SNGL = new CommonGetFunction();
		}
		return SNGL;
	}
    
    // Functionを取得.
	public final Scriptable getFunction(String name) {
		switch(name) {
		case "Date":
			if(DATE == null) {
				// オブジェクト定義として生成.
				DATE = new DateScriptable();
				DATE.setStatic(true);
			}
			return DATE;			
		case "JSON":
			if(JSON == null) {
				JSON = new JsonScriptable();
			}
			return JSON;			
		case "Base64": 
			if(BASE64 == null) {
				BASE64 = new Base64Scriptable();
			}
			return BASE64;
		case "Map":
			if(MAP == null) {
				MAP = new MapScriptableObject();
			}
			return MAP;
		case "List":
			if(LIST == null) {
				LIST = new ListScriptableObject();
			}
			return LIST;
		case "gc":
			if(GC == null) {
				GC = new Gc();
			}
			return GC;
		case "eval":
			if(EVAL == null) {
				EVAL = new Eval();
			}
			return EVAL;
		case "binary":
			if(BINARY == null) {
				BINARY = new Binary();
			}
			return BINARY;
		case "className":
			if(CLASSNAME == null) {
				CLASSNAME = new ClassName();
			}
			return CLASSNAME;
		}
		return null;
	}
	
    // Global利用可能オブジェクト.
    private JsonScriptable JSON = null;
    private Base64Scriptable BASE64 = null;
    private DateScriptable DATE = null;
    private MapScriptableObject MAP = null;
    private ListScriptableObject LIST = null;
    
    // Javaクラス名を取得.
	private final class ClassName extends AbstractRhinoFunction {
		protected ClassName() {}
		@Override
		public String getName() {
			return "className";
		}

		@Override
		public Object function(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if(args == null || args.length == 0 ||
				args[0] == null || args[0] instanceof Undefined) {
				return "";
			}
			return args[0].getClass().getName();
		}
	}
	private ClassName CLASSNAME = null;
    
    // javaガページコレクター実行.
	private final class Gc extends AbstractRhinoFunction {
		protected Gc() {}
		@Override
		public String getName() {
			return "gc";
		}

		@Override
		public Object function(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			System.gc();
			return null;
		}
	}
	private Gc GC = null;
	
    // evalでスクリプト実行.
	private final class Eval extends AbstractRhinoFunction {
		protected Eval() {}
		@Override
		public String getName() {
			return "eval";
		}

		@Override
		public Object function(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			return RunScript.eval((Global)thisObj, String.valueOf(args[0]));
		}
	}
	private Eval EVAL = null;
	
	// binary生成.
	private final class Binary extends AbstractRhinoFunction {
		protected Binary() {}
		@Override
		public String getName() {
			return "binary";
		}

		@Override
		public Object function(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
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
				}
			} else if(len == 2) {
				if(args[0] instanceof String && args[1] instanceof String) {
					return new BinaryScriptable((String)args[0], (String)args[1]);
				}
			}
			throw new RhilaException("Argument not valid");		
		}
	}
	private Binary BINARY = null;
}
