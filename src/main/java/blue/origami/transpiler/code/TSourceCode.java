package blue.origami.transpiler.code;

import blue.origami.transpiler.TCodeSection;
import blue.origami.transpiler.TEnv;
import blue.origami.transpiler.Template;
import blue.origami.transpiler.TType;

public class TSourceCode extends MultiTypedCode {

	public TSourceCode(TCode... args) {
		super(args.length == 0 ? TType.tVoid : args[args.length - 1].getType(), Template.Null, args);
	}

	@Override
	public void emitCode(TEnv env, TCodeSection sec) {
		for (TCode a : this.args) {
			a.emitCode(env, sec);
		}
	}
}