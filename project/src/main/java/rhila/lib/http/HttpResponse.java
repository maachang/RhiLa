package rhila.lib.http;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import rhila.RhilaConstants;
import rhila.RhilaException;
import rhila.lib.ArrayMap;
import rhila.lib.Base64;
import rhila.lib.DateUtil;
import rhila.lib.JsValidate;
import rhila.lib.http.HttpStatus.HttpStatusScriptable;
import rhila.scriptable.AbstractRhinoFunctionInstance;

/**
 * HttpResponse.
 */
public class HttpResponse extends AbstractReqRes<HttpResponse> {
    // lambda snapStart CRaC用.
    protected static final HttpResponse LOAD_CRAC = new HttpResponse();

	// instance可能なScriptable.
	private static final ArrayMap<String, Scriptable> instanceList;
	
	// メソッド名群(sort済み).
	private static final String[] FUNCTION_NAMES = new String[] {
	    "clearBody"
	    ,"clearRedirect"
	    ,"getBody"
	    ,"getBodyToJSON"
	    ,"getBodyToString"
	    ,"getHeader"
	    ,"getHttpVersion"
	    ,"getRedirect"
	    ,"getStatus"
	    ,"isRedirect"
	    ,"setBody"
	    ,"setBodyToBase64"
	    ,"setBodyToJSON"
	    ,"setHttpVersion"
	    ,"setRedirect"
	    ,"toString"
	};
	
	static {
		// 配列で直接追加.
		final int len = FUNCTION_NAMES.length * 2;
		Object[] list = new Object[len];
		for(int i = 0, j = 0; i < len; i += 2, j ++) {
			list[i] = FUNCTION_NAMES[j];
			list[i + 1] = new FunctionList(j);
		}
		instanceList = new ArrayMap<String, Scriptable>(list);
	}
	
	// objectインスタンスリスト.
	private final ArrayMap<String, Object> objInsList =
		new ArrayMap<String, Object>();
	
	// httpStatus.
	private HttpStatusScriptable status = null;
	
	// コンストラクタ.
	public HttpResponse() {
		staticFlag = false;
	}
	
	// コンストラクタ.
	protected HttpResponse(boolean staticFlag) {
		this.staticFlag = staticFlag;
	}
	
	// responseクリア.
	public void reset() {
		status = null;
		header = null;
		body = null;
	}
	
	// HttpStatusを取得.
	public HttpStatus getStatus() {
		if(status == null) {
			status = HttpStatusScriptable.newInstance(
				new HttpStatus());
		}
		return status.getSrc();
	}
	
	// redirect条件が設定されている場合.
	public boolean isRedirect() {
		if(header == null || status == null) {
			return false;
		}
		// リダイレクトステータスが設定されている場合.
		else if(HttpStatus.isRedirectStatus(status.src.getStatus())) {
			return true;
		}
		return false;
	}
	
	// redirectURLを取得.
	public String getRedirect() {
		return getHeader().getHeader("location");
	}
	
	// redirect条件をクリア.
	public HttpResponse clearRedirect() {
		boolean ret = isRedirect();
		if(ret) {
			return this;
		}
		getStatus().setStatus(200);
		getHeader().removeHeader("location");
		return this;
	}
	
	// redirect設定.
	public HttpResponse setRedirect(String url) {
		return setRedirect(301, url);
	}
	
	// redirect設定.
	public HttpResponse setRedirect(int status, String url) {
		if(!HttpStatus.isRedirectStatus(status)) {
			throw new RhilaException(
				"Not a redirect target Status: " + status);
		}
		getStatus().setStatus(status);
		getHeader().setHeader("location", url);
		return this;
	}

	@Override
	public String getName() {
		return "HttpResponse";
	}
	
	@Override
	public String toString() {
		if(staticFlag) {
			return "[HttpResponse]";
		}
		StringBuilder buf = new StringBuilder();
		getHttpHeaderToString(buf);
		return buf.toString();
	}
	
	// デフォルトヘッダをセット.
	private final HttpHeader setDefaultHeaders() {
		final HttpHeader header = getHeader();
		// [header]Dateがセットされていない場合.
		this.setDefaultHeader("date", DateUtil.toRfc822(false, new Date()));
		// [header]Serverがセットされていない場合.
		this.setDefaultHeader("server", RhilaConstants.SERVER_NAME);
		// [header]connectionがセットされていない場合.
		this.setDefaultHeader("connection", "close");
		
		return header;
	}
	
	// HTTPヘッダの文字変換.
	public void getHttpHeaderToString(StringBuilder out) {
		int state = 200;
		String message = "OK";
		if(status != null) {
			state = status.src.getStatus();
			message = status.src.getMessage();
		}
		// HTTP/2.0 200 OK
		out.append(HTTP_VERSION).append(httpVersion).append(" ")
			.append(state).append(" ").append(message).append("\r\n");
		HttpHeader header = setDefaultHeaders();
		
		// ヘッダ出力.
		header.toString(true, out);
	}
	
