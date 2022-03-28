
public class Plus extends BinaryOperator{
	
	public Plus(Term left, Term right) {
		super(left, right);

	}
	
	@Override
	public int getValue() {
		return getLeft().getValue() + getRight().getValue();
	}

}
