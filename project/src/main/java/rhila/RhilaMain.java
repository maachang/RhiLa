package rhila;

import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import rhila.core.Global;
import rhila.core.GlobalFactory;

/**
 * rhilaMain.
 * AWS Lambda の実行ハンドラー.
 */
public class RhilaMain implements
	RequestHandler<Map<String,Object>, Map<String,Object>> {
	
	// Lambda snapStart用 CRaC呼び出し.
	static {
		CRaCDefine.LOAD_CRAC.getClass();
	}
	
	public RhilaMain() {
	}

	// railaMain実行.
	public Map<String,Object> handleRequest(
		Map<String, Object> event, Context context) {
		// global変数を生成.
		Global global = GlobalFactory.getGlobal();
		try {
			// Global変数に対してLambda用のContextをセット.
			global.setLambdaContext(context);
			
			// リクエスト実行.
			return request(event, global);
		} finally {
			// globalオブジェクトをリリース.
			GlobalFactory.releaseGlobal();
		}
	}
	
	// リクエスト実行.
	public Map<String, Object> request(
		Map<String, Object> event, Global global) {
		// event: request.
		
		
		
		// 戻り値: response.
		return null;
	}
}
