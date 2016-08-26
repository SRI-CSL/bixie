package bixie.util;

import java.util.List;

public class JSONTraceItem {

	private int level;
	private String filename;
	private int line_number;
	private String description;
	private List<TagValueRecord> node_tags;

	public JSONTraceItem() {
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public int getLine_number() {
		return line_number;
	}

	public void setLine_number(int line_number) {
		this.line_number = line_number;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<TagValueRecord> getNode_tags() {
		return node_tags;
	}

	public void setNode_tags(List<TagValueRecord> node_tags) {
		this.node_tags = node_tags;
	}

	@Override
	public String toString() {
		return "JsonTraceItem [level=" + level + ", filename=" + filename + ", line_number=" + line_number
				+ ", description=" + description + ", node_tags=" + node_tags + "]";
	}

}
