package com.therandomlabs.utils.config;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.ParsingException;
import org.apache.commons.lang3.StringUtils;

public final class ConfigManager {
	private static final Map<Class<?>, ConfigData> CONFIGS = new HashMap<>();
	private static final List<Predicate<Field>> VERSION_CHECKERS = new ArrayList<>();

	private static boolean client = true;

	private ConfigManager() {}

	public static void setClient(boolean flag) {
		client = flag;
	}

	public static void registerVersionChecker(Predicate<Field> predicate) {
		VERSION_CHECKERS.add(predicate);
	}

	public static void register(Class<?> clazz) {
		final Config config = clazz.getAnnotation(Config.class);

		if(config == null) {
			throw new ConfigException(clazz.getName() + " is not a configuration class");
		}

		final String id = config.id();

		final String[] comment = config.comment();

		if(StringUtils.join(comment).trim().isEmpty()) {
			throw new ConfigException("Configuration comment may not be empty");
		}

		//Ensure path is valid by initializing it first
		final String pathData = config.path();
		final String pathString =
				"config/" + (pathData.isEmpty() ? id : config.path()) + ".toml";
		final Path path = Paths.get(pathString).toAbsolutePath();

		try {
			Files.createDirectories(path.getParent());
		} catch(IOException ex) {
			throw new ConfigException("Failed to create configuration directory", ex);
		}

		final List<Category> categories = new ArrayList<>();
		loadCategories("", id + ".config.", "", clazz, categories);
		final ConfigData data = new ConfigData(comment, clazz, pathString, path, categories);

		CONFIGS.put(clazz, data);
		reloadFromDisk(clazz);
	}

	public static void reloadFromDisk(Class<?> clazz) {
		final ConfigData data = CONFIGS.get(clazz);

		try {
			data.config.load();
		} catch(ParsingException ex) {
			data.config.entrySet().clear();
		}

		reloadFromConfig(clazz);
	}

	public static void reloadFromConfig(Class<?> clazz) {
		final ConfigData data = CONFIGS.get(clazz);

		for(Category category : data.categories) {
			for(Property property : category.properties) {
				if(property.exists(data.config)) {
					final String name = property.getFullyQualifiedName();

					try {
						if(property.shouldLoad()) {
							final Object delayedLoad = data.delayedLoad.get(name);

							if(delayedLoad != null) {
								property.reloadDefault();
								data.config.set(name, delayedLoad);
								data.delayedLoad.remove(name);
							}

							property.deserialize(data.config);
						} else {
							//Mainly for ResourceLocations so that if a modded ResourceLocation
							//is loaded too early, it isn't reset in the config
							data.delayedLoad.put(name, data.config.get(name));
						}
					} catch(Exception ex) {
						throw ConfigException.property(name, ex);
					}
				}
			}
		}

		writeToDisk(clazz);
	}

	public static void writeToDisk(Class<?> clazz) {
		final ConfigData data = CONFIGS.get(clazz);

		final List<CommentedConfig> subConfigs = new ArrayList<>();
		subConfigs.add(data.config);

		//Remove all comments so we can tell which properties and categories no longer exist
		//afterwards
		while(!subConfigs.isEmpty()) {
			final int size = subConfigs.size();

			for(int i = 0; i < size; i++) {
				for(CommentedConfig.Entry entry : subConfigs.get(i).entrySet()) {
					entry.removeComment();

					final Object raw = entry.getRawValue();

					if(raw instanceof CommentedConfig) {
						subConfigs.add((CommentedConfig) raw);
					}
				}
			}

			subConfigs.subList(0, size).clear();
		}

		for(Category category : data.categories) {
			category.initialize(data.config);

			category.onReload(false);

			if(client) {
				category.onReload(true);
			}

			for(Property property : category.properties) {
				final String name = property.getFullyQualifiedName();

				try {
					//Even if this is replaced by delayedLoad anyway, Property#serialize
					//sets the comment so that it doesn't get removed below
					property.serialize(data.config);

					final Object delayedLoad = data.delayedLoad.get(name);

					if(delayedLoad != null) {
						data.config.set(name, delayedLoad);
					}
				} catch(Exception ex) {
					throw ConfigException.property(name, ex);
				}
			}
		}

		//Remove all entries without a comment, i.e. entries that are not defined in the
		//configuration class

		subConfigs.add(data.config);

		while(!subConfigs.isEmpty()) {
			final int size = subConfigs.size();

			for(int i = 0; i < size; i++) {
				final CommentedConfig subConfig = subConfigs.get(i);
				final Set<String> toRemove = new HashSet<>();

				for(CommentedConfig.Entry entry : subConfig.entrySet()) {
					if(entry.getComment() == null) {
						toRemove.add(entry.getKey());
						continue;
					}

					final Object raw = entry.getRawValue();

					if(raw instanceof CommentedConfig) {
						subConfigs.add((CommentedConfig) raw);
					}
				}

				toRemove.forEach(subConfig::remove);
			}

			subConfigs.subList(0, size).clear();
		}

		data.config.save();

		try {
			final List<String> lines = new ArrayList<>(Files.readAllLines(data.path));
			lines.addAll(0, data.comment);
			Files.write(data.path, lines);
		} catch(IOException ex) {
			throw new ConfigException("Failed to write config", ex);
		}
	}

