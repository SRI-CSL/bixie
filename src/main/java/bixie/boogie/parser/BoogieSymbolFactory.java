/*
 * boogieamp - Parser, Factory, and Utilities to create Boogie Programs from Java
 * Copyright (C) 2013 Martin Schaef and Stephan Arlt
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package bixie.boogie.parser;

import java_cup.runtime.Symbol;
import java_cup.runtime.SymbolFactory;

public class BoogieSymbolFactory implements SymbolFactory {
	class BoogieSymbol extends Symbol {
		private final String name;
		private final int lcolumn;
		private final int rcolumn;

		public BoogieSymbol(String name, int id, int state) {
			// Grrr, the constructor is protected, but
			// at least the field is writeable...
			super(id);
			this.parse_state = state;
			this.name = name;
			this.lcolumn = -1;
			this.rcolumn = -1;
		}

		public BoogieSymbol(String name, int id, int left, int lcolumn,
				int right, int rcolumn, Object o) {
			super(id, left, right, o);
			this.name = name;
			this.lcolumn = lcolumn;
			this.rcolumn = rcolumn;
		}

		public BoogieSymbol(String name, int id, Symbol left, Symbol right,
				Object o) {			
			super(id, left, right, o);
			this.name = name;
			if (left instanceof BoogieSymbol)
				lcolumn = ((BoogieSymbol) left).lcolumn;
			else
				lcolumn = 0;
			if (right instanceof BoogieSymbol)
				rcolumn = ((BoogieSymbol) right).rcolumn;
			else
				rcolumn = 0;
		}

		public int getLeftColumn() {
			return this.lcolumn;
		}

		public int getRightColumn() {
			return this.rcolumn;
		}

		public String getLocation() {
			if (lcolumn >= 0)
				return "" + left + ":" + lcolumn;
			else
				return "" + left;
		}

		public String getName() {
			return name;
		}

		public String toString() {
			return "(" + name + " " + left + ":" + lcolumn + "-" + right + ":"
					+ rcolumn + ")";
		}
	}

	// Factory methods
	public Symbol newSymbol(String name, int id, int lline, int lcol,
			int rline, int rcol, Object value) {
		return new BoogieSymbol(name, id, lline, lcol, rline, rcol, value);
	}

	public Symbol newSymbol(String name, int id, int lline, int lcol,
			int rline, int rcol) {
		return new BoogieSymbol(name, id, lline, lcol, rline, rcol, null);
	}

	public Symbol newSymbol(String name, int id, Symbol left, Symbol right,
			Object value) {
		return new BoogieSymbol(name, id, left, right, value);
	}

	public Symbol newSymbol(String name, int id, Symbol left, Symbol right) {
		return new BoogieSymbol(name, id, left, right, null);
	}

	public Symbol newSymbol(String name, int id) {
		return new BoogieSymbol(name, id, -1, -1, -1, -1, null);
	}

	public Symbol newSymbol(String name, int id, Object value) {
		return new BoogieSymbol(name, id, -1, -1, -1, -1, value);
	}

	public Symbol startSymbol(String name, int id, int state) {
		BoogieSymbol s = new BoogieSymbol(name, id, state);
		return s;
	}
}
