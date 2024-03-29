package rhila.core;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import rhila.lib.LibGetFunction;
import rhila.lib.http.HttpGetFunction;
import rhila.lib.http.client.HttpClientGetFunction;
import rhila.scriptable.RhilaWrapper;
import rhila.scriptable.RhinoGetFunction;
import rhila.scriptable.ScriptableGetFunction;

/**
 * globalオブジェクト.
 * 
 * ※現状この実装は、Lambda実行毎にGlobal作らないので、
 *   この場合は他のデータを保持し続ける事になるから、これは
 *   大きな修正が必要となる.
 */
public class Global extends ImporterTopLevel {
    private static final long serialVersionUID = 6802578607688922051L;
    
    // rhino js Optimization Level.
    // 最適化なし.
    private static final int SCRIPT_OPTIMIZATION_LEVEL = -1;
    
    // java LanguageVersion.
    // rhinoでの現状の最大サポート.
    private static final int SCRIPT_LANGUAGE_VERSION = Context.VERSION_ES6;
    
    // [rhino]context.
    private Context ctx;
    
    // [lambda]Context.
    private com.amazonaws.services.lambda.runtime.Context lambdaCtx;
    
    // env定義.
    private ProcessEnv env;
    
    // コンストラクタ.
    protected Global() {}
    
    // context取得.
    public Context getContext() {
        return ctx;
    }
    
    // processEnvを取得.
    public ProcessEnv getEnv() {
        return env;
    }
    
    // env設定.
    protected Global setEnv(ProcessEnv env) {
    	this.env = env;
        ScriptableObject.deleteProperty(this, "env");
        ScriptableObject.putConstProperty(this, "env", env);
        return this;
    }
    
    // lambda用Contextを取得.
    public com.amazonaws.services.lambda.runtime.Context
    	getLambdaContext() {
    	return lambdaCtx;
    }
    
    // lambda用Contextを設定.
    public Global setLambdaContext(
    	com.amazonaws.services.lambda.runtime.Context lambdaCtx) {
    	this.lambdaCtx = lambdaCtx;
        return this;
    }
    
    @Override
    public String toString() {
        return "[global]";
    }
    
    // 初期化処理.
    protected void initGlobal(Context ctx) {
        this.ctx = ctx;
        
        // javaPrimitiveのwrapをOff.
        ctx.getWrapFactory().setJavaPrimitiveWrap(false);
        
        // Lambda上で実行されるので最適化不要.
        ctx.setOptimizationLevel(SCRIPT_OPTIMIZATION_LEVEL);
        
        // 言語バージョンを設定.
        ctx.setLanguageVersion(SCRIPT_LANGUAGE_VERSION);
        
        // スタンダードオブジェクトを利用する.
        super.initStandardObjects(ctx, false);
        
        // 登録済み変数の削除.
        ScriptableObject.deleteProperty(this, "global");
        ScriptableObject.deleteProperty(this, "eval");
        ScriptableObject.deleteProperty(this, "Function");
        ScriptableObject.deleteProperty(this, "Date");
        ScriptableObject.deleteProperty(this, "Object");
        ScriptableObject.deleteProperty(this, "Array");
        ScriptableObject.deleteProperty(this, "JSON");
        ScriptableObject.deleteProperty(this, "encodeURIComponent");
        ScriptableObject.deleteProperty(this, "decodeURIComponent");
        ScriptableObject.deleteProperty(this, "setTimeout");
        ScriptableObject.deleteProperty(this, "setInterval");
        
        // 変数定義.
        ScriptableObject.putConstProperty(this, "global", this);
        
        // envが設定されていない場合の反映.
        getEnv();
    }
    
    // 利用可能なGlobalFunction群登録リスト.
    private static final RhinoGetFunction[] GET_FUNCTIONS = new RhinoGetFunction[] {
        CoreGetFunction.getInstance()
        ,ScriptableGetFunction.getInstance()
        ,LibGetFunction.getInstance()
        ,HttpGetFunction.getInstance()
        ,HttpClientGetFunction.getInstance()
    };
    private static final int GET_FUNCTION_LENGTH = GET_FUNCTIONS.length;
    
    // global情報を取得.
    @Override
    public Object get(String arg0, Scriptable arg1) {
        Object ret = null;
        for(int i = 0; i < GET_FUNCTION_LENGTH; i ++) {
            ret = GET_FUNCTIONS[i].getFunction(arg0);
            if(ret != null) {
                return ret;
            }
        }
        // 存在しない場合はGlobal内容を取得.
        return RhilaWrapper.wrap(super.get(arg0, arg1));
    }
    
    @Override
    public Object get(int arg0, Scriptable arg1) {
        return RhilaWrapper.wrap(super.get(arg0, arg1));
    }
    
    @Override
    public void put(String arg0, Scriptable arg1, Object arg2) {
        super.put(arg0, arg1, RhilaWrapper.unwrap(arg2));
    }

    @Override
    public void put(int arg0, Scriptable arg1, Object arg2) {
        super.put(arg0, arg1, RhilaWrapper.unwrap(arg2));
    }
}