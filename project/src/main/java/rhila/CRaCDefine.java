package rhila;

import rhila.core.CoreGetFunction;
import rhila.core.GlobalFactory;
import rhila.lib.http.HttpGetFunction;
import rhila.lib.http.HttpUtil;
import rhila.lib.http.client.HttpClientGetFunction;
import rhila.lib.http.client.RhilaSocketFactory;
import rhila.scriptable.ScriptableGetFunction;

/**
 * 外部CRaC定義.
 * ここでLambdaでのCRaC定義を行います.
 */
public final class CRaCDefine {
	protected CRaCDefine() {}
	
    // lambda snapStart CRaC用.
	public static final CRaCDefine LOAD_CRAC = new CRaCDefine();
	
    // Lambda snapStart用 CRaC呼び出し.
	static {
		// net初期設定.
		HttpUtil.initNet();
		
		// globalFactory.
		GlobalFactory.LOAD_CRAC.getClass();
		
		// runtimeJava(よく使うJavaオブジェクト).
		RuntimeJavaCRaCDefine.LOAD_CRAC.getClass();
		
		// RhilaException.
		RhilaException.LOAD_CRAC.getClass();
		
		// RhilaConstants.
		RhilaConstants.LOAD_CRAC.getClass();
		
		// Core用LoadFunctions.
		CoreGetFunction.LOAD_CRAC.getClass();
		
		// Scriptable用LoadFunctions.
		ScriptableGetFunction.LOAD_CRAC.getClass();
		
		// Http用LoadFunctions.
		HttpGetFunction.LOAD_CRAC.getClass();
		
		// HttpClient用LoadFunctions.
		HttpClientGetFunction.LOAD_CRAC.getClass();
		
		// RhiLa用SocketFactory.
		RhilaSocketFactory.LOAD_CRAC.getClass();
	}
}
