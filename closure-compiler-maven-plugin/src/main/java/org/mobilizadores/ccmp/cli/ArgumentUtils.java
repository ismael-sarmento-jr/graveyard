package org.mobilizadores.ccmp.cli;

import java.lang.reflect.Field;
import java.lang.reflect.MalformedParameterizedTypeException;
import java.lang.reflect.ParameterizedType;

import org.apache.commons.lang3.ClassUtils;

/**
 * Adds a layer of abstraction on top of reflection utilities, to make dealing with 
 * arguments more transparent.
 */
public class ArgumentUtils {

	/**
	 * 
	 * @param option
	 * @return
	 * @throws WrongArgumentTypeException
	 */
	public static Class<?> getActualTypeOfIterable(Field option) throws WrongArgumentTypeException {
		try {
			return Class.forName(((ParameterizedType) option.getGenericType()).getActualTypeArguments()[0].getTypeName());
		} catch (ClassNotFoundException 
				| MalformedParameterizedTypeException 
				| TypeNotPresentException e ) {
			throw new WrongArgumentTypeException(e.getMessage());
		}
	}

	/**
	 * Checks if the class represents a simple type. A simple types are any of following:
	 * String, Boolean, Byte, Character, Short, Integer, Long, Double, Float - primitive or wrapper.
	 * 
	 * If it is not a simple type, it is considered a complex type. A complex type can be anything, 
	 * like Car, Mountain, Vacation, etc.
	 * 
	 * @param type
	 * 			class type to be checked
	 * @return
	 * 		true if it is a simple type
	 * 		false if it is a complex type
	 */
	public static boolean isSimpleType(Class<?> type) {
		return ClassUtils.isPrimitiveOrWrapper(type) || String.class.isAssignableFrom(type);
	}
}
