package jp.angeworm.ensyuD.language;

public class ArrayType extends Type {
	public int min;
	public int max;
	
	public ArrayType(String type, int min, int max) {
		super(type);
		this.min = min;
		this.max = max;
	}

	@Override
	public String toString() {
		return "ArrayType " + super.type + "[min=" + min + ", max=" + max + "]";
	}

}
