
package org.simbrain.network.nodes;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.*;

import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.simbrain.coupling.Coupling;
import org.simbrain.coupling.CouplingMenuItem;
import org.simbrain.coupling.MotorCoupling;
import org.simbrain.coupling.SensoryCoupling;
import org.simbrain.network.NetworkPanel;
import org.simbrain.network.NetworkPreferences;
import org.simbrain.network.actions.ConnectNeuronsAction;
import org.simbrain.network.dialog.neuron.NeuronDialog;

import org.simnet.interfaces.*;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;

/**
 * <b>NeuronNode</b> is a Piccolo PNode corresponding to a Neuron in the neural network model.
 */
public class NeuronNode
    extends ScreenElement implements ActionListener {

    /** The logical neuron this screen element represents. */
    private Neuron neuron;

    /** Represents a coupling between this neuron and an external source of "sensory" input. */
    private SensoryCoupling sensoryCoupling;

    /** Represents a coupling between this neuron and an external source of "motor" output. */
    private MotorCoupling motorCoupling;

    /** Diameter of neuron. */
    private static final int DIAMETER = 24;

    /** Length of arrow. */
    private static final int ARROW_LINE = 20;

    /** Arrow associated with output node. */
    private PPath outArrow;

    /** Arrow associated with input node. */
    private PPath inArrow;

    /** Main circle of node. */
    private PNode circle;

    /** Color when neuron is maximally activated. */
    private float hotColor = NetworkPreferences.getHotColor();

    /** Color when neuron is minimally activated. */
    private float coolColor = NetworkPreferences.getCoolColor();

    /** A list of SynapseNodes connected to this NeuronNode; used for updating. */
    private HashSet connectedSynapses = new HashSet();

    /** Id reference to model neuron; used in persistence. */
    private String id;
    
    /**
     * Default constructor; used by Castor.
     */
    public NeuronNode(){
    }

    /**
     * Initialize a NeuronNode to a location. Used by Castor.
     *
     * @param x x coordinate of NeuronNode
     * @param y y coordinate of NeuronNode
     */
    public NeuronNode(final double x, final double y) {
        offset(x, y);
    }

    /**
     * Create a new neuron node.
     *
     * @param net Reference to NetworkPanel
     * @param neuron reference to model neuron
     * @param x initial x location of neuron
     * @param y initial y location of neuron
     */
    public NeuronNode(final NetworkPanel net, final Neuron neuron, final double x, final double y) {
        super(net);
        this.neuron = neuron;
        offset(x, y);
        init();
    }

    /** @see ScreenElement */
    public void initCastor(final NetworkPanel net) {
        super.initCastor(net);
        init();
    }

    /**
     * Initialize the NeuronNode.
     */
    private void init() {
        circle = PPath.createEllipse(0, 0, DIAMETER, DIAMETER);

        addChild(circle);

        // Handle input and output arrows
        outArrow = createOutArrow();
        inArrow = createInArrow();
        this.addChild(outArrow);
        this.addChild(inArrow);
        updateOutArrow();
        updateInArrow();

        update();

        setPickable(true);
        setChildrenPickable(false);

        addPropertyChangeListener(PROPERTY_FULL_BOUNDS, new SynapseNodePositionUpdater());

        // The main circle is what users select
        setBounds(circle.getBounds());
    }


    /** @see ScreenElement */
    protected boolean hasToolTipText() {
        return true;
    }

    /** @see ScreenElement */
    protected String getToolTipText() {
       return String.valueOf(neuron.getActivation());
    }

    /** @see ScreenElement */
    protected boolean hasContextMenu() {
        return true;
    }

    /**
     * Return the center of this node (the circle) in global coordinates.
     * @return the center point of this node.
     */
    public Point2D getCenter() {
        return circle.getGlobalBounds().getCenter2D();
    }

    /** @see ScreenElement */
    protected JPopupMenu getContextMenu() {

        JPopupMenu contextMenu = new JPopupMenu();

        // If neurons have been selected, create an acction which will connect selected neurons to this one
        if (getNetworkPanel().getSelectedNeurons() != null) {
            contextMenu.add(new ConnectNeuronsAction(getNetworkPanel(),
                    getNetworkPanel().getSelectedNeurons(), Collections.singleton(this)));
            contextMenu.addSeparator();
        }
        contextMenu.add(getNetworkPanel().getWorkspace().getMotorCommandMenu(this, this));
        contextMenu.add(getNetworkPanel().getWorkspace().getSensorIdMenu(this, this));

        return contextMenu;
    }

    /** @see ScreenElement */
    protected boolean hasPropertyDialog() {
        return true;
    }

    /** @see ScreenElement */
    protected JDialog getPropertyDialog() {
        NeuronDialog dialog = new NeuronDialog(this.getNetworkPanel().getSelectedNeurons());
        return dialog;
    }

    /**
     * Update the neuron view based on the model neuron.
     */
    public void update() {
        updateColor();
    }

    /**
     * Sets the color of this neuron based on its activation level.
     */
    private void updateColor() {
        double activation = neuron.getActivation();

        //Force to blank if 0
        if ((activation > -.1) && (activation < .1)) {
            circle.setPaint(Color.white);
        } else if (activation > 0) {
            float saturation = checkValid((float) Math.abs(activation / neuron.getUpperBound()));
            circle.setPaint(Color.getHSBColor(hotColor, saturation, (float) 1));
        } else if (activation < 0) {
            float saturation = checkValid((float) Math.abs(activation / neuron.getLowerBound()));
            circle.setPaint(Color.getHSBColor(coolColor, saturation, (float) 1));
        }

//        if (this.isSelected() == true) {
//            this.setPaint(parentPanel.getSelectionColor());
//        }
//
//        if (neuron instanceof SpikingNeuron) {
//            if (((SpikingNeuron) neuron).hasSpiked()) {
//                this.setStrokePaint(Color.YELLOW);
//                outArrow.setStrokePaint(Color.YELLOW);
//            } else {
//                this.setStrokePaint(parentPanel.getLineColor());
//                outArrow.setStrokePaint(parentPanel.getLineColor());
//            }
//        }
    }

    /**
     * Check whether the specified saturation is valid or not.
     *
     * @param val the saturation value to check.
     * @return whether it is valid or not.
     */
    private float checkValid(final float val) {
        float tempval = val;

        if (val > 1) {
            tempval = 1;
        }

        if (val < 0) {
            tempval = 0;
        }

        return tempval;
    }

    /**
     * Change the type of neuron this pnode is associated with It is assumed that the basic properties of the new
     * neuron have been set.
     *
     * @param newNeuron the neuron to change to
     */
    public void changeNeuron(final Neuron newNeuron) {
        Network.changeNeuron(neuron, newNeuron);
    }

    /**
     * Return true if this neuron has a sensory coupling attached.
     *
     * @return true if this neuron has a sensory coupling attached
     */
    public boolean isInput() {
        return (sensoryCoupling != null);
    }

    /**
     * Creates an arrow which designates an on-screen neuron as an output node, which sends signals to an external
     * environment (the world object).
     *
     * @return an object representing the input arrow of a PNodeNeuron
     *
     * @see org.simbrain.sim.world
     */
    private PPath createOutArrow() {
        PPath path = new PPath();
        GeneralPath arrow = new GeneralPath();
        Point2D p = this.globalToLocal(this.getOffset());
        float cx = (float) p.getX() + DIAMETER/2;
        float cy = (float) p.getY() + DIAMETER/2;

        arrow.moveTo(cx, cy - DIAMETER/2);
        arrow.lineTo(cx, cy - DIAMETER/2 - ARROW_LINE);

        arrow.moveTo(cx, cy - DIAMETER/2 - ARROW_LINE);
        arrow.lineTo(cx - DIAMETER/4, cy - DIAMETER);

        arrow.moveTo(cx, cy - DIAMETER/2 - ARROW_LINE);
        arrow.lineTo(cx + DIAMETER/4, cy - DIAMETER);

        path.append(arrow, true);
        return path;
    }

    /**
     * Creates an arrow which designates an on-screen neuron as an input node, which receives signals from an external
     * environment (the world object)
     *
     * @return an object representing the input arrow of a PNodeNeuron
     *
     * @see org.simbrain.sim.world
     */
    private PPath createInArrow() {
        PPath path = new PPath();
        GeneralPath arrow = new GeneralPath();
        Point2D p = this.globalToLocal(this.getOffset());
        float cx = (float) p.getX() + DIAMETER/2;
        float cy = (float) p.getY() + DIAMETER/2;
        float top = cy + DIAMETER/2 + 1;

        arrow.moveTo(cx, top);
        arrow.lineTo(cx, top + ARROW_LINE);

        arrow.moveTo(cx, top);
        arrow.lineTo(cx - DIAMETER/4 , cy + DIAMETER/2 + DIAMETER/4);

        arrow.moveTo(cx, top);
        arrow.lineTo(cx + DIAMETER/4 , top + DIAMETER/4);

        path.append(arrow, true);
        return path;
    }


    /**
     * Updates graphics depending on whether this is an output node or not.
     */
    public void updateOutArrow() {
        if (isOutput()) {
            outArrow.setVisible(true);
        } else {
            outArrow.setVisible(false);
        }
    }

    /**
     * Updates graphics depending on whether this is an ipnut node or not.
     */
    public void updateInArrow() {
        if (isInput()) {
            inArrow.setVisible(true);
        } else {
            inArrow.setVisible(false);
        }
    }

    /**
     * Returns String representation of this NeuronNode.
     *
     * @return String representation of this node.
     */
    public String toString() {
        String ret = new String();
        ret += "NeuronNode: (" + this.getGlobalFullBounds().x + ")(" + getGlobalFullBounds().y + ")\n";
        return ret;
    }

    /**
     * Return true if this neuron has a motor coupling attached.
     *
     * @return true if this neuron has a motor coupling attached
     */
    public boolean isOutput() {
        return (motorCoupling != null);
    }

    /**
     * Return the neuron for this neuron node.
     *
     * @return the neuron for this neuron node
     */
    public Neuron getNeuron() {
        return neuron;
    }

    /**
     * Set the neuron for this neuron node to <code>neuron</code>.
     *
     * <p>This is a bound property.</p>
     *
     * @param neuron neuron for this neuron node
     */
    public void setNeuron(final Neuron neuron) {

        Neuron oldNeuron = this.neuron;
        this.neuron = neuron;
        firePropertyChange("neuron", oldNeuron, neuron);
    }


    /**
     * @return Returns the motorCoupling.
     */
    public MotorCoupling getMotorCoupling() {
        return motorCoupling;
    }


    /**
     * @param motorCoupling The motorCoupling to set.
     */
    public void setMotorCoupling(final MotorCoupling motorCoupling) {
        this.motorCoupling = motorCoupling;
    }


    /**
     * @return Returns the sensoryCoupling.
     */
    public SensoryCoupling getSensoryCoupling() {
        return sensoryCoupling;
    }


    /**
     * @param sensoryCoupling The sensoryCoupling to set.
     */
    public void setSensoryCoupling(final SensoryCoupling sensoryCoupling) {
        this.sensoryCoupling = sensoryCoupling;
    }

    /** @see ScreenElement */
    protected JPopupMenu createContextMenu() {
        return getContextMenu();
    }


    /**
     * @see ActionPerformed
     */
    public void actionPerformed(final ActionEvent e) {
        // Handle pop-up menu events
        Object o = e.getSource();

        if (o instanceof JMenuItem) {
            JMenuItem m = (JMenuItem) o;

            String st = m.getActionCommand();

            // Sensory and Motor Couplings
            if (m instanceof CouplingMenuItem) {
                CouplingMenuItem cmi = (CouplingMenuItem) m;
                Coupling coupling = cmi.getCoupling();

                if (coupling instanceof MotorCoupling) {
                    ((MotorCoupling) coupling).setNeuron(this);
                    this.setMotorCoupling((MotorCoupling) coupling);
                } else if (coupling instanceof SensoryCoupling) {
                    ((SensoryCoupling) coupling).setNeuron(this);
                    this.setSensoryCoupling((SensoryCoupling) coupling);
                }
            }

           if (st.equals("Not output")) {
                motorCoupling = null;
            } else if (st.equals("Not input")) {
                sensoryCoupling = null;
            }

           updateInArrow();
           updateOutArrow();
       }
    }


    /**
     * @return Returns the dIAMETER.
     */
    public static int getDIAMETER() {
        return DIAMETER;
    }

    public Set getConnectedSynapses() {
        // TODO:
        // may want to make this set unmodifiable
        return connectedSynapses;
    }

    /**
     * Update connected synapse node positions.
     */
    private void updateSynapseNodePositions() {

        for (Iterator i = connectedSynapses.iterator(); i.hasNext();) {
            SynapseNode synapseNode = (SynapseNode) i.next();
            synapseNode.updatePosition();
        }
    }

    /**
     * Synapse node position updater, called in response
     * to changes in this neuron node's fullBounds property.
     */
    private class SynapseNodePositionUpdater
        implements PropertyChangeListener {

        /** @see PropertyChangeListener */
        public void propertyChange(final PropertyChangeEvent event) {
            updateSynapseNodePositions();
        }
    }

    /**
     * @return Returns the id.
     */
    public String getId() {
        return "pnode_" + neuron.getId();
    }

    /**
     * @param id The id to set.
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * @return Returns the xpos.
     */
    public double getXpos() {
        return this.getGlobalBounds().getX();
    }

    /**
     * @param xpos The xpos to set.
     */
    public void setXpos(final double xpos) {
        Point2D p = new Point2D.Double(xpos, getYpos());
        globalToLocal(p);
        this.setBounds(p.getX(), p.getY(), this.getWidth(), this.getHeight());
    }

    /**
     * @return Returns the ypos.
     */
    public double getYpos() {
        return this.getGlobalBounds().getY();
    }

    /**
     * @param ypos The ypos to set.
     */
    public void setYpos(final double ypos) {
        Point2D p = new Point2D.Double(getXpos(), ypos);
        globalToLocal(p);
        this.setBounds(p.getX(), p.getY(), this.getWidth(), this.getHeight());
    }
}