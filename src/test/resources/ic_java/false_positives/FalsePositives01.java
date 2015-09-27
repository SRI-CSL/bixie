package ic_java.false_positives;


public class FalsePositives01 {

	private String[] sourceKeys;
	String[] sourceValues;

	String[] removeKeys;
	String[] removeValues;

	private int countSource;

	public void removeAttribute() {
		try {
			boolean hit = false;
			for (int countSource = 0; countSource < sourceKeys.length; countSource++) {
				hit = false;
				if (sourceKeys[countSource] == "name") {
					hit = true;
				}

				if (!hit) {
					// THIS IS THE IMPORTANT LINE!
					sourceKeys[countSource].toString();
				}
			}
		} catch (ClassCastException cce) {

		}
	}

	public void FalsePositive01() {
		boolean hit = false;
		int counter = 0;
		do {
			hit = false;
			if (countSource > counter) {
				counter++;
				hit = true;
				if (counter > 10000) {
					return;
				}
			}
		} while (hit);
		countSource = 3; // do something just to have code here.
	}

	public void FalsePositive02(Object htmlTag) {
		if (htmlTag.toString().compareTo("strike") == 0) {
			countSource = 1;
		} else if (htmlTag.toString().compareTo("sup") == 0) {
			countSource = 2;
		} else if (htmlTag.toString().compareTo("sub") == 0) {
			countSource = 3;
		}
	}

	int beginEndTag;

	public void FalsePositive03(String source, String searchString,
			int interncaret) {
		int temphitpoint = -1;
		boolean flaghitup = false;
		int hitUp = 0;
		do {
			flaghitup = false;
			temphitpoint = source.indexOf(searchString, interncaret);
			if (temphitpoint > 0 && temphitpoint < beginEndTag) {
				hitUp++;
				flaghitup = true;
				interncaret = temphitpoint + searchString.length();
			}
		} while (flaghitup);
		if (hitUp == 0) {
			// at some point, this was reported as infeasible
			// because we forced the loop to do at least
			// one iteration
		}
	}

	public void feasible01(String source) {
		boolean hit = false;
		String idString;
		int counter = 0;
		do
		{
			hit = false;
			idString = "diesisteineidzumsuchenimsource" + counter;
			if(source.indexOf(idString) > -1)
			{
				counter++;
				hit = true;
				if(counter > 10000)
				{
					return;
				}
			}
		} while(hit);		
	}
	
	
	@SuppressWarnings("unused")
	public void feasibe02(Object htmlTag) {
		if (htmlTag.toString().compareTo("strike")==0)
		{
			String[] str = {"text-decoration","strike"};			
		}
		else if (htmlTag.toString().compareTo("sup")==0)
		{
			String[] str = {"vertical-align","sup"};			
		}
		else if (htmlTag.toString().compareTo("sub")==0)
		{
			String[] str = {"vertical-align","sub"};			
		}
	}	
	
}
