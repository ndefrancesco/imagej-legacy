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

package net.imagej.legacy.convert;

import ij.ImagePlus;

import java.util.Collection;

import net.imagej.display.ImageDisplay;

import org.scijava.Priority;
import org.scijava.convert.Converter;
import org.scijava.object.ObjectService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * {@link Converter} implementation for converting {@link ImageDisplay} to an
 * {@link ImagePlus}.
 *
 * @author Curtis Rueden
 */
@Plugin(type = Converter.class, priority = Priority.LOW)
public class ImageDisplayToImagePlusConverter extends
	AbstractLegacyConverter<ImageDisplay, ImagePlus>
{

	@Parameter(required = false)
	private ObjectService objectService;

	// -- Converter methods --

	@Override
	public <T> T convert(final Object src, final Class<T> dest) {
		if (!legacyEnabled()) throw new UnsupportedOperationException();
		final ImageDisplay display = (ImageDisplay) src;
		final Object imp = legacyService.getImageMap().registerDisplay(display);
		@SuppressWarnings("unchecked")
		final T typedImp = (T) imp;
		return typedImp;
	}

	@Override
	public void populateInputCandidates(final Collection<Object> objects) {
		if (objectService == null) return;
		objects.addAll(objectService.getObjects(ImageDisplay.class));
	}

	@Override
	public Class<ImagePlus> getOutputType() {
		return ImagePlus.class;
	}

	@Override
	public Class<ImageDisplay> getInputType() {
		return ImageDisplay.class;
	}
}
