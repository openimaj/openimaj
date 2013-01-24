package org.openimaj.demos.sandbox.video.gt;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;

/**
 * A replacement for the AWT ButtonGroup class.
 * 
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 17 Jul 2012
 * @version $Author$, $Revision$, $Date$
 */
public class ButtonGroup 
{
	/** The buttons */
	private final List<AbstractButton> buttons = new ArrayList<AbstractButton>();
	
	/** The listeners */
	private final List<ItemListener> listeners = new ArrayList<ItemListener>();

	/**
	 * Add a button
	 * 
	 * @param b
	 */
	public void add(final AbstractButton b) {
		this.buttons.add(b);
		b.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				ButtonGroup.this.updateButtons(b);
				ButtonGroup.this.fireListenerEvent( b );
			}
		});
	}

	/**
	 * Remove the given button from the group.
	 * 
	 * @param b The button to remove
	 */
	public void remove(final AbstractButton b) {
		this.buttons.remove(b);
	}

	/**
	 * Make sure only the given button is selected.
	 * 
	 * @param b The button to select.
	 */
	private void updateButtons(final AbstractButton b) {
		for (final AbstractButton button : this.buttons)
			button.setSelected(button == b);
	}

	/**
	 * Returns the selected button in the group.
	 * 
	 * @return The selected button in the group or null if no buttons are
	 *         selected.
	 */
	public AbstractButton getSelected() {
		for (final AbstractButton button : this.buttons)
			if (button.isSelected())
				return button;
		return null;
	}

	/**
	 * Sets all buttons in the group to unselected.
	 */
	public void selectNone() {
		for (final AbstractButton button : this.buttons)
			button.setSelected(false);
	}

	/**
	 * Set the selected button to the given one. Note that this method will
	 * select the button whether or not the button is in the button group.
	 * 
	 * @param b The button to select
	 */
	public void setSelected(final AbstractButton b) {
		b.setSelected(true);
		this.updateButtons(b);
	}
	
	/**
	 * 	Add the given item listener to listen for button change events from
	 * 	this button group.
	 *	@param il the item listener
	 */
	public void addItemListener( final ItemListener il )
	{
		this.listeners.add( il );
	}
	
	/**
	 * 	Remove the given item listener from this button group.
	 *	@param il The item listener to remove.
	 */
	public void removeItemListener( final ItemListener il )
	{
		this.listeners.remove( il );
	}
	
	/**
	 * 	Fire an itemStateChange event to the item listeners.
	 *	@param b The button that was selected
	 */
	protected void fireListenerEvent( final AbstractButton b )
	{
		if( b.isSelected() )
			for( final ItemListener il : this.listeners )
			{
				final ItemEvent ie = new ItemEvent( b, b.hashCode(), b, 
						ItemEvent.SELECTED );
				il.itemStateChanged( ie );
			}
	}
	
	/**
	 * 	Removes all the buttons being controlled by this button group.
	 */
	public void clear()
	{
		this.buttons.clear();
	}
}
