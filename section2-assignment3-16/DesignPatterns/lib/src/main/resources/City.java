public final class City {
	private final String name;
	private final String state;
	private final int    population;

	public City(String name, String state, int population) {
		this.name       = name;
		this.state      = state;
		this.population = population;
	}
	public String getName() {
		return name;
	}
	public String getState() {
		return state;
	}
	public int getPopulation() {
		return population;
	}
	@Override
	public String toString() {
		return "City [name=" + name + ", state=" + state + ", population=" + population + "]";
	}
}
