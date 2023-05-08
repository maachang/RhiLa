package rhila.lib.http;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhila.RhilaException;
import rhila.lib.ArrayMap;
import rhila.lib.NumberUtil;
import rhila.scriptable.AbstractRhinoFunction;
import rhila.scriptable.AbstractRhinoFunctionInstance;

// HTTPステータス.
public class HttpStatus {
    // lambda snapStart CRaC用.
    protected static final HttpStatus LOAD_CRAC = new HttpStatus();
	// instance可能なScriptable.
	private static final ArrayMap<String, Scriptable> instanceList =
		new ArrayMap<String, Scriptable>();
	// HTTPステータスメッセージ.
	private static final ArrayMap<Integer, String> STATUS;
	
	// HttpStatusScriptableメソッド.
	private static final String[] FUNCTION_NAMES = new String[] {
		"getHttpMessage"
		,"getMessage"
		,"getStatus"
		,"reset"
		,"setStatus"
	};
	
	// HttpStatusのMapping情報を取得.
	static {
		ArrayMap<Integer, String> map =
			new ArrayMap<Integer, String>(
			100, "Continue"
			,101, "Switching Protocols"
			,200, "Ok"
			,201, "Created"
			,202, "Accepted"
			,203, "Non-Authoritative Information"
			,204, "No Content"
			,205, "Reset Content"
			,206, "Partial Content"
			,300, "Multiple Choices"
			,301, "Moved Permanently"
			,302, "Moved Temporarily"
			,303, "See Other"
			,304, "Not Modified"
			,305, "Use Proxy"
			,307, "Temporary Redirect"
			,308, "Permanent Redirect"
			,400, "Bad Request"
			,401, "Authorization Required"
			,402, "Payment Required"
			,403, "Forbidden"
			,404, "Not Found"
			,405, "Method Not Allowed"
			,406, "Not Acceptable"
			,407, "Proxy Authentication Required"
			,408, "Request Time-out"
			,409, "Conflict"
			,410, "Gone"
			,411, "Length Required"
			,412, "Precondition Failed"
			,413, "Request Entity Too Large"
			,414, "Request-URI Too Large"
			,415, "Unsupported Media Type"
			,416, "Requested range not satisfiable"
			,417, "Expectation Faile"
			,500, "Internal Server Error"
			,501, "Not Implemented"
			,502, "Bad Gateway"
			,503, "Service Unavailable"
			,504, "Gateway Time-out"
			,505, "HTTP Version not supported"
		);
		STATUS = map;
		final int len = FUNCTION_NAMES.length;
		for(int i = 0; i < len; i ++) {
			instanceList.put(FUNCTION_NAMES[i],
				new HttpStatusFunctions(i));
		}
	}
	
	// HTTPステータスコードからメッセージを取得.
	public static final String getStatusMessage(Object status) {
		return STATUS.get(NumberUtil.parseInt(status));
	}
	
	// objectインスタンスリスト.
	private final ArrayMap<String, Object> objInsList =
		new ArrayMap<String, Object>();
	
	private int status;
	private String message;
	
	// コンストラクタ.
	public HttpStatus() {
		this.status = 200;
		this.message = "OK";
	}
	
	// コンストラクタ.
	public HttpStatus(int status) {
		if(status == 200) {
			this.status = 200;
			this.message = "OK";
		} else {
			setStatus(status);
		}
	}
	
	// コンストラクタ.
	public HttpStatus(int status, String message) {
		setStatus(status, message);
	}
	
	// ステータスリセット.
	public void reset() {
		this.status = 200;
		this.message = "OK";
	}
	
	// ステータス設定.
	public void setStatus(int status) {
		setStatus(status, null);
	}
	
	// ステータス＋メッセージ設定.
	public void setStatus(int status, String message) {
		if(message == null) {
			message = getStatusMessage(status);
			if(message == null) {
				this.status = 200;
				this.message = "OK";
			} else {
				this.status = status;
				this.message = message;
			}
		} else {
			this.status = status;
			this.message = message;
		}
	}
	
	// ステータスを取得.
	public int getStatus() {
		return status;
	}
	
	// ステータスメッセージを取得.
	public String getMessage() {
		return message;
	}
		
