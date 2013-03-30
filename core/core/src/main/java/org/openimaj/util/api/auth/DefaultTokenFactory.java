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

public class DefaultTokenFactory implements TokenFactory {
	private static final DefaultTokenFactory instance = new DefaultTokenFactory();
	private static final String PREFS_BASE_NODE = "/org/openimaj/util/api/auth";

	private DefaultTokenFactory() {
	}

	public static DefaultTokenFactory getInstance() {
		return instance;
	}

	public <T> void deleteToken(Class<T> tokenClass) throws BackingStoreException {
		final Preferences base = Preferences.userRoot().node(PREFS_BASE_NODE);
		base.node(tokenClass.getName()).removeNode();
		base.sync();
	}

	@Override
	public <T> T getToken(Class<T> tokenClass) {
		final Token tokenDef = tokenClass.getAnnotation(Token.class);

		if (tokenDef == null)
			throw new IllegalArgumentException("The provided class is not annotated with @Token");

		try {
			T token = tryLoadToken(tokenClass);

			if (token == null) {
				token = createToken(tokenDef, tokenClass);
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

	private <T> T createToken(Token def, Class<T> clz) throws InstantiationException, IllegalAccessException,
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
				return createToken(def, clz);
			}
		}

		saveToken(instance);

		return instance;
	}

	public <T> void saveToken(T token) throws IllegalArgumentException, IllegalAccessException, BackingStoreException {
		final Class<?> tokenClass = token.getClass();

		final Token tokenDef = tokenClass.getAnnotation(Token.class);

		if (tokenDef == null)
			throw new IllegalArgumentException("The provided class is not annotated with @Token");

		final Preferences prefs = Preferences.userRoot().node(PREFS_BASE_NODE).node(tokenClass.getName());
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

	private <T> T tryLoadToken(Class<T> clz) throws BackingStoreException, InstantiationException, IllegalAccessException
	{
		Preferences prefs = Preferences.userRoot().node(PREFS_BASE_NODE);

		if (!prefs.nodeExists(clz.getName()))
			return null;

		prefs = prefs.node(clz.getName());
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

	public static <T> T loadToken(Class<T> tokenClass) {
		return getInstance().getToken(tokenClass);
	}
}
