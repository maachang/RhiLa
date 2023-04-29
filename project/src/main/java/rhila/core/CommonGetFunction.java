package rhila.core;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhila.RhilaException;
import rhila.lib.ArrayMap;
import rhila.scriptable.AbstractRhinoFunction;
import rhila.scriptable.Base64Scriptable;
import rhila.scriptable.BinaryScriptable;
import rhila.scriptable.DateScriptable;
import rhila.scriptable.JsonScriptable;
import rhila.scriptable.ListScriptable.ListScriptableObject;
import rhila.scriptable.LowerKeyMapScriptable.LowerKeyMapScriptableObject;
import rhila.scriptable.MapScriptable.MapScriptableObject;
import rhila.scriptable.RhinoGetFunction;

/**
 * common定義function.
 */
public final class CommonGetFunction implements RhinoGetFunction {
	protected CommonGetFunction() {}
	public static final CommonGetFunction SNGL = new CommonGetFunction();
	private static final ArrayMap<String, Scriptable> instanceList =
		new ArrayMap<String, Scriptable>();
	
	// 初期設定.
	static {
		instanceList.put("Date", new DateScriptable());
		instanceList.put("JSON", new JsonScriptable());
		instanceList.put("Base64", new Base64Scriptable());
		instanceList.put("Map", new MapScriptableObject());
		instanceList.put("Object", instanceList.get("Map"));
		instanceList.put("LowerKeyMap", new LowerKeyMapScriptableObject());
		instanceList.put("LoMap", instanceList.get("LowerKeyMap"));
		instanceList.put("List", new ListScriptableObject());
		instanceList.put("Array", instanceList.get("List"));
		instanceList.put("gc", new Gc());
		instanceList.put("eval", new Eval());
		instanceList.put("binary", new Binary());
		instanceList.put("className", new ClassName());
		instanceList.put("print", new Print());
		instanceList.put("errPrint", new ErrPrint());
	}
	
	// オブジェクトを取得.
	public static final CommonGetFunction getInstance() {
		return SNGL;
	}
	
    // Functionを取得.
	public final Scriptable getFunction(String name) {
		return instanceList.get(name);
	}
	    	    
    // Javaクラス名を取得.
	private static final class ClassName extends AbstractRhinoFunction {
		protected ClassName() {}
		@Override
		public String getName() {
			return "className";
		}

		// この内容はfunction呼び出しはしない.
		@Override
		public Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if(args == null || args.length == 0 ||
				args[0] == null || args[0] instanceof Undefined) {
				return "";
			}
			return args[0].getClass().getName();
		}
	}
	
    // Print.
	private static final class Print extends AbstractRhinoFunction {
		protected Print() {}
		@Override
		public String getName() {
			return "print";
		}

		@Override
		public Object function(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if(args == null || args.length == 0 ||
				args[0] == null || args[0] instanceof Undefined) {
				System.out.println();
			} else {
				final int len = args.length;
				for(int i = 0; i < len; i ++) {
					System.out.print(args[i] + " ");
				}
				System.out.println();
				
			}
			return Undefined.instance;
		}
	}
	
    // Print.
	private static final class ErrPrint extends AbstractRhinoFunction {
		protected ErrPrint() {}
		@Override
		public String getName() {
			return "errPrint";
		}

		@Override
		public Object function(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if(args == null || args.length == 0 ||
				args[0] == null || args[0] instanceof Undefined) {
				System.err.println();
			} else {
				final int len = args.length;
				for(int i = 0; i < len; i ++) {
					System.err.print(args[i] + " ");
				}
				System.err.println();
			}
			return Undefined.instance;
		}
	}
    
    // javaガページコレクター実行.
	private static final class Gc extends AbstractRhinoFunction {
		protected Gc() {}
		@Override
		public String getName() {
			return "gc";
		}

		@Override
		public Object function(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			System.gc();
			return Undefined.instance;
		}
	}
	
    // evalでスクリプト実行.
	private static final class Eval extends AbstractRhinoFunction {
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
	
	// binary生成.
	private static final class Binary extends AbstractRhinoFunction {
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
}
