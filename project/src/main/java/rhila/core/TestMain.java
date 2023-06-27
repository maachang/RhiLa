package rhila.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Version;

import rhila.CRaCDefine;

/**
 * コマンド実行テスト用.
 */
public class TestMain {
	
	// Lambda snapStart用 CRaC呼び出し.
	static {
		CRaCDefine.LOAD_CRAC.getClass();
	}
	
	public static final void main(String[] args) throws Exception {
		exec(args);
	}
	
	public static final void exec(String args[]) throws Exception {
		final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		final Global global = GlobalFactory.getGlobal();
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
			GlobalFactory.releaseGlobal();
		}
	}

}
