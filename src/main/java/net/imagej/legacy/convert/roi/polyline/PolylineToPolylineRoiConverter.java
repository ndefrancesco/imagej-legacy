/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2018 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package net.imagej.legacy.convert.roi.polyline;

import ij.gui.PolygonRoi;
import ij.gui.Roi;

import net.imagej.legacy.convert.roi.AbstractMaskPredicateToRoiConverter;
import net.imglib2.RealLocalizable;
import net.imglib2.roi.geom.real.Polyline;

import org.scijava.Priority;
import org.scijava.convert.Converter;
import org.scijava.plugin.Plugin;

/**
 * Converts an {@link Polyline} to an {@link PolygonRoi} of type POLYLINE. This
 * converter may be lossy, since {@code PolygonRoi}s store vertices as
 * {@code float}s.
 *
 * @author Alison Walter
 */
@Plugin(type = Converter.class, priority = Priority.LOW)
public class PolylineToPolylineRoiConverter extends
	AbstractMaskPredicateToRoiConverter<Polyline, PolygonRoi>
{

	@Override
	public Class<PolygonRoi> getOutputType() {
		return PolygonRoi.class;
	}

	@Override
	public Class<Polyline> getInputType() {
		return Polyline.class;
	}

	@Override
	public PolygonRoi convert(final Polyline mask) {
		final float[] x = new float[mask.numVertices()];
		final float[] y = new float[mask.numVertices()];
		for (int i = 0; i < mask.numVertices(); i++) {
			final RealLocalizable v = mask.vertex(i);
			x[i] = (float) v.getDoublePosition(0);
			y[i] = (float) v.getDoublePosition(1);
		}
		return new PolygonRoi(x, y, mask.numVertices(), Roi.POLYLINE);
	}

	@Override
	public boolean isLossy() {
		return true;
	}
}