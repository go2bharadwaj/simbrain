/*
 * Part of Simbrain--a java-based neural network kit
 * Copyright (C) 2005,2007 The Authors.  See http://www.simbrain.net/credits
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
package org.simbrain.world.dataworld;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.simbrain.workspace.CouplingContainer;
import org.simbrain.workspace.WorkspaceComponent;
import org.simbrain.workspace.gui.DesktopComponent;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;


/**
 * <b>DataWorldComponent</b> is a "spreadsheet world" used to send rows of raw data to input nodes.
 */
public class DataWorldDesktopComponent extends DesktopComponent implements ActionListener, MenuListener {

    /** World scroll pane. */
    private JScrollPane worldScroller = new JScrollPane();

    /** Data world. */
    private DataWorld world;

    /** Menu bar. */
    private JMenuBar mb = new JMenuBar();

    /** File menu. */
    private JMenu file = new JMenu("File  ");

    /** Open menu item. */
    private JMenuItem open = new JMenuItem("Open");

    /** Save menu item. */
    private JMenuItem save = new JMenuItem("Save");

    /** Save as menu item. */
    private JMenuItem saveAs = new JMenuItem("Save as");

    /** Close menu item. */
    private JMenuItem close = new JMenuItem("Close");

    /** Edit menu item. */
    private JMenu edit = new JMenu("Edit");

    /** Add row menu item. */
    private JMenuItem addRow = new JMenuItem("Add a row");

    /** Add column menu item. */
    private JMenuItem addCol = new JMenuItem("Add a column");

    /** Zero fill menu item. */
    private JMenuItem zeroFill = new JMenuItem("ZeroFill the Table");

    /** Remove row menu item. */
    private JMenuItem remRow = new JMenuItem("Remove a row");

    /** Remove column menu item. */
    private JMenuItem remCol = new JMenuItem("Remove a column");

    /** Randomize menu item. */
    private JMenuItem randomize = new JMenuItem("Randomize");

    /** Random properties menu item. */
    private JMenuItem randomProps = new JMenuItem("Adjust Randomization Bounds");

    /** Determines whether table is in iteration mode. */
    private JCheckBoxMenuItem iterationMode = new JCheckBoxMenuItem("Iteration mode");

    /** Determines whether iteration mode uses last column. */
    private JCheckBoxMenuItem columnIteration = new JCheckBoxMenuItem("Use last column");

    /**
     * This method is the default constructor.
     */
    public DataWorldDesktopComponent(DataWorldComponent component) {
        super(component);
        init();
    }

    /**
     * Initilaizes items needed to create frame.
     */
    public void init() {
        this.checkIterationMode();
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add("Center", worldScroller);
        world = new DataWorld(this);
        addMenuBar(world);
        getContentPane().add(world.getTable().getTableHeader(), BorderLayout.PAGE_START);
        worldScroller.setViewportView(world);
        worldScroller.setEnabled(false);
        this.pack();
    }

