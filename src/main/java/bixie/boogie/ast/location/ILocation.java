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

package bixie.boogie.ast.location;

/**
 * Defines an area in a text file. Used to specify where an ASTNode is defined.
 * 
 * @author heizmann@informatik.uni-freiburg.de
 * 
 */
public interface ILocation {

	/**
	 * @return Name of this {@code Location}s file.
	 */
	public String getFileName();

	/**
	 * @return Number of line where this {@code Location} begins.
	 */
	public int getStartLine();

	/**
	 * @return Number of line where this {@code Location} ends.
	 */
	public int getEndLine();

	/**
	 * @return Number of column where this {@code Location} begins.
	 */
	public int getStartColumn();

	/**
	 * @return Number of column where this {@code Location} ends.
	 */
	public int getEndColumn();

	/**
	 * This {@code Location} can be an auxiliary {@code Location} constructed
	 * with respect to some <i>origin</i> {@code Location}. E.g., if this is an
	 * auxiliary {@code Location} for the else-branch the <i>origin</i>
	 * {@code Location} can be the {@code Location} of an if-then-else statement
	 * of a program.
	 * 
	 * If this {@code Location} is no auxiliary location the <i>origin</i> is
	 * the location itself.
	 */
	public ILocation getOrigin();

	/**
	 * 
	 * @return true iff this Location represents a loop.
	 */
	public boolean isLoop();
}
