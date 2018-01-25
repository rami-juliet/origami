package blue.origami.transpiler;

import blue.origami.transpiler.type.Ty;

public interface NameHint {

	public Ty getType();

	public boolean equalsName(String key);

	// a, a', a'', a2, a12, a$ => a
	public static String keyName(String name) {
		int loc = name.length() - 1;
		for (; loc > 0; loc--) {
			char c = name.charAt(loc);
			if (c != '\'' && c != '$' && !Character.isDigit(c) && c != '_') {
				break;
			}
		}
		return name.substring(0, loc + 1);
	}

	public static String flatName(String name) {
		return safeName(name.replace("_", "").toLowerCase());
	}

	static boolean isFlatName(String name) {
		return flatName(name).equals(name);
	}

	public static NameHint addNameHint(Env env, AST ns, Ty ty) {
		String name = keyName(ns.getString());
		NameHint hint = env.get(name, NameHint.class);
		if (hint == null) {
			hint = new NameDecl(name, ty);
			env.getTranspiler().add(name, hint);
		} else {
			if (!hint.getType().eq(ty)) {
				env.reportWarning(ns, TFmt.already_defined_YY1_as_YY2, name, hint.getType());
			}
		}
		if (!isFlatName(name)) {
			String flatName = NameHint.flatName(name);
			// System.out.println("defining flat: " + flatName);
			NameHint hint2 = env.get(flatName, NameHint.class);
			if (hint2 == null) {
				env.getTranspiler().add(flatName, new NameDecl(name, ty));
			}
		}
		return hint;
	}

	public static Ty findNameHint(Env env, String name) {
		NameHint hint = matchSubNames(env, keyName(name));
		if (hint == null && !isFlatName(name)) {
			// System.out.println("trying flat matching " + flatName(name));
			hint = matchSubNames(env, flatName(name));
		}
		if (hint == null) {
			if (name.endsWith("s") || name.endsWith("*")) {
				Ty ty = findNameHint(env, name.substring(0, name.length() - 1));
				if (ty != null) {
					return Ty.tList(ty);
				}
			}
			return null;
		}
		return hint.getType();
	}

	static NameHint matchSubNames(Env env, String name) {
		NameHint hint = env.get(name, NameHint.class);
		if (hint == null && name.length() > 2) {
			return matchSubNames(env, name.substring(1));
		}
		return hint;
	}

	public static String safeName(String name) {
		return keyName(name.replace('?', 'Q'));
	}

	public static boolean isOneLetterName(String name) {
		String n = keyName(name);
		return n.length() == 1 && Character.isLowerCase(n.charAt(0));
	}

	public static boolean isMutable(String name) {
		return name.endsWith("$");
	}

}

class NameDecl implements NameHint {
	String name;
	Ty ty;

	NameDecl(String name, Ty ty) {
		this.name = name;
		this.ty = ty;
	}

	@Override
	public boolean equalsName(String name) {
		return this.name.equals(name);
	}

	@Override
	public Ty getType() {
		return this.ty;
	}

}
