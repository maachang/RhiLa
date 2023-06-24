package rhila.lib;

import org.mozilla.javascript.Undefined;

import rhila.RhilaException;
import rhila.core.GlobalFactory;
import rhila.core.ProcessEnv;
import rhila.core.RunScript;
import rhila.lib.http.HttpRequest;
import rhila.lib.http.HttpResponse;
import rhila.lib.http.client.HttpClient;
import rhila.scriptable.MapScriptable;

/**
 * Githubコンテンツを読み込む.
 */
public class GithubContents {
	protected GithubContents() {};
	
    // lambda snapStart CRaC用.
    protected static final GithubContents LOAD_CRAC = new GithubContents();
	
	// githubコンテンツHost名.
	private static final String GITHUB_CONTENT_HOST = "https://raw.githubusercontent.com/";
	
	// 環境変数.
	//   - GIT_ORGANIZATION = 必須
	//   - GIT_REPO = 必須
	//   - GIT_BRANCH = 必須
	//   - GIT_TOKEN = privateのRepogitoryの場合必須.
	
	// URLを取得.
	protected static final String getURL(String currentPath, String path) {
	    // パスの先頭に / がある場合は除去する.
	    if((path = path.trim()).startsWith("/")) {
	        path = path.substring(1);
	    }
	    // カレントパスと統合.
	    if(!ObjectUtil.isNull(currentPath)) {
		    path = currentPath + "/" + path;
	    }
	    // env取得.
		ProcessEnv env = GlobalFactory.getGlobal().getEnv();
	    // URL返却.
	    return GITHUB_CONTENT_HOST + HttpClient.encodeURIToPath(
	    	new StringBuilder(env.requiredEnv("GIT_ORGANIZATION")).append("/")
	    		.append(env.requiredEnv("GIT_REPO")).append("/")
	    		.append(env.requiredEnv("GIT_BRANCH")).append("/")
	    		.append(path)
	    		.toString()
	    );
	}
	
	// githubコンテンツを取得.
	public static final HttpResponse read(String path) {
		return read(null, path);
	}
	
	// githubコンテンツを取得.
	public static final HttpResponse read(String currentPath, String path) {
		if(ObjectUtil.isNull(currentPath) && ObjectUtil.isNull(path)) {
			throw new RhilaException("path does not exist.");
		}
		// envからgithub定義を取得.
		final String url = getURL(currentPath, path);
		final String token = GlobalFactory.getGlobal().getEnv()
			.getEnv("GIT_TOKEN");
		// tokenありのアクセス.
		if(token != null) {
			HttpRequest req = new HttpRequest();
			req.getHeader().setHeader("authorization", "token " + token);
			return HttpClient.requestGet(url, req);
		}
		// tokenなしのアクセス.
		return HttpClient.requestGet(url);
	}
	
	// public(htmlなどが存在するPath)の読み込み.
	public static final HttpResponse readPublic(String path) {
		return read(GlobalFactory.getGlobal().getEnv()
			.requiredEnv("GIT_PUBLIC_PATH"), path);
	}
	
	// library(jsライブラリが存在するPath)の読み込み.
	public static final HttpResponse readLibrary(String path) {
		return read(GlobalFactory.getGlobal().getEnv()
			.requiredEnv("GIT_LIBRARY_PATH"), path);
	}
	
	// 処理結果のステータスコードチェック.
	private static final void checkStatus(HttpResponse res, String path) {
		// 取得に失敗の場合.
		if(res.getStatusCode() > 300) {
			if(res.getStatusCode() == 404) {
				throw new RhilaException(
					"Target path \"" + path + "\" does not exist.");
			}
			throw new RhilaException(
				"Failed to Require the target path \"" + path + "\".");
		}
	}
	
    // requireキャッシュ.
    private static final ArrayMap<String, Object> REQUIRE_CACHE =
    	new ArrayMap<String, Object>();
	
    // require.
    // この処理は基本的にjs内で呼び出される.
	public static final Object require(String src) {
		// jsonやjsの拡張子が存在しない場合.
		if(!src.endsWith(".json") && !src.endsWith(".js")) {
			src = src + ".js";
		}
		Object o;
		// キャッシュに存在する場合.
		if((o = REQUIRE_CACHE.get(src)) != null) {
			return o;
		}
		// 取得処理.
		HttpResponse res = GithubContents.readLibrary(src);
		// ステータスチェック.
		checkStatus(res, src);
		// json返却.
		if(src.endsWith(".json")) {
			return Json.decode(src);
		}
		// ライブラリ読み込み.
		o = RunScript.loadLibrary(res.getBodyToString(), src);
		// exportsの取得成功(普通は必ず取得できる)
		if(o instanceof MapScriptable) {
			REQUIRE_CACHE.put(src, o);
			return o;
		}
		// exportsの取得に失敗.
		// (exports=null or exports=undefined).
		return Undefined.instance;
	}
}
