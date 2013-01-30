package jp.angeworm.ensyuD.language;

public class ConstantValue extends Value{
	public ConstantValue(String value, Type type) {
		super(value, type);
	}
	
	public ConstantValue(String value, String type) {
		super(value, type);
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
