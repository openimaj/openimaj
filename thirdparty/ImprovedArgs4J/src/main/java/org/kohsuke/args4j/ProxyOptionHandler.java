package org.kohsuke.args4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.kohsuke.args4j.spi.MethodSetter;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;
import org.kohsuke.args4j.spi.Setters;

/**
 * The {@link ProxyOptionHandler} allows options to have associated options.
 * For example, an enum option might have different options depending
 * of its value.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class ProxyOptionHandler extends OptionHandler<Object> {
	OptionHandler<?> proxy;

	/**
	 * Default constructor.
	 * @param parser the parser
	 * @param option the option definition
	 * @param setter the setter
	 * @throws CmdLineException 
	 */
	public ProxyOptionHandler(CmdLineParser parser, OptionDef option, Setter<? super Object> setter) throws CmdLineException {
		super(parser, option, setter);

		OptionDef proxyOption = new OptionDef(option.usage(), option.metaVar(), option.required(), OptionHandler.class, option.isMultiValued()); 
		proxy = parser.createOptionHandler(proxyOption, setter);

		if (!option.required() && CmdLineOptionsProvider.class.isAssignableFrom(this.setter.getType())) {
			handleExtraArgs();
		}
	}

	@Override
	public String getDefaultMetaVariable() {
		return proxy.getDefaultMetaVariable();
	}

	@Override
	public int parseArguments(Parameters params) throws CmdLineException {
		if (CmdLineOptionsProvider.class.isAssignableFrom(this.setter.getType())) {
			removeExtraArgs();
		}

		int val = proxy.parseArguments(params);

		if (CmdLineOptionsProvider.class.isAssignableFrom(this.setter.getType())) {
			handleExtraArgs();
		}

		return val;
	}

	private void removeExtraArgs() throws CmdLineException {
		try {
			Setter<?> actualsetter = null;

			if (setter instanceof SetterWrapper) {
				actualsetter = ((SetterWrapper)setter).setter;
			} else {
				actualsetter = setter;
			}

			Class<?> type = actualsetter.getClass();

			Field beanField = type.getDeclaredField("bean");
			beanField.setAccessible(true);
			Object bean = beanField.get(actualsetter);

			Field field = type.getDeclaredField("f");
			field.setAccessible(true);

			field = (Field) field.get(actualsetter);
			field.setAccessible(true);

			//the actual value being set:
			Object object = field.get(bean);
			if (object == null) return;

			if(object instanceof ArrayList) {
				//For the time being we'll do nothing if its a list; we'll
				//assume that once an option has been added it can't be removed.
				//This INCLUDES defaults!!
//				@SuppressWarnings("unchecked")
//				ArrayList<CmdLineOptionsProvider> list = (ArrayList<CmdLineOptionsProvider>) object;
//				
//				if (list.size() > 0) {
//					Object obj = list.get(list.size()-1).getOptions();
//
//					//removeOptions(obj, owner);
//				}
			}
			else
			{
				Object obj = ((CmdLineOptionsProvider)object).getOptions();

				if (obj instanceof Enum)
					System.err.println("Warning: Using an enum ("+field+") as an options object with proxied options is not recommended and will be disallowed in the near future!");

				removeOptions(obj, owner);				
			}
		} catch (CmdLineException e) {
			throw e;
		} catch (Exception e) {
			throw new CmdLineException(owner, "", e);
		}
	}

	@SuppressWarnings("unchecked")
	private void handleExtraArgs() throws CmdLineException {
		try {
			Setter<?> actualsetter = null;

			if (setter instanceof SetterWrapper) {
				actualsetter = ((SetterWrapper)setter).setter;
			} else {
				actualsetter = setter;
			}

			Class<?> type = actualsetter.getClass();

			Field beanField = type.getDeclaredField("bean");
			beanField.setAccessible(true);
			Object bean = beanField.get(actualsetter);

			Field field = type.getDeclaredField("f");
			field.setAccessible(true);

			field = (Field) field.get(actualsetter);
			field.setAccessible(true);

			//the actual value being set:
			Object object = field.get(bean);
			if (object == null) return;

			if(object instanceof ArrayList) {
				if(((ArrayList<CmdLineOptionsProvider>) object).size() > 0){
					Object obj = ((ArrayList<CmdLineOptionsProvider>) object).get(((ArrayList<CmdLineOptionsProvider>) object).size()-1).getOptions();

					addOptions(obj, owner);
					setObjectField(bean, field, obj);
				}
			}
			else
			{
				Object obj = ((CmdLineOptionsProvider)object).getOptions();

				if (obj instanceof Enum)
					System.err.println("Warning: Using an enum ("+field+") as an options object with proxied options is not recommended and will be disallowed in the near future!");

				addOptions(obj, owner);				
				setObjectField(bean, field, obj);
			}

			// for display purposes, we like the arguments in argument order, but the options in alphabetical order
			Field optionsField = owner.getClass().getDeclaredField("options");
			optionsField.setAccessible(true);

			final List<OptionHandler<?>> options = (List<OptionHandler<?>>) optionsField.get(owner);
			Collections.sort(options, new Comparator<OptionHandler<?>>() {
				@Override
				public int compare(OptionHandler<?> o1, OptionHandler<?> o2) {
					return o1.option.toString().compareTo(o2.option.toString());
				}
			});		        
		} catch (Exception e) {
			throw new CmdLineException(owner, "", e);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void setObjectField(Object bean, Field field, Object obj) throws IllegalArgumentException, IllegalAccessException {
		//test and deal with new style
		try {
			Field newoptsfield = getDeclaredField(bean.getClass(), field.getName() + "Op");
			newoptsfield.setAccessible(true);
			Object o = newoptsfield.get(bean);

			if (Collection.class.isAssignableFrom(newoptsfield.getType())) {
				if (o == null) {
					o = new ArrayList();
					newoptsfield.set(bean, o);
				}
				((Collection)o).add(obj);
			} else {
				newoptsfield.set(bean, obj);
			}
			//newoptsfield.set(bean, value)
		} catch (NoSuchFieldException nsfe) {
			//nsfe.printStackTrace();
		}
	}

	protected Field getDeclaredField(Class<?> clz, String name) throws NoSuchFieldException {
		try {
			return clz.getDeclaredField(name);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchFieldException e) {
			if (clz.getSuperclass() != null)
				return getDeclaredField(clz.getSuperclass(), name);
			throw e;
		}
	}

	@SuppressWarnings("rawtypes")
	private class SetterWrapper implements Setter {
		Setter setter;
		boolean used = false;

		SetterWrapper(Setter setter) {
			this.setter = setter;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void addValue(Object value) throws CmdLineException {
			used = true;
			setter.addValue(value);
		}

		@Override
		public Class getType() {
			return setter.getType();
		}

		@Override
		public boolean isMultiValued() {
			return setter.isMultiValued();
		}
	}

	private void addOptions(Object bean, CmdLineParser parser) {
		// recursively process all the methods/fields.
		for (Class<?> c=bean.getClass(); c!=null; c=c.getSuperclass()) {
			for (Method m : c.getDeclaredMethods()) {
				Option o = m.getAnnotation(Option.class);
				if(o!=null) {
					parser.addOption(new SetterWrapper(new MethodSetter(parser,bean,m)), o);
				}
				Argument a = m.getAnnotation(Argument.class);
				if(a!=null) {
					parser.addArgument(new SetterWrapper(new MethodSetter(parser,bean,m)), a);
				}
			}

			for( Field f : c.getDeclaredFields() ) {
				Option o = f.getAnnotation(Option.class);
				if(o!=null) {
					parser.addOption(new SetterWrapper(Setters.create(f,bean)),o);
				}
				Argument a = f.getAnnotation(Argument.class);
				if(a!=null) {
					parser.addArgument(new SetterWrapper(Setters.create(f,bean)), a);
				}
			}
		}
	}

	private void removeOptions(Object bean, CmdLineParser parser) throws CmdLineException {
		if (bean == null) return;

		// recursively process all the methods/fields.
		for (Class<?> c=bean.getClass(); c!=null; c=c.getSuperclass()) {
			for (Method m : c.getDeclaredMethods()) {
				Option o = m.getAnnotation(Option.class);
				if(o!=null) {
					removeOption(parser, o);

					//TODO: handle recursive removal
				}
				Argument a = m.getAnnotation(Argument.class);
				if(a!=null) {
					removeArgument(parser, a);
				}
			}

			for( Field f : c.getDeclaredFields() ) {
				Option o = f.getAnnotation(Option.class);
				if(o!=null) {
					removeOption(parser, o);

					try {
						f.setAccessible(true);
						Object val = f.get(bean);
						if (val instanceof CmdLineOptionsProvider)
							removeOptions(((CmdLineOptionsProvider)val).getOptions(), parser);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				Argument a = f.getAnnotation(Argument.class);
				if(a!=null) {
					removeArgument(parser, a);
				}
			}
		}
	}

	private void removeArgument(CmdLineParser parser, Argument a) throws CmdLineException {
		try {
			Field argsField = CmdLineParser.class.getDeclaredField("arguments");
			argsField.setAccessible(true);

			List<?> args = (List<?>) argsField.get(parser);
			OptionHandler<?> op = (OptionHandler<?>) args.get(a.index());

			if (op.setter instanceof SetterWrapper && ((SetterWrapper)op.setter).used) {
				throw new CmdLineException(parser, "The use of the argument " + op.option.metaVar() + " is shaded by another argument");
			}

			args.set(a.index(), null);
		} catch (CmdLineException e) {
			throw e;
		} catch (Exception e) {
			throw new CmdLineException(parser, "", e);
		}
	}

	private void removeOption(CmdLineParser parser, Option o) throws CmdLineException {
		try {
			Method find = CmdLineParser.class.getDeclaredMethod("findOptionHandler", String.class);
			find.setAccessible(true);

			OptionHandler<?> op = (OptionHandler<?>) find.invoke(parser, o.name());

			if (op.setter instanceof SetterWrapper && ((SetterWrapper)op.setter).used) {
				throw new CmdLineException(parser, "The use of the option " + op.option + " is shaded by another option");
			}

			Field optionsField = CmdLineParser.class.getDeclaredField("options");
			optionsField.setAccessible(true);

			List<?> options = (List<?>) optionsField.get(parser);
			options.remove(op);
		} catch (CmdLineException e) {
			throw e;
		} catch (Exception e) {
			throw new CmdLineException(parser, "", e);
		} 
	}
}
