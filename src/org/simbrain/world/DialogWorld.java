/*
 * Part of Simbrain--a java-based neural network kit
 * Copyright (C) 2003 Jeff Yoshimi <www.jeffyoshimi.net>
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.simbrain.world;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.simbrain.util.LabelledItemPanel;
import org.simbrain.util.StandardDialog;


	
/**
 * <b>DialogWorld</b> is used to set the enivronment's parameters, 
 * in particular, the way stimuli are constructed to be sent the network, and the way 
 * outputs from the network are expressed in the world.
 */
public class DialogWorld extends StandardDialog implements ActionListener {


	private World theWorld;
	private LabelledItemPanel myContentPane = new LabelledItemPanel();
	private JTextField movementIncrement = new JTextField();
	private JRadioButton useLocalBounds = new JRadioButton();
	private JRadioButton updateDrag = new JRadioButton();
	
		
	public DialogWorld(World wp)
	{
		theWorld = wp;
		init();
	}

	/**
	 * This method initialises the components on the panel.
	 */
	private void init()
	{
	   setTitle("World Dialog");
		
	   fillFieldValues();
	 
	   movementIncrement.setColumns(3);
	   myContentPane.addItem("Enable local boundaries", useLocalBounds);		 
	   myContentPane.addItem("Update network while dragging objects", updateDrag);		 
	   myContentPane.addItem("Set movement increment", movementIncrement);	 

	   setContentPane(myContentPane);


	}
	 
	/**
	* Populate fields with current data
	*/
   public void fillFieldValues() {
   	// TODO fix this
   	  // movementIncrement.setText(Integer.toString(theWorld.getCreature().getAbsoluteMovementIncrement()));
   	   updateDrag.setSelected(theWorld.isUpdateWhileDragging());
   	   useLocalBounds.setSelected(theWorld.getLocalBounds());
   	   
//   	   if(theWorld.isFollowMode() == false) {
//   	   	
//   	   }
   	   
	}
	 
   /**
   * Set projector values based on fields 
   */
  public void getValues() {
  	 	//TODO: Fix this
  		//theWorld.getCreature().setAbsoluteMovementIncrement(Integer.parseInt(movementIncrement.getText()));
		theWorld.setBounds(useLocalBounds.isSelected());
		theWorld.setUpdateWhileDragging(updateDrag.isSelected());
  }

  public void actionPerformed(ActionEvent e) {
  }
}
