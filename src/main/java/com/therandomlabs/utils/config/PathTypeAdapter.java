package com.therandomlabs.utils.config;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;

public final class PathTypeAdapter implements TypeAdapter {
	private final boolean isArray;

	public PathTypeAdapter(boolean isArray) {
		this.isArray = isArray;
	}

	@Override
	public Object getValue(CommentedFileConfig config, String name, Object defaultValue) {
		if(!isArray) {
			return getPath(config.get(name));
		}

		final List<String> list = config.get(name);
		final List<Path> values = new ArrayList<>(list.size());

		for(String element : list) {
			final Path path = getPath(element);

			if(path != null) {
				values.add(path);
			}
		}

		return values.toArray(new Path[0]);
	}

	@Override
	public void setValue(CommentedFileConfig config, String name, Object value) {
		if(isArray) {
			config.set(
					name,
					Arrays.stream((Object[]) value).
							map(this::asString).
							collect(Collectors.toList())
			);
		} else {
			config.set(name, asString(value));
		}
	}

	@Override
	public String asString(Object value) {
		return toStringWithUnixPathSeparators((Path) value);
	}

	@Override
	public boolean isArray() {
		return isArray;
	}

	private static Path getPath(String path) {
		try {
			return Paths.get(path).normalize();
		} catch(InvalidPathException ignored) {}

		return null;
	}

	private static String toStringWithUnixPathSeparators(Path path) {
		return path.toString().replace('\\', '/');
	}
}
