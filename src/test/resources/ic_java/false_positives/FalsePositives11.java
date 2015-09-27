package ic_java.false_positives;

import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTML.Attribute;

public class FalsePositives11 {

	public static final String TOP = "top";
	public static final String TEXTTOP = "texttop";
	public static final String MIDDLE = "middle";
	public static final String ABSMIDDLE = "absmiddle";
	public static final String CENTER = "center";
	public static final String BOTTOM = "bottom";
	public static final String IMAGE_CACHE_PROPERTY = "imageCache";

	protected Element fElement;
	protected boolean bLoading; // set to true while the receiver is locked, to
								// indicate the reciever is loading the image.
								// This is used in imageUpdate.

	/*
	 * example from terpword. This example shows how the NullPointerAnalysis
	 * fails to show that $this is always non-null
	 */
	@SuppressWarnings("unused")
	public void initialize(Element elem) {
		synchronized (this) {
			bLoading = true;
		}
		int width = 0;
		int height = 0;
		boolean customWidth = false;
		boolean customHeight = false;
		try {
			fElement = elem;
			// request image from document's cache
			AttributeSet attr = elem.getAttributes();

			// get height & width from params or image or defaults
			height = getIntAttr(HTML.Attribute.HEIGHT, -1);
			customHeight = (height > 0);

			width = getIntAttr(HTML.Attribute.WIDTH, -1);
			customWidth = (width > 0);

		} finally {
			synchronized (this) {
				bLoading = false; // NullnessAnaysis fails to show that $this is
									// non-null
			}
		}
	}

	private int getIntAttr(Attribute height, int i) {
		// TODO Auto-generated method stub
		return 0;
	}

}
