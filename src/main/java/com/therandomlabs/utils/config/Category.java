package com.therandomlabs.utils.config;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;

final class Category {
	final String fullyQualifiedName;
	final String languageKeyPrefix;
	final String languageKey;
	final Class<?> clazz;
	final String comment;
	final String name;
	final List<Property> properties = new ArrayList<>();

	final Method onReload;
	final Method onReloadClient;

	Category(
			String fullyQualifiedNamePrefix, String languageKeyPrefix, Class<?> clazz,
			String comment, String name
	) {
		fullyQualifiedName = fullyQualifiedNamePrefix + name;
		this.languageKeyPrefix = languageKeyPrefix;
		languageKey = languageKeyPrefix + name;
		this.clazz = clazz;
		this.comment = comment;
		this.name = name;
		onReload = getOnReloadMethod(clazz, "onReload");
		onReloadClient = getOnReloadMethod(clazz, "onReloadClient");
	}

	void initialize(CommentedFileConfig config) {
		config.setComment(fullyQualifiedName, comment);
	}

	void onReload(boolean client) {
		final Method method = client ? onReloadClient : onReload;

		if(method != null) {
			try {
				method.invoke(null);
			} catch(IllegalAccessException | InvocationTargetException ex) {
				throw new ConfigException("Failed to reload configuration category", ex);
			}
		}
	}

	String getFullyQualifiedName() {
		return fullyQualifiedName;
	}

	String getLanguageKeyPrefix() {
		return languageKey + ".";
	}

	private static Method getOnReloadMethod(Class<?> clazz, String name) {
		final Method onReload;

		try {
			onReload = clazz.getDeclaredMethod(name);
		} catch(NoSuchMethodException ex) {
			return null;
		}

		final int modifiers = onReload.getModifiers();

		if(!Modifier.isPublic(modifiers) || !Modifier.isStatic(modifiers) ||
				onReload.getReturnType() != void.class) {
			throw new ConfigException(name + " must be public static void");
		}

		return onReload;
	}
}
