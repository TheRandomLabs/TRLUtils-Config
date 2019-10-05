package com.therandomlabs.utils.config;

import org.apache.commons.lang3.ArrayUtils;

public final class ArrayConverter {
	private ArrayConverter() {}

	public static Object toPrimitiveArray(Object[] boxedArray) {
		if(boxedArray instanceof Boolean[]) {
			return ArrayUtils.toPrimitive((Boolean[]) boxedArray);
		}

		if(boxedArray instanceof Byte[]) {
			return ArrayUtils.toPrimitive((Byte[]) boxedArray);
		}

		if(boxedArray instanceof Character[]) {
			return ArrayUtils.toPrimitive((Character[]) boxedArray);
		}

		if(boxedArray instanceof Double[]) {
			return ArrayUtils.toPrimitive((Double[]) boxedArray);
		}

		if(boxedArray instanceof Float[]) {
			return ArrayUtils.toPrimitive((Float[]) boxedArray);
		}

		if(boxedArray instanceof Integer[]) {
			return ArrayUtils.toPrimitive((Integer[]) boxedArray);
		}

		if(boxedArray instanceof Long[]) {
			return ArrayUtils.toPrimitive((Long[]) boxedArray);
		}

		if(boxedArray instanceof Short[]) {
			return ArrayUtils.toPrimitive((Short[]) boxedArray);
		}

		return boxedArray;
	}

	public static Object[] toBoxedArray(Object primitiveArray) {
		if(primitiveArray instanceof Object[]) {
			return (Object[]) primitiveArray;
		}

		if(primitiveArray instanceof boolean[]) {
			return ArrayUtils.toObject((byte[]) primitiveArray);
		}

		if(primitiveArray instanceof byte[]) {
			return ArrayUtils.toObject((byte[]) primitiveArray);
		}

		if(primitiveArray instanceof char[]) {
			return ArrayUtils.toObject((char[]) primitiveArray);
		}

		if(primitiveArray instanceof double[]) {
			return ArrayUtils.toObject((double[]) primitiveArray);
		}

		if(primitiveArray instanceof float[]) {
			return ArrayUtils.toObject((float[]) primitiveArray);
		}

		if(primitiveArray instanceof int[]) {
			return ArrayUtils.toObject((int[]) primitiveArray);
		}

		if(primitiveArray instanceof long[]) {
			return ArrayUtils.toObject((long[]) primitiveArray);
		}

		if(primitiveArray instanceof short[]) {
			return ArrayUtils.toObject((long[]) primitiveArray);
		}

		return (Object[]) primitiveArray;
	}
}
