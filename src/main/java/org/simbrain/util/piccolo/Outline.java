package org.simbrain.util.piccolo;

import org.piccolo2d.PNode;
import org.piccolo2d.nodes.PPath;
import org.piccolo2d.util.PBounds;

import java.awt.*;
import java.util.Collection;

/**
 * A node that draws an outline around a set of nodes.
 */
public class Outline extends PNode {

    /**
     * The width and height of the arc in the rounded rectangle that surrounds the outlined objects.
     */
    public static final int ARC_SIZE = 20;

    /**
     * Margin in pixels around outlined object.
     */
    private int outlinePadding = 10;

    /**
     * The actual outline object.
     */
    private PPath outline = PPath.createRoundRectangle(0, 0, 0, 0, ARC_SIZE, ARC_SIZE);

    /**
     * Construct the outline object.
     */
    public Outline() {
        outline.setPaint(Color.gray);
        addChild(outline);
        outline.setVisible(true);
        setPickable(false);
    }

    /**
     * Update the bounds of the outlined objects, which are passed in as an argument.
     */
    public void update(Collection<? extends PNode> outlinedNodes) {
        PBounds bounds = new PBounds();
        for (PNode node : outlinedNodes) {
            bounds.add(node.getFullBoundsReference());
        }
        updateBound(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    private void updateBound(double x, double y, double width, double height) {
        removeChild(outline);
        outline = PPath.createRoundRectangle(x - outlinePadding,
                y - outlinePadding,
                width + outlinePadding * 2,
                height + outlinePadding * 2,
                ARC_SIZE,
                ARC_SIZE
        );
        outline.setStrokePaint(Color.gray);
        outline.setPaint(null);
        addChild(outline);
    }

    public int getOutlinePadding() {
        return outlinePadding;
    }

    public void setOutlinePadding(int outlinePadding) {
        this.outlinePadding = outlinePadding;
    }

}