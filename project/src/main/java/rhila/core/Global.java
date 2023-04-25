package rhila.core;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import rhila.RhilaException;
import rhila.WrapUtil;
import rhila.scriptable.Base64Scriptable;
import rhila.scriptable.BinaryScriptable;
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
		return WrapUtil.wrap(super.get(arg0, arg1));
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
    
	// globalでの呼び出しメソッドを設定する.
    private final String[] GLOBAL_FUNCTION_LIST = {
    	"gc",
    	"eval",
    	"binary",
    	"jsMap",
    	"jsList"
    };
	
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
    	
        // globalメソッド定義.
		super.defineFunctionProperties(
			GLOBAL_FUNCTION_LIST, Global.class, ScriptableObject.DONTENUM);
		
		// 登録済み変数の削除.
		ScriptableObject.deleteProperty(this, "global");
		ScriptableObject.deleteProperty(this, "JSON");
		
		// 変数定義.
		ScriptableObject.putConstProperty(this, "global", this);
		ScriptableObject.putConstProperty(this, "env", env);
		ScriptableObject.putConstProperty(
			this, "JSON", JsonScriptable.INSTANCE);
		ScriptableObject.putConstProperty(
			this, "Base64", Base64Scriptable.INSTANCE);
		
		// 初期化済み.
		initFlag = true;
    }
    
    // javaガページコレクター実行.
    public static void gc(
    	Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        System.gc();
    }
	
    // evalでスクリプト実行.
	public static Object eval(
		Context cx, Scriptable thisObj, Object[] args, Function funObj) {
		return RunScript.eval((Global)thisObj, String.valueOf(args[0]));
	}
	
	// binary生成.
	public static Object binary(
		Context cx, Scriptable thisObj, Object[] args, Function funObj) {
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

	// map生成.
	public static Object jsMap(
		Context cx, Scriptable thisObj, Object[] args, Function funObj) {
		return new MapScriptable();
	}
	
	// list生成.
	public static Object jsList(
		Context cx, Scriptable thisObj, Object[] args, Function funObj) {
		return new ListScriptable();
	}
}

