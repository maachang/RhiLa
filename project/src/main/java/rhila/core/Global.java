package rhila.core;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import rhila.RhilaException;
import rhila.lib.ConvertGetFunction;
import rhila.scriptable.RhilaWrapper;
import rhila.scriptable.RhinoGetFunction;

/**
 * globalオブジェクト.
 */
public class Global extends ImporterTopLevel {
    private static final long serialVersionUID = 6802578607688922051L;
    
    // rhino js Optimization Level.
    // 最適化なし.
    private static final int SCRIPT_OPTIMIZATION_LEVEL = -1;
    
    // java LanguageVersion.
    // rhinoでの現状の最大サポート.
    private static final int SCRIPT_LANGUAGE_VERSION = Context.VERSION_ES6;
    
    // 初期設定フラグ.
    private boolean initFlag = false;
    
    // context.
    private Context ctx = null;
    
    // env定義.
    private ProcessEnv env;
    
    // シングルトン.
    private static final Global SNGL = new Global();
    
    // 新しいオブジェクトを取得.
    public static final Global getInstance(
    	ContextFactory factory, ProcessEnv env) {
    	Global ret = SNGL.newInstance();
        ret.initContextFactory(factory, env);
        return ret;
    }
    
    protected Global() {}
    
    // 新しいオブジェクトを生成する.
    private final Global newInstance() {
    	return new Global();
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
        ctx.setOptimizationLevel(SCRIPT_OPTIMIZATION_LEVEL);
        
        // 言語バージョンを設定.
        ctx.setLanguageVersion(SCRIPT_LANGUAGE_VERSION);
        
        // スタンダードオブジェクトを呼び出す.
        super.initStandardObjects(ctx, false);
        
        // 登録済み変数の削除.
        ScriptableObject.deleteProperty(this, "global");
        ScriptableObject.deleteProperty(this, "eval");
        ScriptableObject.deleteProperty(this, "Date");
        ScriptableObject.deleteProperty(this, "Object");
        ScriptableObject.deleteProperty(this, "Array");
        
        // 変数定義.
        ScriptableObject.putConstProperty(this, "global", this);
        ScriptableObject.putConstProperty(this, "env", env);
        
        // 初期化済み.
        initFlag = true;
    }
    
    // 利用可能なGlobalFunction群登録リスト.
    private static final RhinoGetFunction[] GET_FUNCTIONS = new RhinoGetFunction[] {
        CommonGetFunction.getInstance()
        ,ConvertGetFunction.getInstance()
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
}