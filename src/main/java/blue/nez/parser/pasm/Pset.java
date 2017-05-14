package blue.nez.parser.pasm;

public class Pset extends PAsmInst {
	public final int[] bits;

	public Pset(int[] bits, PAsmInst next) {
		super(next);
		this.bits = bits;
	}

	@Override
	public PAsmInst exec(PAsmContext px) throws PAsmTerminationException {
		return (bitis(this.bits, nextbyte(px))) ? this.next : raiseFail(px);
	}

}