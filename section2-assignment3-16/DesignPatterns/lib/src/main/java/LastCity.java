
public class LastCity implements Observer{
	private City city;
	private CensusOffice office;
	public void update(Observable o) {
		if (o instanceof CensusOffice) {		
			office =  (CensusOffice) o;
			city = office.getReported();
			
		}
	}
	
	public CensusOffice getLastOffice() {
		return office;
	}
	
	public City getLastCity() {
		return city;
	}
}