    /**
     * Creates the Menu Bar and adds it to the frame.
     *
     * @param table Table to be used for world
     */
    public void addMenuBar(final DataWorld table) {
        open.addActionListener(this);
        open.setActionCommand("open");
        open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit
                .getDefaultToolkit().getMenuShortcutKeyMask()));
        save.addActionListener(this);
        save.setActionCommand("save");
        save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit
                .getDefaultToolkit().getMenuShortcutKeyMask()));
        saveAs.addActionListener(this);
        saveAs.setActionCommand("saveAs");
        close.addActionListener(this);
        close.setActionCommand("close");
        close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit
                .getDefaultToolkit().getMenuShortcutKeyMask()));
        mb.add(file);
        file.add(open);
        file.add(save);
        file.add(saveAs);
        file.add(close);
        file.addMenuListener(this);

        addRow.addActionListener(this);
        addRow.setActionCommand("addRow");
        addCol.addActionListener(this);
        addCol.setActionCommand("addCol");
        remRow.addActionListener(this);
        remRow.setActionCommand("remRow");
        remCol.addActionListener(this);
        remCol.setActionCommand("remCol");
        zeroFill.addActionListener(this);
        zeroFill.setActionCommand("zeroFill");
        randomize.addActionListener(this);
        randomize.setActionCommand("randomize");
        randomProps.addActionListener(this);
        randomProps.setActionCommand("randomProps");
        iterationMode.addActionListener(this);
        iterationMode.setActionCommand("iterationMode");
        columnIteration.addActionListener(this);
        columnIteration.setActionCommand("columnIteration");
        edit.add(addRow);
        edit.add(addCol);
        edit.add(zeroFill);
        edit.addSeparator();
        edit.add(remRow);
        edit.add(remCol);
        edit.addSeparator();
        edit.add(randomize);
        edit.add(randomProps);
        edit.addSeparator();
        edit.add(iterationMode);
        edit.add(columnIteration);
        mb.add(edit);

        setJMenuBar(mb);
    }

    /**
     * Checks iteration mode to enable or disable column iteration.
     */
    private void checkIterationMode() {
        if (iterationMode.getState()) {
            columnIteration.setEnabled(true);
        } else {
            columnIteration.setEnabled(false);
        }
    }

    /**
     * Read a world from a world-xml file.
     *
     * @param theFile the xml file containing world information
     */
    public void open(final File theFile) {
        setCurrentFile(theFile);
        FileReader reader;
        try {
            reader = new FileReader(theFile);
            TableModel model = (TableModel) getXStream().fromXML(reader);
            model.postOpenInit();
            world.setTableModel(model);
            pack();
            setStringReference(theFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a properly initialized xstream object.
     * @return the XStream object
     */
    private XStream getXStream() {
        XStream xstream = new XStream(new DomDriver());
        xstream.omitField(TableModel.class, "consumers");
        xstream.omitField(TableModel.class, "producers");
        xstream.omitField(TableModel.class, "couplingList");
        xstream.omitField(TableModel.class, "model");
        return xstream;
    }

    /**
     * Save a specified file.
     *
     * @param worldFile File to save world
     */
    public void save(final File worldFile) {
        setCurrentFile(worldFile);
        world.getTableModel().preSaveInit();
        String xml = getXStream().toXML(world.getTableModel());
        try {
            FileWriter writer  = new FileWriter(worldFile);
            writer.write(xml);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        setStringReference(worldFile);
        setChangedSinceLastSave(false);
    }

    /**
     * Responds to actions performed.
     *
     * @param e Action event
     */
    public void actionPerformed(final ActionEvent e) {
        if (e.getActionCommand().equals("open")) {
            showOpenFileDialog();
        } else if (e.getActionCommand().equals("save")) {
            save();
        } else if (e.getActionCommand().equals("saveAs")) {
            showSaveFileDialog();
        } else if (e.getActionCommand().equals("addRow")) {
            this.getWorld().getTableModel().getModel().addRow(this.getWorld().getTableModel().newRow());
            this.setChangedSinceLastSave(true);
            pack();
        } else if (e.getActionCommand().equals("addRowHere")) {
            if (
                this.getWorld().getSelectedPoint().x < (this.getWorld()
                    .getTable().getRowHeight() * this.getWorld().getTable()
                    .getRowCount())) {
                this.getWorld().getTableModel().getModel().insertRow(
                        this.getWorld().getTable().rowAtPoint(
                                this.getWorld().getSelectedPoint()),
                        this.getWorld().getTableModel().newRow());
            } else {
                this.getWorld().getTableModel().getModel().addRow(this.getWorld().getTableModel().newRow());
            }
            this.setChangedSinceLastSave(true);
            pack();
        } else if (e.getActionCommand().equals("addCol")) {
            this.getWorld().getTableModel().addColumn(Integer.toString(this.getWorld().getTableModel().getModel().getColumnCount()+1));
            this.getWorld().getTableModel().zeroFillNew();
            this.setChangedSinceLastSave(true);
            pack();
        } else if (e.getActionCommand().equals("addColHere")) {
            this.getWorld().getTableModel().addColumn(Integer.toString(this.getWorld().getTableModel().getModel().getColumnCount()+1));
            this.getWorld().getTableModel().zeroFillNew();
            this.setChangedSinceLastSave(true);
            pack();
        } else if (e.getActionCommand().equals("remRow")) {
            this.getWorld().getTableModel().getModel().removeRow(this.getWorld().getTable().getRowCount() - 1);
            this.setChangedSinceLastSave(true);
            pack();
        } else if (e.getActionCommand().equals("remRowHere")) {
            this.getWorld().getTableModel().getModel().removeRow(
                    this.getWorld().getTable().rowAtPoint(
                    this.getWorld().getSelectedPoint()));
            this.setChangedSinceLastSave(true);
            pack();
        } else if (e.getActionCommand().equals("remCol")) {
            this.getWorld().getTableModel().removeColumn(this.getWorld().getTableModel().getModel().getColumnCount() - 1);
            this.setChangedSinceLastSave(true);
            pack();
        } else if (e.getActionCommand().equals("remColHere")) {
            int col = this.getWorld().getTable().columnAtPoint(this.getWorld().getSelectedPoint());
            this.getWorld().getTableModel().removeColumn(col);
            this.setChangedSinceLastSave(true);
            pack();
        } else if (e.getActionCommand().equals("zeroFill")) {
            this.getWorld().getTableModel().zeroFill();
            this.setChangedSinceLastSave(true);
        } else if (e.getActionCommand().equals("randomize")) {
            world.getTableModel().randomize();
            this.setChangedSinceLastSave(true);
        } else if (e.getActionCommand().equals("randomProps")) {
            world.displayRandomizeDialog();
            this.setChangedSinceLastSave(true);
        } else if (e.getActionCommand().equals("iterationMode")) {
            world.getTableModel().setIterationMode(iterationMode.getState());
            checkIterationMode();
        } else if (e.getActionCommand().equals("columnIteration")) {
            world.getTableModel().setLastColumnBasedIteration(columnIteration.getState());
        }
    }

    /**
     * Menu selected event.
     *
     * @param e Menu event
     */
    public void menuSelected(final MenuEvent e) {
        if (e.getSource().equals(file)) {
            if (isChangedSinceLastSave()) {
                save.setEnabled(true);
            } else if (!isChangedSinceLastSave()) {
                save.setEnabled(false);
            }
        }
    }

    /**
     * Menu deselected event.
     *
     * @param e Menu event
     */
    public void menuDeselected(final MenuEvent e) {
    }

    /**
     * Menu canceled event.
     *
     * @param e Menu event
     */
    public void menuCanceled(final MenuEvent e) {
    }

    /**
     * @return the world
     */
    public DataWorld getWorld() {
        return world;
    }

    /**
     * @param world the world to set
     */
    public void setWorld(DataWorld world) {
        this.world = world;
    }

    @Override
    public String getFileExtension() {
       return "xml";
    }

    @Override
    public void setCurrentDirectory(final String currentDirectory) {        
        super.setCurrentDirectory(currentDirectory);
        DataWorldPreferences.setCurrentDirectory(currentDirectory);
    }

    @Override
    public String getCurrentDirectory() {
        return DataWorldPreferences.getCurrentDirectory();
    }

   /**
    * Returns reference to table model which contains couplings.
    */
   public CouplingContainer getCouplingContainer() {
       return this.getWorld().getTableModel();
   }

    @Override
    public void updateComponent() {
        this.getWorld().getTableModel().getModel().fireTableDataChanged();
        this.getWorld().completedInputRound();
        repaint();
    }

    @Override
    public void close() {
    }

    /** @see javax.swing.JFrame */
    public void pack() {
        super.pack();
        this.setMaximumSize(new Dimension((int) this.getMaximumSize().getWidth(), (int) this.getSize().getHeight()));
        repaint();
    }
}

