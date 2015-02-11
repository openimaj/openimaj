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
/**
 *
 */
package org.openimaj.demos;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.io.IOUtils;
import org.openimaj.util.processes.JavaProcess;
import org.openimaj.util.processes.ProcessException;
import org.reflections.Reflections;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.simple.extend.XhtmlNamespaceHandler;

import com.uwyn.jhighlight.renderer.JavaXhtmlRenderer;

/**
 * This class provides a means for listing and running demos that have been
 * automatically scanned from the classpath. The class looks for types that are
 * annotated with the {@link Demo} annotation and displays these in a list. The
 * source code should be available as a resource to this class, so that the
 * source can be loaded and displayed.
 * <p>
 * Icons for demos should be resized so that they are 32x32 pixels and they
 * should ideally be transparent PNGs.
 * <p>
 * Screenshots should be PNGs and should be resized to be 250 pixels wide.
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 2nd November 2011
 */
public class Demos {
	/**
	 * The location of the source code on the web. The class will look for the
	 * source code at this location if it cannot find it on disk.
	 */
	public final static String OPENIMAJ_SRC_URL =
			"http://svn.code.sf.net/p/openimaj/code/trunk/demos/demos/src/main/java/";

	/**
	 * This is the display for the demo runner.
	 *
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * @created 2nd November 2011
	 */
	protected class DemoRunnerPanel extends JPanel {
		/** */
		private static final long serialVersionUID = 1L;

		private JTabbedPane demoTabs = new JTabbedPane();
		private Map<DemoPackage, JList<DemoObject>> demoTabMap =
				new HashMap<DemoPackage, JList<DemoObject>>();
		private Map<DemoPackage, Vector<DemoObject>> demos =
				new HashMap<DemoPackage, Vector<DemoObject>>();
		private JLabel demoTitle = new JLabel();
		private JLabel demoAuthor = new JLabel();
		private JLabel demoScreen = new JLabel();
		private JLabel demoDescription = new JLabel();
		private JButton demoRunButton = new JButton("Run Demo");
		private JButton demoSourceButton = new JButton("See Source Code");

		private DemoObject lastSelectedDemo = null;

