package rhila.core;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import rhila.RhilaException;
import rhila.WrapUtil;
import rhila.scriptable.AbstractRhinoFunction;
import rhila.scriptable.Base64Scriptable;
import rhila.scriptable.BinaryScriptable;
import rhila.scriptable.DateScriptable;
import rhila.scriptable.JsonScriptable;
import rhila.scriptable.ListScriptable;
import rhila.scriptable.MapScriptable;

/**
 * globalオブジェクト.
 */
public class Global extends ImporterTopLevel {
	private static final long serialVersionUID = 6802578607688922051L;
	
	/** java LanguageVersion. **/
	//private static final int SCRIPT_LANGUAGE_VERSION = Context.VERSION_1_5;
	//private static final int SCRIPT_LANGUAGE_VERSION = Context.VERSION_1_8;
	private static final int SCRIPT_LANGUAGE_VERSION = Context.VERSION_ES6;
	
	// 初期設定フラグ.
	private boolean initFlag = false;
	
	// context.
	private Context ctx = null;
	
	// env定義.
	private ProcessEnv env;
	
	@SuppressWarnings("unused")
	private Global() {}
	
	// コンストラクタ.
	public Global(ContextFactory factory, ProcessEnv env) {
		this.initContextFactory(factory, env);
	}
	
	// 初期化完了チェック.
	public boolean isInit() {
		return initFlag;
	}
    
    // context取得.
    public Context getContext() {
    	return ctx;
    }
    
    // processEnvを取得.
    public ProcessEnv getEnv() {
    	return env;
    }
    
	@Override
	public String toString() {
		return "[global]";
	}
	
	// ContextFactory初期化処理.
    protected void initContextFactory(
    	ContextFactory factory, ProcessEnv env) {
        initGlobal(factory.enterContext(), env);
    }
    
	@Override
	public Object get(String arg0, Scriptable arg1) {
		// 登録メソッドを取得.
		Object ret = getFunction(arg0);
		if(ret == null) {
			// 存在しない場合はGlobal内容を取得.
			ret = WrapUtil.wrap(super.get(arg0, arg1));
		}
		return ret;
	}

	@Override
	public Object get(int arg0, Scriptable arg1) {
		return WrapUtil.wrap(super.get(arg0, arg1));
	}
	
	@Override
	public void put(String arg0, Scriptable arg1, Object arg2) {
		super.put(arg0, arg1, WrapUtil.unwrap(arg2));
	}

	@Override
	public void put(int arg0, Scriptable arg1, Object arg2) {
		super.put(arg0, arg1, WrapUtil.unwrap(arg2));
	}
		
    // 初期化処理.
    protected void initGlobal(Context ctx, ProcessEnv env) {
    	// 初期化済み.
    	if(initFlag) {
    		// エラー出力.
    		throw new RhilaException(
    			"The global definition has already been initialized.");
    	}
    	// processEnvが設定されていない
    	if(env == null) {
    		// 新規作成.
    		env = new ProcessEnv();
    	}
    	this.env = env;
    	this.ctx = ctx;
    	
    	// javaPrimitiveのwrapをOff.
    	ctx.getWrapFactory().setJavaPrimitiveWrap(false);
    	
    	// Lambda上で実行されるので最適化不要.
    	ctx.setOptimizationLevel(-1);
    	
    	// 言語バージョンを設定.
    	ctx.setLanguageVersion(SCRIPT_LANGUAGE_VERSION);
    	
    	// スタンダードオブジェクトを呼び出す.
    	super.initStandardObjects(ctx, false);
		
		// 登録済み変数の削除.
		ScriptableObject.deleteProperty(this, "global");
		ScriptableObject.deleteProperty(this, "Date");
		
		// 変数定義.
		ScriptableObject.putConstProperty(this, "global", this);
		ScriptableObject.putConstProperty(this, "env", env);
		
		// 初期化済み.
		initFlag = true;
    }
    
    // 予約Global定義を取得.
	private final Object getFunction(String name) {
		switch(name) {
		case "Date":
			if(DATE == null) {
				DATE = new DateScriptable();
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
		case "jsMap":
			if(JSMAP == null) {
				JSMAP = new JsMap();
			}
			return JSMAP;
		case "jsList":
			if(JSLIST == null) {
				JSLIST = new JsList();
			}
			return JSLIST;			
		}
		return null;
	}
	
    // Global利用可能オブジェクト.
    private JsonScriptable JSON = null;
    private Base64Scriptable BASE64 = null;
    private DateScriptable DATE = null;
    
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

	// map生成.
	private final class JsMap extends AbstractRhinoFunction {
		protected JsMap() {}
		@Override
		public String getName() {
			return "jsMap";
		}

		@Override
		public Object function(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			return new MapScriptable();
		}
	}
	private JsMap JSMAP = null;
	
	// list生成.
	private final class JsList extends AbstractRhinoFunction {
		protected JsList() {}
		@Override
		public String getName() {
			return "jsList";
		}

		@Override
		public Object function(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			return new ListScriptable();
		}
	}
	private JsList JSLIST = null;
}

