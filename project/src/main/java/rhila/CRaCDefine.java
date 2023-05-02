package rhila;

import org.mozilla.javascript.Context;

/**
 * 外部CRaC定義.
 * ここでLambdaでのCRaC定義を行います.
 * 
 */
public final class CRaCDefine {
	protected CRaCDefine() {}
		
	// rhinoのContext.
	@SuppressWarnings("deprecation")
	protected static final Context CONTEXT = new Context();

}
