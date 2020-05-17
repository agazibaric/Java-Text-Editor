package hr.fer.zemris.ooup.lab3.texteditor;

public class LocationRange {
	
	Location l1;
	Location l2;
	
	
	public LocationRange(Location l1, Location l2) {
		this.l1 = l1;
		this.l2 = l2;
	}
	
	public LocationRange copy() {
		return new LocationRange(l1.copy(), l2.copy());
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((l1 == null) ? 0 : l1.hashCode());
		result = prime * result + ((l2 == null) ? 0 : l2.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof LocationRange))
			return false;
		LocationRange other = (LocationRange) obj;
		if (l1 == null) {
			if (other.l1 != null)
				return false;
		} else if (!l1.equals(other.l1))
			return false;
		if (l2 == null) {
			if (other.l2 != null)
				return false;
		} else if (!l2.equals(other.l2))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return l1.toString() + " " + l2.toString();
	}
	
	

}
