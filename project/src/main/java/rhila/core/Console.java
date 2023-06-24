package rhila.core;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhila.scriptable.AbstractRhinoFunction;

// [js]ログ出力オブジェクト.
public class Console {
    // lambda snapStart CRaC用.
    protected static final Console LOAD_CRAC = new Console();
    
    // ログ出力.
    public static final void log(String msg) {
    	com.amazonaws.services.lambda.runtime.Context ctx =
    		GlobalFactory.getGlobal().getLambdaContext();
    	if(ctx == null) {
    		System.out.println(msg);
    	} else {
    		ctx.getLogger().log(msg);
    	}
    }
    
    //[js]ログ出力用.
	public static final class Log extends AbstractRhinoFunction {
	    // lambda snapStart CRaC用.
		protected static final Log LOAD_CRAC = new Log();
		// コンストラクタ.
		protected Log() {}
		@Override
		public String getName() {
			return "log";
		}

		@Override
		public Object function(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if(args == null || args.length == 0) {
				return null;
			}
			Object o = args[0];
			if(o == null) {
				log("null");
			} else if(o instanceof Undefined) {
				log("undefined");
			} else {
				log(args[0].toString()); 
			}
			return Undefined.instance;
		}
	}


}
