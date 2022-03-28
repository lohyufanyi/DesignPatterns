import java.util.ArrayList;
import java.util.List;

public class CensusOffice implements Observable {
	private List<Observer> observers = new ArrayList<>();
	private int number;
	private City city;
	
	public CensusOffice(int num) {
		if (num <= 0 ) {
			throw new IllegalArgumentException(String.format("office number must greater that 0 "
					+ "[%s]", num));
		}
		else {
			number = num;
		}
	}
	
	public boolean addObserver(Observer o) {
		if (!observers.contains(o)) {
			observers.add(o);
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean removeObserver(Observer o) {
		return observers.remove(o);
	}
	
	public boolean hasObservers() {
		return observers.size() > 0;
	}
	
	public int getNumber() {
		return number;
	}
	
	public City getReported() {
		return city;
	}
	
	public void report(City latest) {
		if (latest == null) {
			throw new IllegalArgumentException("City cannot be null");
		}
		city = latest;
		var tempCopy = new ArrayList<>(observers);
		for (var o: tempCopy) {
			o.update(this);
		}
	}
	
}
