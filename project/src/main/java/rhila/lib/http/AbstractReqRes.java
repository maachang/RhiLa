package rhila.lib.http;

import rhila.RhilaException;
import rhila.lib.Base64;
import rhila.lib.JsValidate;
import rhila.lib.Json;
import rhila.lib.http.HttpHeader.HttpHeaderScriptable;
import rhila.scriptable.AbstractRhinoFunction;
import rhila.scriptable.BinaryScriptable;
import rhila.scriptable.BinaryScriptable.BinaryScriptableObject;

/**
 * HttpRequest/HttpResponse共通部分の処理.
 */
@SuppressWarnings("unchecked")
abstract class AbstractReqRes<T> extends AbstractRhinoFunction {
	
	// HTTPバージョン.
	protected static final String HTTP_VERSION = "HTTP/";
	
	// HttpVersion.
	protected String httpVersion = "1.1";
	
	// HttpHeader.
	protected HttpHeaderScriptable header = null;
	
	// body.
	protected BinaryScriptable body = null;
	
	// staticFlag.
	protected boolean staticFlag = false;
	
	// HttpHeaderを取得.
	public HttpHeader getHeader() {
		if(header == null) {
			header = HttpHeaderScriptable.newInstance(
				new HttpHeader());
		}
		return header.getSrc();
	}
	
	// HttpVersionを取得.
	public String getHttpVersion() {
		return httpVersion;
	}
	
	// HttpVersionを設定.
	public T setHttpVersion(String version) {
		if(version == null) {
			version = "1.1";
		}
		this.httpVersion = version;
		return (T)this;
	}
	
	// bodyを取得.
	public BinaryScriptable getBody() {
		return body;
	}
	
	// body内容を文字列で取得.
	public String getBodyToString() {
		if(body == null) {
			return null;
		}
		return body.toString();
	}
	
	// body内容をJSON形式で取得.
	public Object getBodyToJSON() {
		if(body == null) {
			return null;
		}
		return Json.decode(body.toString());
	}
	
	// body設定内容をクリア.
	public T clearBody() {
		// binaryをクリア.
		this.body = null;
		// content-lengthをクリア.
		this.header.getSrc().removeHeader("content-length");
		// content-typeをクリア.
		this.header.getSrc().removeHeader("content-type");
		return (T)this;
	}
	
	// bodyをセット.
	public T setBody(String body) {
		return setBody(body, null);
	}
	
	// bodyをセット.
	public T setBody(String body, String mimeType) {
		JsValidate.noArgsToError(body);
		// binaryをセット.
		this.body = BinaryScriptableObject.newInstance(body);
		// content-lengthをセット.
		this.header.getSrc().setContentLength(this.body.size());
		// mimeTypeが設定されている場合.
		if(mimeType != null) {
			getHeader().setContentType(mimeType);
		}
		return (T)this;
	}
	
	// bodyをセット.
	public T setBody(byte[] body) {
		return setBody(body, null);
	}
	
	// bodyをセット.
	public T setBody(byte[] body, String mimeType) {
		JsValidate.noArgsToError(body);
		// binaryをセット.
		this.body = BinaryScriptableObject.newInstance(body);
		// content-lengthをセット.
		this.header.getSrc().setContentLength(this.body.size());
		// mimeTypeが設定されている場合.
		if(mimeType != null) {
			getHeader().setContentType(mimeType);
		}
		return (T)this;
	}
	
	// json内容を設定.
	public T setBodyToJSON(Object json) {
		JsValidate.noArgsToError(body);
		setBody(Json.encode(json), MimeType.JSON);
		return (T)this;
	}
	
	// base64形式のbodyをセット.
	public T setBodyToBase64(String body) {
		return setBodyToBase64(body, null);
	}
	
	// base64形式のbodyをセット.
	public T setBodyToBase64(String body, String mimeType) {
		JsValidate.noArgsToError(body);
		// binaryをセット.
		this.body = BinaryScriptableObject.newInstance(
			Base64.decode(body));
		// content-lengthをセット.
		this.header.getSrc().setContentLength(this.body.size());
		// mimeTypeが設定されている場合.
		if(mimeType != null) {
			this.header.getSrc().setContentType(mimeType);
		}
		return (T)this;
	}
		
	// Body: plainセット.
	protected static final int TYPE_PLAIN = 0;
	// Body: JSONセット.
	protected static final int TYPE_JSON = 1;
	// Body: Base64セット.
	protected static final int TYPE_BASE64 = 2;
	
	// bodyをargsパラメータでセット.
	protected static final void setBodyByArgs(
		int type, AbstractReqRes<?> req, Object[] args) {
		JsValidate.noArgsToError(args);
		try {
			Object body = null;
			String mime = null;
			if(args.length >= 1) {
				body = args[0];
			}
			if(args.length >= 2) {
				if(args[1] instanceof String) {
					mime = (String)args[1];
				}
			}
			if(body instanceof String) {
				if(type == TYPE_BASE64) {
					req.setBodyToBase64((String)body, mime);
				} else {
					req.setBody((String)body, mime);
				}
			} else if(body instanceof byte[] ||
				body instanceof BinaryScriptable) {
				if(body instanceof BinaryScriptable ) {
					body = ((BinaryScriptable)body).getRaw();
				}
				if(type == TYPE_BASE64) {
					req.setBodyToBase64(
						new String((byte[])body, "UTF8"), mime);
				} else {
					req.setBody((String)body, mime);
				}
			} else if(type == TYPE_JSON) {
				req.setBodyToJSON(body);
			} else {
				throw new RhilaException(
					"Body Cannot be set due to condition: " +
						body.getClass().getName());
			}
		} catch(RhilaException re) {
			throw re;
		} catch(Exception e) {
			throw new RhilaException(e);
		}
	}
	
	// 指定Keyのヘッダが未設定の場合は設定する.
	protected T setDefaultHeader(String key, String value) {
		if(getHeader().getHeader(key) == null) {
			getHeader().setHeader(key, value);
		}
		return (T)this;
	}
}
