
public class Number implements Term {
	private int num;
	
	public Number(int number) {
		num = number;
	}
	
	@Override
	public int getValue() {
		return num;
	}

}
