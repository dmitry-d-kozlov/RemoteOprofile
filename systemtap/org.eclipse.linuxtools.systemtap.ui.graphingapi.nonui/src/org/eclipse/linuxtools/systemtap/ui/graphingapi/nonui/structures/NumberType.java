/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.structures;

public final class NumberType {
	
	/**
	 * Returns a new instance of the passed in num of type orig.
	 * 
	 * @param orig The desired datatype.
	 * @param num The number to replicate.
	 * 
	 * @return The new cast number.
	 */
	public static Number getNumber(Number orig, double num) {
		if(orig instanceof Long)
			return new Long((long)num);
		else if(orig instanceof Double)
			return new Double(num);
		else if(orig instanceof Float)
			return new Float((float)num);
		else if(orig instanceof Byte)
			return new Byte((byte)num);
		else if(orig instanceof Short)
			return new Short((short)num);
		return new Integer((int)num);
	}
	
	/**
	 * Takes an array of objects and returns an array of Numbers.
	 * 
	 * @param o The array of objects to convert.
	 * 
	 * @return The array of Numbers from o.
	 */
	public static Number[] obj2num(Object[] o) {
		Number[] arr = new Number[o.length];
		for(int i = 0; i < o.length; i++)
			arr[i] = obj2num(o[i]);

		return arr;
	}
	
	/**
	 * Takes an array of Numbers and returns an array of objects.
	 * 
	 * @param o The array of Numbers to convert.
	 * 
	 * @return The array of objects from o.
	 */
	public static Number obj2num(Object o) {
		Number n = null;
	
		try {
			n = cleanObj2Num(o);
		} catch(NumberFormatException e ) {
			n = new Long(0);
		}
		return n;
	}
	
	/**
	 * Returns an array of Numbers type if they are Numbers, otherwise, returns Doubles and Longs as appropriate 
	 * based on whether or not it has a '.'
	 * 
	 * @param o The object array to clean.
	 * 
	 * @return The Number array cleaned.
	 */
	public static Number[] cleanObj2Num(Object[] o) throws NumberFormatException {
		Number[] arr = new Number[o.length];
		for(int i = 0; i < o.length; i++)
			arr[i] = cleanObj2Num(o[i]);

		return arr;
	}

	/**
	 * Returns a Number type if its a Number, otherwise, returns Doubles and Longs as appropriate 
	 * based on whether or not it has a '.'
	 * 
	 * @param o The object to clean.
	 * 
	 * @return The Number object cleaned.
	 */
	public static Number cleanObj2Num(Object o) throws NumberFormatException {
		if(o instanceof Number)
			return (Number)o;
		else {
			if(o.toString().contains("."))
				return new Double(Double.parseDouble(o.toString()));
			else
				return new Long(Long.parseLong(o.toString()));
		}
	}
}
