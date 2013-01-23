package jp.angeworm.ensyuD.compiler;

public class Location {
	public static enum LocationType {
		HeapLocation,
		RegisterLocation,
		StackLocation,
	}
	public LocationType type;
	public String label;
	public int    id;
	
	public Location(String label) {
		this.type = Location.LocationType.HeapLocation;
		this.label = label;
	}
	public Location(Location.LocationType type, String label) {
		this.type = Location.LocationType.HeapLocation;
		this.label = label;
	}
	public Location(Location.LocationType type, int id) {
		this.type = type;
		this.id = id;
	}
	
	public String getLabel() {
		if( !this.type.equals(LocationType.HeapLocation) )
			throw new RuntimeException("this type " + type + " has not label");
		return this.label;
	}
	public int getId() {
		if( this.type.equals(LocationType.HeapLocation) )
			throw new RuntimeException("this type " + type + " has not id");
		return this.id;
	}
	public String toString() {
		switch(this.type) {
		case HeapLocation:
			return label;
		case RegisterLocation:
			return "GR" + id;
		case StackLocation:
			return "GR8%" + id;
		default:
			return "";
		}
	}
}