		/**
		 * Default constructor that sets up the display
		 */
		public DemoRunnerPanel() {
			setLayout(new GridBagLayout());
			setBackground(Color.white);

			final GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = gbc.gridy = 0;
			gbc.gridwidth = 1;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = gbc.weighty = 1;

			// Set up a banner with the OpenIMAJ logo
			final JLabel openImajLogo = new JLabel(new ImageIcon(
					Demos.class.getResource("/org/openimaj/demos/OpenIMAJ.png")));
			gbc.gridx = 1;
			gbc.weighty = 0;
			add(openImajLogo, gbc);

			// Set up the list of demos down the left-hand side of the display
			gbc.gridx = 0;
			gbc.gridy++;
			final int y = gbc.gridy;
			gbc.weighty = 1;
			gbc.insets = new Insets(8, 8, 8, 8);
			add(demoTabs, gbc);

			// Set up the panel down the right-hand side that displays
			// information about each demo.
			final JPanel p = new JPanel(new GridBagLayout());
			p.setOpaque(false);
			p.setPreferredSize(new Dimension(250, 600));
			p.setMaximumSize(new Dimension(250, 6000));
			p.setMinimumSize(new Dimension(250, 100));
			gbc.weighty = 0;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.insets = new Insets(0, 0, 0, 0);
			p.add(demoTitle, gbc);
			gbc.gridy++;
			p.add(demoAuthor, gbc);
			gbc.gridy++;
			gbc.insets = new Insets(6, 0, 6, 0);
			p.add(demoDescription, gbc);
			gbc.gridy++;
			p.add(demoScreen, gbc);
			gbc.gridy++;
			gbc.insets = new Insets(1, 0, 1, 0);
			p.add(demoSourceButton, gbc);
			gbc.gridy++;
			p.add(demoRunButton, gbc);

			// Add a padding panel to the right-hand panel, so that the
			// text does spread out over the panel in an ugly way.
			gbc.weighty = 1;
			gbc.gridy++;
			final JPanel paddingPanel = new JPanel();
			paddingPanel.setOpaque(false);
			p.add(paddingPanel, gbc);

			gbc.gridx++;
			gbc.gridy = y;
			gbc.weighty = 1;
			gbc.weightx = 0;
			gbc.insets = new Insets(8, 8, 8, 8);
			add(p, gbc);

			demoTabs.addChangeListener(new ChangeListener()
			{
				@Override
				public void stateChanged(ChangeEvent e)
				{
					@SuppressWarnings("unchecked")
					final JList<DemoObject> d = (JList<DemoObject>) ((JScrollPane) demoTabs.getSelectedComponent())
					.getViewport().getComponent(0);
					if (d.getSelectedValue() != null)
						updateDisplay(d.getSelectedValue());
				}
			});

			demoTitle.setFont(new Font("Arial", Font.BOLD, 14));
			demoDescription.setFont(new Font("Arial", Font.PLAIN, 10));

			// When run button is pressed, run the selected demo
			demoRunButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					try
					{
						final DemoObject obj = lastSelectedDemo;
						if (obj != null)
						{
							// runDemo( obj.demoClass, obj.annotation );
							runDemoNewJVM(obj.demoClass, obj.annotation);
						}
					}
					catch (final Exception e1)
					{
						e1.printStackTrace();
					}
				}
			});
			demoRunButton.setEnabled(false);

			// When the source button is pressed, display the source of the
			// selected demo.
			demoSourceButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					try
					{
						final DemoObject obj = lastSelectedDemo;

						// Get the resource where the source code is stored
						final String resource = "/" + obj.demoClass.getCanonicalName().
								replace(".", "/") + ".java";
						System.out.println(resource);

						// Read the source code from the resource
						InputStream stream = Demos.class.getResourceAsStream(resource);

						// If the stream is null it means the code isn't
						// available.
						// If that happens, we should try to read the source
						// from
						// the svn on openimaj.org.
						if (stream == null)
						{
							final URL u = new URL(OPENIMAJ_SRC_URL + resource);
							stream = u.openStream();
						}

						final String source = IOUtils.toString(stream, "ISO-8859-1");
						// "<html><head></head><body><h1>Hello</h1></body></html>";

						stream.close();

						// Syntax highlight the source code in XHTML
						final JavaXhtmlRenderer r = new JavaXhtmlRenderer();
						final String h = r.highlight(obj.demoClass.getSimpleName(),
								source, "ISO-8859-1", false);

						// Render the XHTML to an XHTML panel
						final XHTMLPanel p = new XHTMLPanel();
						p.setDocumentFromString(h, resource, new XhtmlNamespaceHandler());

						// Stick the XHTMLPanel in a frame
						final JFrame f = new JFrame();
						f.setSize(800, 600);
						f.setLocationRelativeTo(null);
						f.getContentPane().add(new JScrollPane(p));
						f.setVisible(true);
					}
					catch (final IOException e1)
					{
						e1.printStackTrace();
					}
				}
			});
			demoSourceButton.setEnabled(false);
		}

		/**
		 * Updates the information display of the demo.
		 *
		 * @param dObj
		 *            The {@link DemoObject} for the selected demo
		 */
		private void updateDisplay(DemoObject dObj) {
			demoTitle.setText(dObj.annotation.title());
			demoDescription.setText("<html><p>" + dObj.annotation.description() + "</p></html>");
			demoAuthor.setText("By " + dObj.annotation.author());

			if (dObj.annotation.screenshot() != null)
				demoScreen.setIcon(new ImageIcon(
						Demos.class.getResource(dObj.annotation.screenshot())));
			else
				demoScreen.setIcon(null);
		}

		/**
		 * Add a demo to the list
		 *
		 * @param obj
		 *            The object representing the demo
		 */
		public void addDemo(DemoObject obj) {
			JList<DemoObject> d = demoTabMap.get(obj.pkg);

			if (d == null) {
				final JList<DemoObject> r = new JList<DemoObject>();
				demoTabMap.put(obj.pkg, r);
				d = r;

				// When the list is clicked upon, update the demo information
				d.addListSelectionListener(new ListSelectionListener()
				{
					@Override
					public void valueChanged(ListSelectionEvent e)
					{
						demoRunButton.setEnabled(true);
						demoSourceButton.setEnabled(true);
						updateDisplay(lastSelectedDemo =
								r.getSelectedValue());
					}
				});
				d.setCellRenderer(new IconListRenderer());

				demoTabs.addTab(
						obj.pkg == null ? "Demos" : obj.pkg.title(),
								new JScrollPane(d));
			}

			Vector<DemoObject> dd = demos.get(obj.pkg);

			if (dd == null)
				demos.put(obj.pkg, dd = new Vector<Demos.DemoObject>());

			dd.add(obj);
			d.removeAll();
			d.setListData(dd);

			revalidate();
		}
	}

	/**
	 * Used for each demo in the list.
	 *
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * @created 2nd November 2011
	 */
	protected class DemoObject {
		public Demo annotation;
		public Class<?> demoClass;
		public DemoPackage pkg;

		public DemoObject(Class<?> c) {
			annotation = c.getAnnotation(Demo.class);
			demoClass = c;
			pkg = demoClass.getPackage().getAnnotation(DemoPackage.class);
		}

		@Override
		public String toString() {
			return annotation.title();
		}
	}

	/**
	 * A list renderer that adds an icon to the label.
	 *
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * @created 3rd November 2011
	 */
	protected class IconListRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = 1L;

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value,
				int index, boolean isSelected, boolean cellHasFocus)
		{
			final JLabel l = (JLabel) super.getListCellRendererComponent(
					list, value, index, isSelected, cellHasFocus);

			final DemoObject o = (DemoObject) value;
			if (o.annotation.icon() != null) {
				URL u = getClass().getResource(o.annotation.icon());
				if (u == null)
					u = getClass().getResource("/defaults/demo.png");
				l.setIcon(new ImageIcon(u));
			}

			return l;
		}
	}

	/** The panel that will be used to display the list of dmeos */
	private DemoRunnerPanel panel = new DemoRunnerPanel();

	/**
	 * Default constructor
	 */
	public Demos() {
		try {
			// Get a list of the available demos
			final Set<Class<?>> c = findDemos();

			// Add the demos to the list
			for (final Class<?> cc : c)
				panel.addDemo(new DemoObject(cc));

			// Show the menu
			final JFrame f = new JFrame();
			f.getContentPane().add(panel);
			f.setSize(800, 600);
			f.setLocationRelativeTo(null);
			f.setVisible(true);
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Finds the class files that have been annotated with the @Demo annotation.
	 */
	private Set<Class<?>> findDemos() {
		final Reflections reflections = new Reflections("org.openimaj.demos");
		return reflections.getTypesAnnotatedWith(Demo.class);
	}

	/**
	 * Given a demo class file, will instantiate the demo and run its main
	 * method.
	 *
	 * @param clazz
	 *            The demo class file
	 */
	@SuppressWarnings("unused")
	private void runDemo(Class<?> clazz, Demo annotation) throws Exception {
		try {
			final Method main = clazz.getDeclaredMethod("main", String[].class);
			System.out.println(main);
			main.invoke(null, (Object) annotation.arguments());
		} catch (final Throwable t) {
			final String msg = String.format("Unexpected problem: %s",
					getStackTrace(t.getCause()));
			JOptionPane.showMessageDialog(null, msg);
			throw new Exception(t);
		}
	}

	/**
	 * Given a demo class file, instantiate the demo and run its main method in
	 * a new JVM
	 *
	 * @param clazz
	 *            The demo class file
	 */
	private void runDemoNewJVM(final Class<?> clazz, Demo annotation) throws Exception {
		final String[] jvmArgs = annotation.vmArguments();
		final String[] appArgs = annotation.arguments();

		new Thread() {
			@Override
			public void run() {
				try {
					JavaProcess.runProcess(clazz, jvmArgs, appArgs);
				} catch (final ProcessException e) {
					e.printStackTrace();
				}
			};
		}.start();
	}

	/**
	 * Returns a string of a stack trace.
	 *
	 * @param aThrowable
	 *            The throwable to get a string for
	 * @return The throwable's stack as a string
	 */
	private static String getStackTrace(Throwable aThrowable) {
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		aThrowable.printStackTrace(printWriter);
		return result.toString().substring(0, 1024) + "...";
	}

	/**
	 * Default main just starts the demo system.
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		new Demos();
	}
}
