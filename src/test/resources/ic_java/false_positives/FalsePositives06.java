package ic_java.false_positives;

import java.io.Serializable;

public class FalsePositives06 {

	@SuppressWarnings("serial")
	public class FilterMap implements Serializable {

	}

	@SuppressWarnings("unused")
	private static final class ContextFilterMaps {
		private final Object lock = new Object();

		/**
		 * The set of filter mappings for this application, in the order they
		 * were defined in the deployment descriptor with additional mappings
		 * added via the {@link ServletContext} possibly both before and after
		 * those defined in the deployment descriptor.
		 */
		private FilterMap[] array = new FilterMap[0];

		/**
		 * Filter mappings added via {@link ServletContext} may have to be
		 * inserted before the mappings in the deployment descriptor but must be
		 * inserted in the order the {@link ServletContext} methods are called.
		 * This isn't an issue for the mappings added after the deployment
		 * descriptor - they are just added to the end - but correctly the
		 * adding mappings before the deployment descriptor mappings requires
		 * knowing where the last 'before' mapping was added.
		 */
		private int insertPoint = 0;

		/**
		 * Return the set of filter mappings.
		 */
		public FilterMap[] asArray() {
			synchronized (lock) {
				return array;
			}
		}

	}

}