package ic_java.true_positives;

import java.util.List;

public class TruePositives02 {

	
	public int getNewIndex(int index, List<Object> ints, List<Object> chars, List<Object> bytes,
			List<Object> booleans) {
		int max = 0;
		if (booleans.size() > 0 && index < 63)
			max = 64;
		else if (bytes.size() > 0 && index < 56)
			max = 57;
		else if (chars.size() > 0 && index < 48)
			max = 49;
		else if (ints.size() > 0 && index < 32)
			max = 33;

		if (max != 0) {
			int rand = getInt(4);
			max = max - index;
			// at this point, max must be 1 because
			// if max!=0, then one of the cases above
			// was taken s.t. max is always set to index+1
			// hence the line above always sets index to 1
			if (max > rand)
				max = rand;
			else if (max != 1)
				max = getInt(max); // unreachable
			index += max;
		}
		return index;
	}

	private int getInt(int i) {
		// TODO Auto-generated method stub
		return 0;
	}
}
