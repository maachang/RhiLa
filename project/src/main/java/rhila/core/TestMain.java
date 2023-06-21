package rhila.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Version;

/**
 * コマンド実行テスト用.
 */
public class TestMain {
	
	public static final void main(String[] args) throws Exception {
		exec(args);
		
		/*
		// javaのHttpClientを試す(githubリポジトリのsource)
		// !!githubusercontentのアクセス
		// time: 66 msec(10回実行平均)
		// time: 300 msec(1回実行)
		// length: 1656(gzip抜き)
		HttpClient httpClient = HttpClient.newHttpClient();
        // リクエストを作成
        HttpRequest request = HttpRequest.newBuilder(
        	URI.create("https://raw.githubusercontent.com/maachang/testLFU/main/public/index.html")).build();
        // 同期API呼び出し
        int loopLen = 1;
		long time = System.currentTimeMillis();
		HttpResponse<String> response = null;
		for(int i = 0; i < loopLen; i ++) {
	        response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		}
		System.out.println("time: " + ((System.currentTimeMillis() - time) / loopLen) + " msec");
        System.out.println("length: " + response.body().length()); // 受信したJSON文字列を確認
        */
		
		//System.out.println("sun.boot.library.path=" + System.getProperty("sun.boot.library.path"));
	}
	
	public static final void exec(String args[]) throws Exception {
		final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		final ProcessEnv processEnv = new ProcessEnv();
		final Global global = Global.getInstance();
		global.setEnv(processEnv);
		try {
			Object out;
			String cmd;
			System.out.println("rhino version: " + Version.VALUE);
			while(true) {
				System.out.print("js> ");
				cmd = in.readLine();
				if(cmd == null || (cmd = cmd.trim()).isEmpty()) {
					continue;
				} else if("exit".equals(cmd) || "exit()".equals(cmd)) {
					System.out.println("exit.");
					break;
				}
				try {
					out = RunScript.eval(global, cmd);
					if(out == null || out instanceof Undefined) {
						System.out.println("");
					} else {
						try {
							out = Context.toString(out);
						} catch(Exception ee) {}
						System.out.println(out);
					}
				} catch(Throwable e) {
					e.printStackTrace();
					Thread.sleep(5);
				}
			}
		} finally {
			try { in.close(); } catch(Throwable e) {}
			Context.exit();
		}
	}

}