	public static CommentedFileConfig get(Class<?> clazz) {
		return CONFIGS.get(clazz).config;
	}

	public static String getPathString(Class<?> clazz) {
		return CONFIGS.get(clazz).pathString;
	}

	public static Path getPath(Class<?> clazz) {
		return CONFIGS.get(clazz).path;
	}

	private static void loadCategories(
			String fullyQualifiedNamePrefix, String languageKeyPrefix, String parentCategory,
			Class<?> clazz, List<Category> categories
	) {
		for(Field field : clazz.getDeclaredFields()) {
			final Config.Category categoryData = field.getAnnotation(Config.Category.class);

			if(categoryData == null) {
				continue;
			}

			final String comment = " " + StringUtils.join(categoryData.value(), "\n ");

			if(comment.trim().isEmpty()) {
				throw new ConfigException("Category comment may not be empty");
			}

			final String name = field.getName();
			final int modifiers = field.getModifiers();

			if(!Modifier.isPublic(modifiers) || !Modifier.isStatic(modifiers) ||
					!Modifier.isFinal(modifiers)) {
				throw new ConfigException(name + " is not public static final");
			}

			boolean valid = true;

			for(Predicate<Field> predicate : VERSION_CHECKERS) {
				if(!predicate.test(field)) {
					valid = false;
					break;
				}
			}

			if(!valid) {
				continue;
			}

			final Class<?> categoryClass = field.getType();
			final String categoryName = parentCategory + name;

			final Category category = new Category(
					fullyQualifiedNamePrefix, languageKeyPrefix, categoryClass, comment,
					categoryName
			);
			loadCategory(category);
			categories.add(category);

			//Load subcategories
			loadCategories(
					fullyQualifiedNamePrefix, languageKeyPrefix, categoryName + ".", categoryClass,
					categories
			);
		}
	}

	private static void loadCategory(Category category) {
		for(Field field : category.clazz.getDeclaredFields()) {
			final Config.Property propertyData = field.getAnnotation(Config.Property.class);

			if(propertyData == null) {
				continue;
			}

			final String comment = " " + StringUtils.join(propertyData.value(), "\n ");

			if(comment.trim().isEmpty()) {
				throw new ConfigException("Property comment may not be empty");
			}

			final String name = field.getName();
			final int modifiers = field.getModifiers();

			if(!Modifier.isPublic(modifiers) || !Modifier.isStatic(modifiers) ||
					Modifier.isFinal(modifiers)) {
				throw new ConfigException(name + " is not public static non-final");
			}

			boolean valid = true;

			for(Predicate<Field> predicate : VERSION_CHECKERS) {
				if(!predicate.test(field)) {
					valid = false;
					break;
				}
			}

			if(!valid) {
				continue;
			}

			final Config.Previous previousData = field.getAnnotation(Config.Previous.class);
			final String previous = previousData == null ? null : previousData.value();

			try {
				category.properties.add(new Property(category, name, field, comment, previous));
			} catch(RuntimeException ex) {
				throw new ConfigException(name, ex);
			}
		}
	}
}
