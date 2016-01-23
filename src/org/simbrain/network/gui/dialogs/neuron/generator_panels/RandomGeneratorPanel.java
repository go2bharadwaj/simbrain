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
package org.simbrain.network.gui.dialogs.neuron.generator_panels;

import java.util.List;

import org.simbrain.network.core.Neuron;
import org.simbrain.network.core.NeuronUpdateRule;
import org.simbrain.network.gui.dialogs.neuron.AbstractNeuronRulePanel;
import org.simbrain.network.gui.dialogs.neuron.NoiseGeneratorPanel;
import org.simbrain.network.neuron_update_rules.activity_generators.RandomNeuronRule;

/**
 * <b>RandomNeuronPanel</b> Currently unimplemented pending decisions about
 * changing random neurons into "input generators".
 */
public class RandomGeneratorPanel extends AbstractNeuronRulePanel {

    /** A reference to the neuron update rule being edited. */
    private static final RandomNeuronRule prototypeRule = new RandomNeuronRule();

    /**
     * Creates an instance of this panel.
     *
     */
    public RandomGeneratorPanel() {
        super();
        noisePanel = new NoiseGeneratorPanel();
        this.add(noisePanel);
    }

    @Override
    protected NeuronUpdateRule getPrototypeRule() {
        return prototypeRule;
    }

    @Override
    public void fillDefaultValues() {        
    }

    @Override
    public void commitChanges(Neuron neuron) {
        
    }

    @Override
    protected void writeValuesToRules(List<Neuron> neurons) {
    }
}
