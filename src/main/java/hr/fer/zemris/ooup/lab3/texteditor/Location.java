package hr.fer.zemris.ooup.lab3.texteditor;

public class Location {
	
	int x;
	int y;
	
	public Location(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public void setLocation(Location l) {
		this.x = l.x;
		this.y = l.y;
	}
	
	public Location copy() {
		return new Location(this.x, this.y);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Location))
			return false;
		Location other = (Location) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}
	
	
}
