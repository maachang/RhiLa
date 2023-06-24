package rhila.core;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;

import rhila.CRaCDefine;

// globalFactory.
public class GlobalFactory {
	protected GlobalFactory() {}
	
    // lambda snapStart CRaC用.
	public static final GlobalFactory LOAD_CRAC = new GlobalFactory();
	
	// スレッド単位でのGlobalオブジェクト管理.
	private static final ThreadLocal<Global> threadGlobal =
		new ThreadLocal<Global>();
	
	// contextFactory.
	protected final ContextFactory factory = new ContextFactory();
	
	// ProcessEnv.
	protected ProcessEnv env = null;
	
    // Lambda snapStart用 CRaC呼び出し.
    static {
		// script実行での実行関連のCRaC呼び出し.
    	Global g = null;
    	try {
    		g = LOAD_CRAC.createGlobal();
    		RunScript.eval(g, "1 + 1;");
    	} catch(Exception e) {
    		e.printStackTrace();
    	} finally {
    		LOAD_CRAC.releaseGlobal(g);
    	}
    }
	
	// オブジェクトを取得.
	public static final GlobalFactory getInstance() {
		return LOAD_CRAC;
	}
	
	// globalオブジェクトを取得.
	public static final Global getGlobal() {
		return LOAD_CRAC.get();
	}
	
	// globalオブジェクトをリリース.
	public static final void releaseGlobal() {
		LOAD_CRAC.release();
	}
	
	// globalオブジェクトを取得.
	public Global get() {
		Global ret = threadGlobal.get();
		if(ret == null) {
			ret = createGlobal();
			threadGlobal.set(ret);
		}
		return ret;
	}
	
	// globalオブジェクトのリリース.
	public GlobalFactory release() {
		Global global = threadGlobal.get();
		if(global != null) {
			releaseGlobal(global);
			threadGlobal.remove();
		}
		return this;
	}
	
	// globalオブジェクトを作成.
	private final Global createGlobal() {
		Global ret = new Global();
		ret.initGlobal(factory.enterContext());
		ret.setEnv(getEnv());
		return ret;
	}
	
	// globalオブジェクトをリリース.
	private final void releaseGlobal(Global global) {
		try {
			// コンテキストをクローズ.
			Context ctx = global.getContext();
			factory.onContextReleased(ctx);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
    // processEnvを取得.
    public ProcessEnv getEnv() {
    	// 存在しない場合は作成する.
    	if(env == null) {
    		setEnv(new ProcessEnv());
    	}
        return env;
    }
    
    // env設定.
    public GlobalFactory setEnv(ProcessEnv env) {
    	this.env = env;
        // 設定された内容は書き込み禁止とする.
        this.env.setWriteLock(true);
        return this;
    }
}
