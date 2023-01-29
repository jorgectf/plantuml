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
 *
 */
package net.sourceforge.plantuml.svek;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sourceforge.plantuml.ISkinParam;
import net.sourceforge.plantuml.baraye.EntityImp;
import net.sourceforge.plantuml.baraye.EntityUtils;
import net.sourceforge.plantuml.cucadiagram.Display;
import net.sourceforge.plantuml.cucadiagram.GroupHierarchy;
import net.sourceforge.plantuml.cucadiagram.GroupType;
import net.sourceforge.plantuml.cucadiagram.ICucaDiagram;
import net.sourceforge.plantuml.cucadiagram.LeafType;
import net.sourceforge.plantuml.cucadiagram.Link;
import net.sourceforge.plantuml.cucadiagram.Stereotype;
import net.sourceforge.plantuml.cucadiagram.dot.DotData;
import net.sourceforge.plantuml.graphic.FontConfiguration;
import net.sourceforge.plantuml.graphic.HorizontalAlignment;
import net.sourceforge.plantuml.graphic.StringBounder;
import net.sourceforge.plantuml.graphic.TextBlock;
import net.sourceforge.plantuml.graphic.color.ColorType;
import net.sourceforge.plantuml.style.PName;
import net.sourceforge.plantuml.style.SName;
import net.sourceforge.plantuml.style.Style;
import net.sourceforge.plantuml.svek.image.EntityImageState;
import net.sourceforge.plantuml.svek.image.EntityImageStateCommon;
import net.sourceforge.plantuml.ugraphic.UStroke;
import net.sourceforge.plantuml.ugraphic.color.HColor;

public final class GroupPngMakerState {

	private final ICucaDiagram diagram;
	private final EntityImp group;
	private final StringBounder stringBounder;

	class InnerGroupHierarchy implements GroupHierarchy {

		public EntityImp getRootGroup() {
			throw new UnsupportedOperationException();
		}

		public Collection<EntityImp> getChildrenGroups(EntityImp parent) {
			if (EntityUtils.groupRoot(parent))
				return diagram.getChildrenGroups(group);

			return diagram.getChildrenGroups(parent);
		}

		public boolean isEmpty(EntityImp g) {
			return diagram.isEmpty(g);
		}

	}

	public GroupPngMakerState(ICucaDiagram diagram, EntityImp group, StringBounder stringBounder) {
		this.diagram = diagram;
		this.stringBounder = stringBounder;
		this.group = group;
		if (group.isGroup() == false)
			throw new IllegalArgumentException();

	}

	private List<Link> getPureInnerLinks() {
		final List<Link> result = new ArrayList<>();
		for (Link link : diagram.getLinks())
			if (EntityUtils.isPureInnerLink12(group, link))
				result.add(link);

		return result;
	}

	public IEntityImage getImage() {
		final Display display = group.getDisplay();
		final ISkinParam skinParam = diagram.getSkinParam();

		final Style style = EntityImageStateCommon.getStyleState(group, skinParam);
		// final Style styleHeader = EntityImageStateCommon.getStyleStateHeader(group,
		// skinParam);
		final Style styleTitle = EntityImageStateCommon.getStyleStateTitle(group, skinParam);
		final Style styleBody = EntityImageStateCommon.getStyleStateBody(group, skinParam);

		final double rounded = style.value(PName.RoundCorner).asDouble();
		final double shadowing = style.value(PName.Shadowing).asDouble();
		final FontConfiguration titleFontConfiguration = styleTitle.getFontConfiguration(skinParam.getIHtmlColorSet());
		final TextBlock title = display.create(titleFontConfiguration, HorizontalAlignment.CENTER,
				diagram.getSkinParam());

		if (group.size() == 0 && group.getChildren().size() == 0)
			return new EntityImageState(group, diagram.getSkinParam());

		final List<Link> links = getPureInnerLinks();

		final DotData dotData = new DotData(group, links, group.getLeafsDirect(), diagram.getUmlDiagramType(),
				skinParam, new InnerGroupHierarchy(), diagram.getEntityFactory(),
				diagram.isHideEmptyDescriptionForState(), DotMode.NORMAL, diagram.getNamespaceSeparator(),
				diagram.getPragma());

		final GeneralImageBuilder svek2 = new GeneralImageBuilder(dotData, diagram.getEntityFactory(),
				diagram.getSource(), diagram.getPragma(), stringBounder, SName.stateDiagram);

		if (group.getGroupType() == GroupType.CONCURRENT_STATE)
			return svek2.buildImage(null, new String[0]);

		if (group.getGroupType() != GroupType.STATE)
			throw new UnsupportedOperationException(group.getGroupType().toString());

		HColor borderColor = group.getColors().getColor(ColorType.LINE);
		if (borderColor == null)
			borderColor = style.value(PName.LineColor).asColor(skinParam.getIHtmlColorSet());

		final Stereotype stereo = group.getStereotype();
		HColor backColor = group.getColors().getColor(ColorType.BACK);
		if (backColor == null)
			backColor = style.value(PName.BackGroundColor).asColor(skinParam.getIHtmlColorSet());

		UStroke stroke = group.getColors().getSpecificLineStroke();
		if (stroke == null)
			stroke = style.getStroke();

		final TextBlock attribute = ((EntityImp) group).getStateHeader(skinParam);

		final Stereotype stereotype = group.getStereotype();
		final boolean withSymbol = stereotype != null && stereotype.isWithOOSymbol();

		final boolean containsOnlyConcurrentStates = containsOnlyConcurrentStates(dotData);
		final IEntityImage image = containsOnlyConcurrentStates ? buildImageForConcurrentState(dotData)
				: svek2.buildImage(null, new String[0]);

		final HColor bodyColor = styleBody.value(PName.BackGroundColor).asColor(skinParam.getIHtmlColorSet());
		return new InnerStateAutonom(image, title, attribute, borderColor, backColor, group.getUrl99(), withSymbol,
				stroke, rounded, shadowing, bodyColor);

	}

	private IEntityImage buildImageForConcurrentState(DotData dotData) {
		final List<IEntityImage> inners = new ArrayList<>();
		for (EntityImp inner : dotData.getLeafs())
			inners.add(inner.getSvekImage());

		return new CucaDiagramFileMakerSvek2InternalImage(inners, dotData.getTopParent().getConcurrentSeparator(),
				dotData.getSkinParam(), group.getStereotype());

	}

	private boolean containsOnlyConcurrentStates(DotData dotData) {
		for (EntityImp leaf : dotData.getLeafs()) {
			if (leaf instanceof EntityImp == false)
				return false;

			if (((EntityImp) leaf).getLeafType() != LeafType.STATE_CONCURRENT)
				return false;

		}
		return true;
	}

}
