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
