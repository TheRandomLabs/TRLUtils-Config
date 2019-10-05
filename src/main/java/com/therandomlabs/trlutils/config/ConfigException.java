package com.therandomlabs.trlutils.config;

public class ConfigException extends RuntimeException {
	private static final long serialVersionUID = 7219817454671602843L;

	public ConfigException(String message) {
		super(message);
	}

	public ConfigException(String message, Throwable cause) {
		super(message, cause);
	}

	public static ConfigException property(String property, Throwable cause) {
		return new ConfigException("Exception regarding property " + property, cause);
	}
}
