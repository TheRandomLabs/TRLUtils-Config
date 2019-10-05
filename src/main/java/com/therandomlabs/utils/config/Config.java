package com.therandomlabs.utils.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Config {
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	@interface Category {
		//Comment
		String[] value();
	}

	//In the context of Minecraft, this refers to a Minecraft restart
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	@interface RequiresRestart {}

	//In the context of Minecraft, this refers to a world reload
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	@interface RequiresReload {}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	@interface RangeInt {
		int min() default Integer.MIN_VALUE;

		int max() default Integer.MAX_VALUE;
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	@interface RangeDouble {
		double min() default Double.MIN_VALUE;

		double max() default Double.MAX_VALUE;
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	@interface Blacklist {
		String[] value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	@interface Property {
		//Comment
		String[] value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	@interface NonNull {}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	@interface Previous {
		//Previous name
		String value();
	}

	String id();

	String[] comment();

	String path() default "";
}
