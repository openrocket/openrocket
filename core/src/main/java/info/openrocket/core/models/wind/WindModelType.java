package info.openrocket.core.models.wind;

public enum WindModelType {
	PINK_NOISE("PinkNoise"),
	MULTI_LEVEL("MultiLevel");

	private final String stringValue;

	WindModelType(String stringValue) {
		this.stringValue = stringValue;
	}

	public String toStringValue() {
		return stringValue;
	}

	public static WindModelType fromString(String stringValue) {
		for (WindModelType type : WindModelType.values()) {
			if (type.stringValue.equalsIgnoreCase(stringValue)) {
				return type;
			}
		}
		throw new IllegalArgumentException("No enum constant " + WindModelType.class.getCanonicalName() + " for string value: " + stringValue);
	}
}
