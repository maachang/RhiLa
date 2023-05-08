package rhila;

import rhila.core.CoreGetFunction;
import rhila.core.Global;
import rhila.lib.http.HttpGetFunction;
import rhila.lib.http.client.RhilaSocketFactory;
import rhila.scriptable.ScriptableGetFunction;

/**
 * 外部CRaC定義.
 * ここでLambdaでのCRaC定義を行います.
 */
public final class CRaCDefine {
	protected CRaCDefine() {}
	
	// このオブジェクト自体の定義..
	public static final CRaCDefine LOAD_CRAC = new CRaCDefine();
	
	// runtimeJava(よく使うJavaオブジェクト).
	protected static RuntimeJavaCRaCDefine RUNTIME_JAVA =
		RuntimeJavaCRaCDefine.LOAD_CRAC;
	
	// Global.
	protected static Global GLOBAL = Global.getInstance();
	
	// RhilaException.
	protected static RhilaException RHILA_EXCEPTION = new RhilaException();
	
	// Core用LoadFunctions.
	protected static final CoreGetFunction CORE_GET_FUNCTION =
		CoreGetFunction.LOAD_CRAC;
	
	
	// Scriptable用LoadFunctions.
	protected static final ScriptableGetFunction SCRIPTABLE_GET_FUNCTION =
		ScriptableGetFunction.LOAD_CRAC;
	
	// Http用LoadFunctions.
	protected static final HttpGetFunction HTTP_GET_FUNCTION =
		HttpGetFunction.LOAD_CRAC;
	
	// RhiLa用SocketFactory.
	protected static final RhilaSocketFactory RHILA_SOCKET_FACTORY =
		RhilaSocketFactory.LOAD_CRAC;
}
