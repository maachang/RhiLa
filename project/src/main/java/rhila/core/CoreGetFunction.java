package rhila.core;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhila.core.Console.Log;
import rhila.lib.ArrayMap;
import rhila.scriptable.AbstractRhinoFunction;
import rhila.scriptable.RhinoGetFunction;

/**
 * core用function.
 */
public final class CoreGetFunction implements RhinoGetFunction {
    // lambda snapStart CRaC用.
	public static final CoreGetFunction LOAD_CRAC = new CoreGetFunction();
	// functionインスタンス管理用.
	private static final ArrayMap<String, Scriptable> instanceList;
	
	// 初期設定.
	static {
		instanceList = new ArrayMap<String, Scriptable>(
			"gc", Gc.LOAD_CRAC
			,"eval", Eval.LOAD_CRAC
			,"className", ClassName.LOAD_CRAC
			,"log", Log.LOAD_CRAC
			,"print", Print.LOAD_CRAC
			,"errPrint", ErrPrint.LOAD_CRAC
		);
		
		// lambda snapStart CRaC用.
		ProcessEnv.LOAD_CRAC.getClass();
		RunScript.LOAD_CRAC.getClass();
		Console.LOAD_CRAC.getClass();
		Gc.LOAD_CRAC.getClass();
		Eval.LOAD_CRAC.getClass();
		ClassName.LOAD_CRAC.getClass();
		Print.LOAD_CRAC.getClass();
		ErrPrint.LOAD_CRAC.getClass();
		Log.LOAD_CRAC.getClass();
	}
	
	// オブジェクトを取得.
	public static final CoreGetFunction getInstance() {
		return LOAD_CRAC;
	}
	
	// コンストラクタ.
	protected CoreGetFunction() {}
	
    // Functionを取得.
	public final Scriptable getFunction(String name) {
		return instanceList.get(name);
	}
	    	    
    // Javaクラス名を取得.
	private static final class ClassName extends AbstractRhinoFunction {
	    // lambda snapStart CRaC用.
		protected static final ClassName LOAD_CRAC = new ClassName();
		// コンストラクタ.
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
	    // lambda snapStart CRaC用.
		protected static final Print LOAD_CRAC = new Print();
		// コンストラクタ.
		protected Print() {}
		@Override
		public String getName() {
			return "print";
		}

		@Override
		public Object function(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			return Log.LOAD_CRAC.function(ctx, scope, thisObj, args);
		}
	}
	
    // Print.
	private static final class ErrPrint extends AbstractRhinoFunction {
	    // lambda snapStart CRaC用.
		protected static final ErrPrint LOAD_CRAC = new ErrPrint();
		// コンストラクタ.
		protected ErrPrint() {}
		@Override
		public String getName() {
			return "errPrint";
		}

		@Override
		public Object function(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if(args == null || args.length == 0) {
				Console.log("[ERROR]undefined");
			} else {
				Object o = args[0];
				if(o == null) {
					Console.log("[ERROR]null");
				} else if(o instanceof Undefined) {
					Console.log("[ERROR]null");
				} else {
					Console.log("[ERROR]" + o);
				}
			}
			return Undefined.instance;
		}
	}
    
    // javaガページコレクター実行.
	private static final class Gc extends AbstractRhinoFunction {
	    // lambda snapStart CRaC用.
		protected static final Gc LOAD_CRAC = new Gc();
		// コンストラクタ.
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
	    // lambda snapStart CRaC用.
		protected static final Eval LOAD_CRAC = new Eval();
		// コンストラクタ.
		protected Eval() {}
		@Override
		public String getName() {
			return "eval";
		}

		@Override
		public Object function(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			return RunScript.eval(thisObj, String.valueOf(args[0]));
		}
	}	
}