	// [js]HttpStatusFunctions.
	private static final class HttpStatusFunctions
		extends AbstractRhinoFunctionInstance {
	    // lambda snapStart CRaC用.
	    @SuppressWarnings("unused")
		protected static final HttpStatusFunctions LOAD_CRAC =
			new HttpStatusFunctions();
		
		private int type;
		private String typeString;
		protected HttpStatus status;
		
		// コンストラクタ.
		private HttpStatusFunctions() {}
		
		// コンストラクタ.
		private HttpStatusFunctions(int type) {
			this.type = type;
			this.typeString = FUNCTION_NAMES[type];
		}
		
		// 新しいインスタンスを生成.
		public final Scriptable getInstance(Object... args) {
			HttpStatusFunctions ret = new HttpStatusFunctions(type);
			ret.status = (HttpStatus)args[0];
			return ret;
		}
		
		@Override
		public String getName() {
			return typeString;
		}
		
		// メソッド実行.
		public Object function(
			Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			switch(type) {
			case 0: // getHttpMessage.
				if(args == null || args.length == 0) {
					throw new RhilaException("one parameter is required");
				}
				return getStatusMessage(args[0]);
			case 1: // getMessage.
				return status.getMessage();
			case 2: // getStatus.
				return status.getStatus();
			case 3: // reset.
				status.reset();
				return Undefined.instance;
			case 4: // setStatus.
				int len = args == null ? 0: args.length;
				if(len == 0) {
					throw new RhilaException("one parameter is required");
				} else if(len == 1) {
					status.setStatus(
						NumberUtil.parseInt(args[0]));
				} else {
					status.setStatus(
						NumberUtil.parseInt(args[0]), String.valueOf(args[1]));
				}
				return Undefined.instance;
			}
			// プログラムの不具合以外にここに来ることは無い.
			throw new RhilaException(
				"An unspecified error (type: " + type + ") occurred");
		}
	}
	
	// [js]HttpStatus.
	public static final class HttpStatusScriptable extends AbstractRhinoFunction {
	    // lambda snapStart CRaC用.
	    protected static final HttpStatusScriptable LOAD_CRAC = new HttpStatusScriptable();
	    
		protected HttpStatusScriptable() {}
		HttpStatus src = null;
		private boolean staticFlag = true;
		
		@Override
		public String getClassName() {
			return "HttpStatusScriptable";
		}
		
		// 元のオブジェクトを取得.
		public HttpStatus getSrc() {
			return src;
		}
		
		// new HttpStatusScriptable();
		@Override
		public Scriptable newInstance(Context arg0, Scriptable arg1, Object[] arg2) {
			// 新しいScriptableオブジェクトを生成.
			HttpStatusScriptable ret = new HttpStatusScriptable();
			// 引数長を取得.
			final int len = arg2 == null || arg2.length == 0 ? 0 : arg2.length;
			if(len == 0) {
				ret.src = new HttpStatus();
			} else if(len == 1) {
				ret.src = new HttpStatus(NumberUtil.parseInt(
					arg2[0]));
			} else {
				ret.src = new HttpStatus(NumberUtil.parseInt(
					arg2[0]), String.valueOf(arg2[1]));
			}
			ret.staticFlag = false;
			return ret;
		}
		
		// new HttpStatusScriptable();
		public static final HttpStatusScriptable newInstance(HttpStatus src) {
			// 新しいScriptableオブジェクトを生成.
			HttpStatusScriptable ret = new HttpStatusScriptable();
			ret.src = src;
			ret.staticFlag = false;
			return ret;
		}


		@Override
		public String getName() {
			return "[HttpStatus]";
		}
		
		@Override
		public String toString() {
			return getName();
		}
		
		@Override
		public Object get(String arg0, Scriptable arg1) {
			return getFunction(arg0);
		}
		
		// function取得.
		private final Object getFunction(String name) {
			// staticの場合.
			if(staticFlag) {
				if(!"getHttpMessage".equals(name)) {
					return null;
				}
			}
			// オブジェクト管理の生成Functionを取得.
			Object ret = src.objInsList.get(name);
			// 存在しない場合.
			if(ret == null) {
				// static管理のオブジェクトを取得.
				ret = instanceList.get(name);
				// 存在する場合.
				if(ret != null) {
					// オブジェクト管理の生成Functionとして管理.
					ret = ((AbstractRhinoFunctionInstance)ret)
						.getInstance(src);
					src.objInsList.put(name, ret);
				}
			}
			return ret;
		}
	}
}
