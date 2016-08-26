package bixie.util;

public class Loc {
	private String file;
	private int lnum;
	private int cnum;
	private int _enum;

	public Loc() {
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public int getLnum() {
		return lnum;
	}

	public void setLnum(int lnum) {
		this.lnum = lnum;
	}

	public int getCnum() {
		return cnum;
	}

	public void setCnum(int cnum) {
		this.cnum = cnum;
	}

	public int getEnum() {
		return _enum;
	}

	public void setEnum(int _enum) {
		this._enum = _enum;
	}

	@Override
	public String toString() {
		return "Loc [file=" + file + ", lnum=" + lnum + ", cnum=" + cnum + ", _enum=" + _enum + "]";
	}

}
