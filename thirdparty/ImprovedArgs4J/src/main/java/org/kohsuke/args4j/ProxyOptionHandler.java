package org.kohsuke.args4j;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

/**
 * The {@link ProxyOptionHandler} allows options to have associated options.
 * For example, an enum option might have different options depending
 * of its value.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class ProxyOptionHandler extends OptionHandler<Object> {
	OptionHandler<?> proxy;
	
	/**
	 * Default constructor.
	 * @param parser the parser
	 * @param option the option definition
	 * @param setter the setter
	 */
	public ProxyOptionHandler(CmdLineParser parser, OptionDef option, Setter<? super Object> setter) {
		super(parser, option, setter);
		
		OptionDef proxyOption = new OptionDef(option.usage(), option.metaVar(), option.required(), OptionHandler.class, option.isMultiValued()); 
		proxy = parser.createOptionHandler(proxyOption, setter);
	}

	@Override
	public String getDefaultMetaVariable() {
		return proxy.getDefaultMetaVariable();
	}

	@SuppressWarnings("unchecked")
	@Override
	public int parseArguments(Parameters params) throws CmdLineException {
		int val = proxy.parseArguments(params);
		
		if (CmdLineOptionsProvider.class.isAssignableFrom(this.setter.getType())) {
			try {
				Class<?> type = setter.getClass();
				
				Field beanField = type.getDeclaredField("bean");
				beanField.setAccessible(true);
				Object bean = beanField.get(setter);
				
				Field field = type.getDeclaredField("f");
				field.setAccessible(true);
				field = (Field) field.get(setter);

				//the actual value being set:
				Object object = field.get(bean);
				
				if(object instanceof ArrayList) {
					Object obj = ((ArrayList<CmdLineOptionsProvider>) object).get(((ArrayList<CmdLineOptionsProvider>) object).size()-1).getOptions();
						
					new ClassParser().parse(obj, owner);
					setObjectField(bean, field, obj);
				}
				else
				{
					Object obj = ((CmdLineOptionsProvider)object).getOptions();
					
					if (obj instanceof Enum)
						System.err.println("Warning: Using an enum as an options object is not recommended and will be disallowed in the near future!");
					
					new ClassParser().parse(obj, owner);				
					setObjectField(bean, field, obj);
				}
				
				// for display purposes, we like the arguments in argument order, but the options in alphabetical order
				Field optionsField = owner.getClass().getDeclaredField("options");
				optionsField.setAccessible(true);
				final List<OptionHandler<?>> options = (List<OptionHandler<?>>) optionsField.get(owner);
		        Collections.sort(options, new Comparator<OptionHandler<?>>() {
					public int compare(OptionHandler<?> o1, OptionHandler<?> o2) {
						return o1.option.toString().compareTo(o2.option.toString());
					}
				});		        
			} catch (SecurityException e) {
				System.err.println("Not allowed");
			} catch (NoSuchFieldException e) {
				System.err.println("Oops, an error has occurred - something inside args4j has probably changed");
				System.exit(0);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				System.err.println("Not allowed");
			}
		}
		
		return val;
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
        	nsfe.printStackTrace();
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
}
