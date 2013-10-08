/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.util.api.auth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.WordUtils;

/**
 * Default implementation of a {@link TokenFactory} that loads the token
 * parameters from the default Java user preference store or interactively
 * queries the user for the required token parameters if the token has not been
 * used before.
 * <p>
 * Interactive querying is performed via the command-line (using
 * {@link System#err} for prompts and {@link System#in} for reading user input.
 * As such, this class will only be really useful for interactive querying in
 * console applications. It is possible however to just use this class for
 * manually storing and retrieving tokens with the appropriate methods.
 * <p>
 * For this class to work in interactive mode, the token class must have a
 * public no-args constructor.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class DefaultTokenFactory implements TokenFactory {
	private static final DefaultTokenFactory instance = new DefaultTokenFactory();
	private static final String PREFS_BASE_NODE = "/org/openimaj/util/api/auth";

	private DefaultTokenFactory() {
	}

	/**
	 * Get the default singleton instance
	 * 
	 * @return the default instance
	 */
	public static DefaultTokenFactory getInstance() {
		return instance;
	}

	/**
	 * Delete the default token parameters for the given class from the store.
	 * 
	 * @param tokenClass
	 *            the token class
	 * @throws BackingStoreException
	 *             if a problem occurred communicating with the backing
	 *             preference store
	 */
	public <T> void deleteToken(Class<T> tokenClass) throws BackingStoreException {
		deleteToken(tokenClass, null);
	}

	/**
	 * Delete the named token parameters for the given class from the store.
	 * 
	 * @param tokenClass
	 *            the token class
	 * @param name
	 *            the name of the token, or <tt>null</tt> for the default token
	 * @throws BackingStoreException
	 *             if a problem occurred communicating with the backing
	 *             preference store
	 */
	public <T> void deleteToken(Class<T> tokenClass, String name) throws BackingStoreException {
		final String tokName = name == null ? tokenClass.getName() : tokenClass.getName() + "-" + name;
		final Preferences base = Preferences.userRoot().node(PREFS_BASE_NODE);

		base.node(tokName).removeNode();

		base.sync();
	}

	@Override
	public <T> T getToken(Class<T> tokenClass) {
		return getToken(tokenClass, null);
	}

	@Override
	public <T> T getToken(Class<T> tokenClass, String name) {
		final Token tokenDef = tokenClass.getAnnotation(Token.class);

		if (tokenDef == null)
			throw new IllegalArgumentException("The provided class is not annotated with @Token");

		try {
			T token = loadToken(tokenClass, name);

			if (token == null) {
				token = createToken(tokenDef, tokenClass, name);
			}

			return token;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String getMessage(Token def, Map<Field, Parameter> params) {
		String msg = String.format("You do not appear to have any credentials stored for the %s. ", def.name()) +
				String.format("To use the %s you need to have a %s.\n", def.name(), formatParams(params.values())) +
				String.format("You can get these from %s.\n\n", def.url());

		if (def.extraInfo() != null && def.extraInfo().length() > 0)
			msg += String.format(def.extraInfo() + "\n\n");

		msg += String.format("To continue please enter the credentials as indicated. ");
		msg += String.format("These will be stored automatically for future use.");

		return msg;
	}

	private <T> T createToken(Token def, Class<T> clz, String name) throws InstantiationException,
			IllegalAccessException,
			IOException, IllegalArgumentException, BackingStoreException
	{
		final Map<Field, Parameter> params = getParameters(clz);

		final T instance = clz.newInstance();
		if (params.size() == 0)
			return instance;

		System.err.format(WordUtils.wrap(getMessage(def, params) + "\n\n", 80));

		final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		final Map<Parameter, String> inputs = new HashMap<Parameter, String>();
		for (final Entry<Field, Parameter> entry : params.entrySet()) {
			query(instance, entry.getKey(), entry.getValue(), br, inputs);
		}

		System.err.println("\n\n");
		System.err.println("The following parameters have been set:\n");
		for (final Entry<Field, Parameter> entry : params.entrySet()) {
			System.err.println(entry.getValue().name() + ": " + inputs.get(entry.getValue()) + "\n");
		}

		while (true) {
			System.err.println("\nPlease confirm the parameters are correct (Y/N): ");
			if (br.readLine().trim().equalsIgnoreCase("y")) {
				break;
			} else if (br.readLine().trim().equalsIgnoreCase("n")) {
				return createToken(def, clz, name);
			}
		}

		saveToken(instance, name);

		return instance;
	}

	/**
	 * Save the parameters of the given token to the backing preference store.
	 * 
	 * @param token
	 *            the token to save
	 * @param name
	 *            the name of the token, or <tt>null</tt> for the default
	 * @throws IllegalArgumentException
	 *             if the token class isn't annotated with {@link Token}.
	 * @throws IllegalAccessException
	 *             if an error occurred reading a parameter of the token
	 * @throws BackingStoreException
	 *             if a problem occurred communicating with the backing
	 *             preference store
	 */
	public <T> void saveToken(T token, String name) throws IllegalArgumentException, IllegalAccessException,
			BackingStoreException
	{

		final Class<?> tokenClass = token.getClass();
		final String tokName = name == null ? tokenClass.getName() : tokenClass.getName() + "-" + name;

		final Token tokenDef = tokenClass.getAnnotation(Token.class);

		if (tokenDef == null)
			throw new IllegalArgumentException("The provided class is not annotated with @Token");

		final Preferences prefs = Preferences.userRoot().node(PREFS_BASE_NODE).node(tokName);
		final Map<Field, Parameter> params = getParameters(tokenClass);

		for (final Entry<Field, Parameter> entry : params.entrySet()) {
			final Field f = entry.getKey();

			if (f.getType() == Integer.class) {
				prefs.putInt(f.getName(), (Integer) f.get(token));
			} else if (f.getType() == Integer.TYPE) {
				prefs.putInt(f.getName(), f.getInt(token));
			} else if (f.getType() == Long.class) {
				prefs.putLong(f.getName(), (Long) f.get(token));
			} else if (f.getType() == Long.TYPE) {
				prefs.putLong(f.getName(), f.getLong(token));
			} else if (f.getType() == Double.class) {
				prefs.putDouble(f.getName(), (Double) f.get(token));
			} else if (f.getType() == Double.TYPE) {
				prefs.putDouble(f.getName(), f.getDouble(token));
			} else if (f.getType() == Float.class) {
				prefs.putFloat(f.getName(), (Float) f.get(token));
			} else if (f.getType() == Float.TYPE) {
				prefs.putFloat(f.getName(), f.getFloat(token));
			} else if (f.getType() == String.class) {
				prefs.put(f.getName(), (String) f.get(token));
			} else if (f.getType() == byte[].class) {
				prefs.putByteArray(f.getName(), (byte[]) f.get(token));
			}
		}

		prefs.sync();
	}

	private void query(Object instance, Field f, Parameter p, BufferedReader br, Map<Parameter, String> rawInputs)
			throws IOException,
			IllegalArgumentException, IllegalAccessException
	{

		while (true) {
			System.err.format("Please enter your %s:\n", p.name());
			final String input = br.readLine().trim();

			rawInputs.put(p, input);

			if (setValue(instance, f, input))
				return;

			System.err.format("Sorry, %s doesn't appear to be the correct format for the %s (hint: expecting a %s).\n",
					input, p.name(), getType(f));
		}
	}

	private boolean setValue(Object instance, Field f, String input) throws IllegalArgumentException,
			IllegalAccessException
	{
		try {
			if (f.getType() == Integer.class)
				f.set(instance, Integer.parseInt(input));
			else if (f.getType() == Integer.TYPE)
				f.setInt(instance, Integer.parseInt(input));
			if (f.getType() == Long.class)
				f.set(instance, Long.parseLong(input));
			else if (f.getType() == Long.TYPE)
				f.setLong(instance, Long.parseLong(input));
			if (f.getType() == Double.class)
				f.set(instance, Double.parseDouble(input));
			else if (f.getType() == Double.TYPE)
				f.setDouble(instance, Double.parseDouble(input));
			if (f.getType() == Float.class)
				f.set(instance, Float.parseFloat(input));
			else if (f.getType() == Float.TYPE)
				f.setFloat(instance, Float.parseFloat(input));
			if (f.getType() == String.class)
				f.set(instance, input);
			if (f.getType() == byte[].class)
				f.set(instance, input.getBytes());
		} catch (final NumberFormatException nfe) {
			return false;
		}

		return true;
	}

	private String getType(Field f) {
		if (f.getType() == Integer.class || f.getType() == Integer.TYPE)
			return "integer";
		if (f.getType() == Long.class || f.getType() == Long.TYPE)
			return "long";
		if (f.getType() == Double.class || f.getType() == Double.TYPE)
			return "double";
		if (f.getType() == Float.class || f.getType() == Float.TYPE)
			return "float";
		if (f.getType() == String.class)
			return "string";
		if (f.getType() == byte[].class)
			return "byte array";

		throw new UnsupportedOperationException("Unsupported field type " + f.getType() + " for field "
				+ f.getName());
	}

	private String formatParams(Collection<Parameter> values) {
		final List<Parameter> paramsList = new ArrayList<Parameter>(values);

		if (values.size() == 1)
			return paramsList.get(0).name();

		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < paramsList.size() - 2; i++)
			sb.append(paramsList.get(i).name() + ", ");
		sb.append(paramsList.get(paramsList.size() - 2).name());
		sb.append(" and ");
		sb.append(paramsList.get(paramsList.size() - 1).name());

		return sb.toString();
	}

	private Map<Field, Parameter> getParameters(Class<?> clz) {
		final Map<Field, Parameter> fields = new HashMap<Field, Parameter>();

		while (clz != null) {
			for (final Field f : clz.getDeclaredFields()) {
				final Parameter p = f.getAnnotation(Parameter.class);
				if (p != null) {
					f.setAccessible(true);
					fields.put(f, p);
				}
			}

			clz = clz.getSuperclass();
		}

		return fields;
	}

	/**
	 * Load a token with an optional name tag from the backing store.
	 * 
	 * @param clz
	 *            the class of the token
	 * @param name
	 *            the name of the token, or <tt>null</tt> for the default token
	 * @return a token loaded with the previously saved parameters, or
	 *         <tt>null</tt> if the token could not be read
	 * @throws BackingStoreException
	 *             if a problem occurred communicating with the backing
	 *             preference store
	 * @throws InstantiationException
	 *             if the token could not be constructed
	 * @throws IllegalAccessException
	 *             if an error occurred setting a parameter
	 */
	public <T> T loadToken(Class<T> clz, String name) throws BackingStoreException, InstantiationException,
			IllegalAccessException
	{
		final String tokName = name == null ? clz.getName() : clz.getName() + "-" + name;
		Preferences prefs = Preferences.userRoot().node(PREFS_BASE_NODE);

		if (!prefs.nodeExists(tokName))
			return null;

		prefs = prefs.node(tokName);
		final String[] keys = prefs.keys();

		final T instance = clz.newInstance();

		final Map<Field, Parameter> params = getParameters(clz);
		for (final Entry<Field, Parameter> p : params.entrySet()) {
			final Field field = p.getKey();
			final Parameter parameter = p.getValue();

			if (!ArrayUtils.contains(keys, field.getName()))
				return null; // missing value in store, so force new one to be
								// created

			loadValue(instance, field, parameter, prefs);
		}

		return instance;
	}

	private <T> void loadValue(T instance, Field field, Parameter parameter, Preferences node)
			throws IllegalArgumentException, IllegalAccessException
	{
		final String fieldName = field.getName();

		if (field.getType() == Integer.TYPE) {
			field.setInt(instance, node.getInt(fieldName, 0));
		} else if (field.getType() == Integer.class) {
			field.set(instance, node.getInt(fieldName, 0));
		} else if (field.getType() == Long.TYPE) {
			field.setLong(instance, node.getLong(fieldName, 0));
		} else if (field.getType() == Long.class) {
			field.set(instance, node.getLong(fieldName, 0));
		} else if (field.getType() == Double.TYPE) {
			field.setDouble(instance, node.getDouble(fieldName, 0));
		} else if (field.getType() == Double.class) {
			field.set(instance, node.getDouble(fieldName, 0));
		} else if (field.getType() == Float.TYPE) {
			field.setFloat(instance, node.getFloat(fieldName, 0));
		} else if (field.getType() == Float.class) {
			field.set(instance, node.getFloat(fieldName, 0));
		} else if (field.getType() == Boolean.TYPE) {
			field.setBoolean(instance, node.getBoolean(fieldName, false));
		} else if (field.getType() == Boolean.class) {
			field.set(instance, node.getBoolean(fieldName, false));
		} else if (field.getType() == String.class) {
			field.set(instance, node.get(fieldName, null));
		} else if (field.getType() == byte[].class) {
			field.set(instance, node.getByteArray(fieldName, null));
		} else {
			throw new UnsupportedOperationException("Unsupported field type " + field.getType() + " for field "
					+ fieldName);
		}
	}

	/**
	 * Convenience method equivalent to
	 * <tt>getInstance().getToken(tokenClass)</tt>.
	 * 
	 * @see #getToken(Class)
	 * @param tokenClass
	 *            the class of the token to build
	 * @return the token
	 */
	public static <T> T get(Class<T> tokenClass) {
		return getInstance().getToken(tokenClass);
	}

	/**
	 * Convenience method equivalent to
	 * <tt>getInstance().getToken(tokenClass, name)</tt>.
	 * 
	 * @see #getToken(Class, String)
	 * @param tokenClass
	 *            the class of the token to build
	 * @param name
	 *            the name of the token
	 * @return the token
	 */
	public static <T> T get(Class<T> tokenClass, String name) {
		return getInstance().getToken(tokenClass);
	}
}
