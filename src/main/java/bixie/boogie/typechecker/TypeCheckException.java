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

package bixie.boogie.typechecker;

/**
 * This exception is thrown by the type checker if there is a type error in the
 * Boogie file.
 * 
 * @author hoenicke
 * 
 */
public class TypeCheckException extends RuntimeException {
	private static final long serialVersionUID = -6173384860964538008L;

	public TypeCheckException() {
	}

	public TypeCheckException(String message) {
		super(message);
	}

	public TypeCheckException(Throwable cause) {
		super(cause);
	}

	public TypeCheckException(String message, Throwable cause) {
		super(message, cause);
	}

}