	// LambdaURLFunction用の処理.
	// Map<String. Object> 形式でResponse結果を作成します.
	// 
	// 返却条件(Map<String, Object>).
    //  statusCode: number
    //  statusMessage: string
    //  headers: map
    //  cookies: array
    //  isBase64Encoded: boolean
    //  body: string
	public Map<String, Object> toLambdaResponseMap() {
		// 戻り値.
		Map<String, Object> ret = new HashMap<String, Object>();
		// isBase64Encoded.
		boolean isBase64Encoded = false;
		// body.
		String bodyString = "";
		
		// デフォルトのHTTPヘッダを設定.
		HttpHeader header = setDefaultHeaders();
		// bodyが存在する場合.
		if(body != null) {
			// bodyがBinaryの場合は
			//  - isBase64Encoded true
			// で、BinaryをBase64変換する.
			// Lambdaの場合は、Content-Lengthの設定は不要.
			// 理由は
			//  - isBase64Encoded
			//  - body
			// でBinaryではなく文字列設定となるので、最終的に
			// LambdaがContent-Length設定するので、必要なし。
			//
			if(MimeType.getInstance().isBinary(header.getMimeType())) {
				bodyString = Base64.encode(body.getRaw());
				isBase64Encoded = true;
			} else {
				String charset = header.getCharset();
				if(charset == null) {
					bodyString = body.convertString();					
				} else {
					bodyString = body.convertString(charset);					
				}
			}
		}
		// Statusを設定.
		ret.put("statusCode", getStatus().getStatus());
		ret.put("statusMessage", getStatus().getMessage());
		
		// ヘッダ一覧を取得.
		Map<String, String> headerMap = header.toHeaderMap();
		// コンテンツ長はLambdaでは不要なので削除する.
		headerMap.remove("content-type");
		// Headerを設定.
		ret.put("headers", headerMap);
		
		// bodyがバイナリ返却の場合、Base64返却.
		ret.put("isBase64Encoded", isBase64Encoded);
		// body返却.
		ret.put("body", bodyString);
		
		// cookie返却が存在する場合.
		if(header.getCookieSize() > 0) {
			ret.put("cookies", header.toSetCookieArray());
		}
		return ret;
	}

	@Override
	public Object get(String arg0, Scriptable arg1) {
		// staticな場合.
		if(staticFlag) {
			return null;
		}
		// staticでない場合.
		Object ret = getFunction(arg0);
		if(ret != null) {
			return ret;
		}
		return null;
	}
	
	// function取得.
	private final Object getFunction(String name) {
		// オブジェクト管理の生成Functionを取得.
		Object ret = objInsList.get(name);
		// 存在しない場合.
		if(ret == null) {
			// static管理のオブジェクトを取得.
			ret = instanceList.get(name);
			// 存在する場合.
			if(ret != null) {
				// オブジェクト管理の生成Functionとして管理.
				ret = ((AbstractRhinoFunctionInstance)ret)
					.getInstance(this);
				objInsList.put(name, ret);
			}
		}
		return ret;
	}
	
	// new HttpResponse.
	public Scriptable newInstance(Context arg0, Scriptable arg1, Object[] arg2) {
		return new HttpResponse();
	}
	
	// functionリストを生成.
	private static final class FunctionList extends AbstractRhinoFunctionInstance {
	    // lambda snapStart CRaC用.
	    @SuppressWarnings("unused")
		protected static final FunctionList LOAD_CRAC = new FunctionList();
	    
		private HttpResponse src;
		private int type;
		private String typeString;
		
		// 新しいインスタンスを生成.
		public final Scriptable getInstance(Object... args) {
			FunctionList ret = new FunctionList(type);
			ret.src = (HttpResponse)args[0];
			return ret;
		}
		
		protected FunctionList() {}
		
		// コンストラクタ.
		protected FunctionList(int type) {
			this.type = type;
			this.typeString = FUNCTION_NAMES[type];
		}
		
		@Override
		public String getName() {
			return typeString;
		}
		
		// メソッド実行.
		public Object function(
			Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			switch(type) {
			case 0: //"clearBody"
				return src.clearBody();
			case 1: //"clearRedirect"
				return src.clearRedirect();
			case 2: //"getBody"
				return src.getBody();
			case 3: //"getBodyToJSON"
				return src.getBodyToJSON();
			case 4: //"getBodyToString"
				return src.getBodyToString();
			case 5: //"getHeader"
				return src.header;
			case 6: //"getHttpVersion"
				return src.getHttpVersion();
			case 7: //"getRedirect"
				return src.getRedirect();
			case 8: //"getStatus"
				return src.getStatus();
			case 9: //"isRedirect"
				return src.isRedirect();
			case 10: //"setBody"
				setBodyByArgs(TYPE_PLAIN, src, args);
				return src;
			case 11: //"setBodyToBase64"
				setBodyByArgs(TYPE_BASE64, src, args);
				return src;
			case 12: //"setBodyToJSON"
				setBodyByArgs(TYPE_JSON, src, args);
				return src;
			case 13: //"setHttpVersion"
				JsValidate.noArgsStringToError(0, args);
				src.setHttpVersion((String)args[0]);
				return src;
			case 14: //"setRedirect"
				JsValidate.noArgsToLengthToError(1, args);
				int len = args.length;
				if(len == 1) {
					JsValidate.noArgsStringToError(1, args);
					return src.setRedirect((String)args[0]);
				} else {
					JsValidate.noArgsNumberToError(1, args);
					JsValidate.noArgsStringToError(2, args);
					return src.setRedirect(
						((Number)args[0]).intValue(), (String)args[1]);
				}
			case 15: //"toString"
				return src.toString();
			}
			// プログラムの不具合以外にここに来ることは無い.
			throw new RhilaException(
				"An unspecified error (type: " + type + ") occurred");
		}
	}
}
