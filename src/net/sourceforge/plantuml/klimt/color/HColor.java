/* ========================================================================
 * PlantUML : a free UML diagram generator
 * ========================================================================
 *
 * (C) Copyright 2009-2023, Arnaud Roques
 *
 * Project Info:  http://plantuml.com
 * 
 * If you like this project or if you find it useful, you can support us at:
 * 
 * http://plantuml.com/patreon (only 1$ per month!)
 * http://plantuml.com/paypal
 * 
 * This file is part of PlantUML.
 *
 * PlantUML is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PlantUML distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 *
 * Original Author:  Arnaud Roques
 *
 */
package net.sourceforge.plantuml.klimt.color;

import java.awt.Color;

import net.sourceforge.plantuml.StringUtils;
import net.sourceforge.plantuml.klimt.UBackground;
import net.sourceforge.plantuml.klimt.UChange;

public abstract class HColor implements UChange {

	public UBackground bg() {
		return new UBackground() {
			public HColor getBackColor() {
				return HColor.this;
			}
		};
	}

	public Color toColor(ColorMapper mapper) {
		throw new UnsupportedOperationException();
	}
	
	final public String toRGB(ColorMapper mapper) {
		final Color color = toColor(mapper);
		return StringUtils.sharp000000(color.getRGB());
	}

	final public String toSvg(ColorMapper mapper) {
		if (this.isTransparent())
			return "#00000000";

		final Color color = toColor(mapper);
		final int alpha = color.getAlpha();
		if (alpha == 255)
			return toRGB(mapper);

		String s = "0" + Integer.toHexString(alpha).toUpperCase();
		s = s.substring(s.length() - 2);
		return toRGB(mapper) + s;
	}



	public HColor lighten(int ratio) {
		return this;
	}

	public HColor darken(int ratio) {
		return this;
	}

	public HColor reverseHsluv() {
		return this;
	}

	public HColor reverse() {
		return this;
	}

	public boolean isDark() {
		return true;
	}

	public String asString() {
		return "?" + getClass().getSimpleName();
	}

	public HColor darkSchemeTheme() {
		return this;
	}

	public HColor getAppropriateColor(HColor back) {
		return this;
	}

	public HColor withDark(HColor dark) {
		throw new UnsupportedOperationException();
	}

	public HColor opposite() {
		throw new UnsupportedOperationException();
	}

	public boolean isTransparent() {
		return false;

	}

}