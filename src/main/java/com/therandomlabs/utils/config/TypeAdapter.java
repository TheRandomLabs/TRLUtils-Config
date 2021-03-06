package com.therandomlabs.utils.config;

import java.util.Arrays;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;

public interface TypeAdapter {
	default Object getValue(CommentedFileConfig config, String name, Object defaultValue) {
		return config.get(name);
	}

	default void setValue(CommentedFileConfig config, String name, Object value) {
		if (isArray()) {
			config.set(name, Arrays.asList(ArrayConverter.toBoxedArray(value)));
		} else {
			config.set(name, value);
		}
	}

	default String asString(Object value) {
		return String.valueOf(value);
	}

	default boolean isArray() {
		return false;
	}

	default boolean canBeNull() {
		return false;
	}

	default boolean shouldLoad() {
		return true;
	}

	default Object reloadDefault(Object defaultValue) {
		return defaultValue;
	}
}
