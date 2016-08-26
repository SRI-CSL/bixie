package bixie.util;

public class TagValueRecord {

	private String tag;
	private String value;

	public TagValueRecord() {
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "TagValueRecord [tag=" + tag + ", value=" + value + "]";
	}

}
