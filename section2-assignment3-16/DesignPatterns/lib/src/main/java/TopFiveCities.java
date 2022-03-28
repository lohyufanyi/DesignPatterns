
import java.util.ArrayList;
import java.util.List;

public class TopFiveCities implements Observer{
	private List<City> city = new ArrayList<City>(20);
	private CensusOffice off;
	private City c;
	
	public void update(Observable o) {
		if (o instanceof CensusOffice) {
			off = ((CensusOffice) o);
			c = off.getReported();
			city.add(c);
		}
	}
	
	public List<City> sort() {
		List<City> fSort = city;
		for (int i = 0; i < city.size(); i ++ ) {
			for (int j = 0; j < city.size()-i-1; j ++) {

				if(city.get(j).getPopulation() < city.get(j + 1).getPopulation()) {
					City temp = fSort.get(j);
					fSort.set(j, fSort.get(j+1));

					fSort.set(j+1, temp);
				}
			}
		}

		if(fSort.size() > 5) {
			return fSort.subList(0, 5);
		}
		return fSort;
	}
	
	public List<City> getTopFive() {
		return sort();
	}

	public static void main(String [] args) {
		
	}
}
