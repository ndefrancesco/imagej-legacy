/*
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2022 ImageJ2 developers.
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
import net.imagej.Dataset;
import net.imglib2.util.Cast;
import org.scijava.convert.AbstractConverter;
import org.scijava.convert.ConvertService;
import org.scijava.convert.Converter;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.Types;

import java.lang.reflect.Type;

/**
 * Converts a string that matches an {@link ImagePlus} title or ID to {@link Dataset}.
 * (The {@link ImagePlus} must be known to the {@link ij.WindowManager}. This
 * means it must be visible)
 * <p>
 * Similar to {@link StringToImagePlusConverter} but output type is {@link Dataset}.
 *
 * @author Matthias Arzt
 */
@Plugin(type = Converter.class)
public class StringToDatasetConverter
	extends AbstractConverter<String, Dataset>
{

	@Parameter
	ConvertService cs;

	private final Converter<String, ImagePlus> toImagePlus =
		new StringToImagePlusConverter();

	@Override
	public boolean canConvert(Object src, Type dest)
	{
		return canConvert(src, Types.raw(dest));
	}

	@Override
	public boolean canConvert(Object src, Class<?> dest)
	{
		return super.canConvert(src, dest) &&
			toImagePlus.canConvert(src, ImagePlus.class);
	}

	@Override
	public <T> T convert(Object src, Class<T> dest)
	{
		if (!(src instanceof String) || !Dataset.class.equals(dest))
			return null;
		ImagePlus imp = toImagePlus.convert(src, ImagePlus.class);
		if (imp == null) return null;
		return Cast.unchecked(cs.convert(imp, Dataset.class));
	}

	@Override
	public Class<String> getInputType()
	{
		return String.class;
	}

	@Override
	public Class<Dataset> getOutputType()
	{
		return Dataset.class;
	}
}
