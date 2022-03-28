public interface Observable {
	boolean addObserver(Observer observer);
	boolean removeObserver(Observer observer);
	boolean hasObservers();
}
