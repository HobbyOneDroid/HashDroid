/* HashFactory.java -- 
   Copyright (C) 2001, 2002, 2003, 2006 Free Software Foundation, Inc.

This file is a part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or (at
your option) any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
USA

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version.  */

package com.hobbyone.HashDroid;

/**
 * <p>
 * A <i>Factory</i> to instantiate message digest algorithm instances.
 * </p>
 */
public class HashFactory {

	// Constants and variables
	// -------------------------------------------------------------------------

	// Constructor(s)
	// -------------------------------------------------------------------------

	/** Trivial constructor to enforce <i>Singleton</i> pattern. */
	private HashFactory() {
		super();
	}

	// Class methods
	// -------------------------------------------------------------------------

	/**
	 * <p>
	 * Return an instance of a hash algorithm given its name.
	 * </p>
	 * 
	 * @param name
	 *            the name of the hash algorithm.
	 * @return an instance of the hash algorithm, or null if none found.
	 * @exception InternalError
	 *                if the implementation does not pass its self- test.
	 */
	public static IMessageDigest getInstance(String name) {
		if (name == null) {
			return null;
		}

		name = name.trim();
		IMessageDigest result = null;
		if (name.equalsIgnoreCase("haval"))
			result = new Haval();
		else if (name.equalsIgnoreCase("md2"))
			result = new MD2();
		else if (name.equalsIgnoreCase("md4"))
			result = new MD4();
		else if (name.equalsIgnoreCase("md5"))
			result = new MD5();
		else if (name.equalsIgnoreCase("ripemd-128"))
			result = new RipeMD128();
		else if (name.equalsIgnoreCase("ripemd-160"))
			result = new RipeMD160();
		else if (name.equalsIgnoreCase("sha-1"))
			result = new Sha160();
		else if (name.equalsIgnoreCase("sha-256"))
			result = new Sha256();
		else if (name.equalsIgnoreCase("sha-384"))
			result = new Sha384();
		else if (name.equalsIgnoreCase("sha-512"))
			result = new Sha512();
		else if (name.equalsIgnoreCase("tiger"))
			result = new Tiger();
		else if (name.equalsIgnoreCase("whirlpool"))
			result = new Whirlpool();

		return result;
	}
}
