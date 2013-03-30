package org.openimaj.util.api.auth;

public interface TokenFactory {
	<T> T getToken(Class<T> tokenClass);
}
