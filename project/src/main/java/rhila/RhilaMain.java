package rhila;

import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import rhila.core.Global;

/**
 * rhilaMain.
 * AWS Lambda の実行ハンドラー.
 */
public class RhilaMain implements
	RequestHandler<Map<String,Object>, Map<String,Object>> {
	
	public RhilaMain() {
		// Lambda snapStart用 CRaC呼び出し.
		Global.getInstance();
	}

	// railaMain実行.
	public Map<String,Object> handleRequest(
		Map<String, Object> event, Context context) {
		
		// Global変数に対してLambda用のContextをセット.
		Global.getInstance().setLambdaContext(context);
		
		// event: request.
		
		
		
		// 戻り値: response.
		return null;
	}
}
