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
package net.sourceforge.plantuml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.sourceforge.plantuml.activitydiagram.ActivityDiagramFactory;
import net.sourceforge.plantuml.activitydiagram3.ActivityDiagramFactory3;
import net.sourceforge.plantuml.api.PSystemFactory;
import net.sourceforge.plantuml.board.BoardDiagramFactory;
import net.sourceforge.plantuml.bpm.BpmDiagramFactory;
import net.sourceforge.plantuml.classdiagram.ClassDiagramFactory;
import net.sourceforge.plantuml.command.regex.RegexConcat;
import net.sourceforge.plantuml.core.Diagram;
import net.sourceforge.plantuml.core.DiagramType;
import net.sourceforge.plantuml.core.UmlSource;
import net.sourceforge.plantuml.creole.legacy.PSystemCreoleFactory;
import net.sourceforge.plantuml.dedication.PSystemDedicationFactory;
import net.sourceforge.plantuml.definition.PSystemDefinitionFactory;
import net.sourceforge.plantuml.descdiagram.DescriptionDiagramFactory;
import net.sourceforge.plantuml.donors.PSystemSkinparameterListFactory;
import net.sourceforge.plantuml.ebnf.PSystemEbnfFactory;
import net.sourceforge.plantuml.eggs.PSystemAppleTwoFactory;
import net.sourceforge.plantuml.eggs.PSystemCharlieFactory;
import net.sourceforge.plantuml.eggs.PSystemColorsFactory;
import net.sourceforge.plantuml.eggs.PSystemEggFactory;
import net.sourceforge.plantuml.eggs.PSystemRIPFactory;
import net.sourceforge.plantuml.eggs.PSystemWelcomeFactory;
import net.sourceforge.plantuml.emoji.PSystemListEmojiFactory;
import net.sourceforge.plantuml.error.PSystemError;
import net.sourceforge.plantuml.error.PSystemErrorUtils;
import net.sourceforge.plantuml.flowdiagram.FlowDiagramFactory;
import net.sourceforge.plantuml.font.PSystemListFontsFactory;
import net.sourceforge.plantuml.gitlog.GitDiagramFactory;
import net.sourceforge.plantuml.hcl.HclDiagramFactory;
import net.sourceforge.plantuml.help.HelpFactory;
import net.sourceforge.plantuml.jsondiagram.JsonDiagramFactory;
import net.sourceforge.plantuml.math.PSystemLatexFactory;
import net.sourceforge.plantuml.math.PSystemMathFactory;
import net.sourceforge.plantuml.mindmap.MindMapDiagramFactory;
import net.sourceforge.plantuml.nwdiag.NwDiagramFactory;
import net.sourceforge.plantuml.oregon.PSystemOregonFactory;
import net.sourceforge.plantuml.project.GanttDiagramFactory;
import net.sourceforge.plantuml.regex.PSystemRegexFactory;
import net.sourceforge.plantuml.salt.PSystemSaltFactory2;
import net.sourceforge.plantuml.sequencediagram.SequenceDiagramFactory;
import net.sourceforge.plantuml.sprite.ListSpriteDiagramFactory;
import net.sourceforge.plantuml.statediagram.StateDiagramFactory;
import net.sourceforge.plantuml.sudoku.PSystemSudokuFactory;
import net.sourceforge.plantuml.timingdiagram.TimingDiagramFactory;
import net.sourceforge.plantuml.utils.Log;
import net.sourceforge.plantuml.utils.StringLocated;
import net.sourceforge.plantuml.version.PSystemLicenseFactory;
import net.sourceforge.plantuml.version.PSystemVersionFactory;
import net.sourceforge.plantuml.wbs.WBSDiagramFactory;
import net.sourceforge.plantuml.wire.WireDiagramFactory;
import net.sourceforge.plantuml.yaml.YamlDiagramFactory;

public class PSystemBuilder {

	public static final long startTime = System.currentTimeMillis();

	final public Diagram createPSystem(List<StringLocated> source, List<StringLocated> rawSource,
			Map<String, String> skinParam) {

		final long now = System.currentTimeMillis();

		Diagram result = null;
		try {
			final DiagramType type = DiagramType.getTypeFromArobaseStart(source.get(0).getString());
			final UmlSource umlSource = UmlSource.createWithRaw(source, type == DiagramType.UML, rawSource);

			for (StringLocated s : source) {
				if (s.getPreprocessorError() != null) {
					// Dead code : should not append
					assert false;
					Log.error("Preprocessor Error: " + s.getPreprocessorError());
					final ErrorUml err = new ErrorUml(ErrorUmlType.SYNTAX_ERROR, s.getPreprocessorError(), 0,
							s.getLocation());
					return PSystemErrorUtils.buildV2(umlSource, err, Collections.<String>emptyList(), source);
				}
			}

			final DiagramType diagramType = umlSource.getDiagramType();
			final List<PSystemError> errors = new ArrayList<>();
			for (PSystemFactory systemFactory : factories) {
				if (diagramType != systemFactory.getDiagramType())
					continue;

				final Diagram sys = systemFactory.createSystem(umlSource, skinParam);
				if (isOk(sys)) {
					result = sys;
					return sys;
				}
				errors.add((PSystemError) sys);
			}

			result = PSystemErrorUtils.merge(errors);
			return result;
		} finally {
			Log.info("Compilation duration " + (System.currentTimeMillis() - now));
			RegexConcat.printCacheInfo();
		}
	}

	private static final List<PSystemFactory> factories = new ArrayList<>();

	static {
		factories.add(new PSystemWelcomeFactory());
		factories.add(new PSystemColorsFactory());
		factories.add(new SequenceDiagramFactory());
		factories.add(new ClassDiagramFactory());
		factories.add(new ActivityDiagramFactory());
		factories.add(new DescriptionDiagramFactory());
		factories.add(new StateDiagramFactory());
		factories.add(new ActivityDiagramFactory3());

		factories.add(new BpmDiagramFactory(DiagramType.BPM));

		factories.add(new PSystemLicenseFactory());
		factories.add(new PSystemVersionFactory());

		factories.add(new PSystemSkinparameterListFactory());
		factories.add(new PSystemListFontsFactory());
		factories.add(new PSystemListEmojiFactory());

		factories.add(new PSystemSaltFactory2(DiagramType.SALT));
		factories.add(new PSystemSaltFactory2(DiagramType.UML));

		factories.add(new NwDiagramFactory(DiagramType.NW));
		factories.add(new NwDiagramFactory(DiagramType.UML));
		factories.add(new MindMapDiagramFactory());
		factories.add(new WBSDiagramFactory());

		factories.add(new PSystemSudokuFactory());

		factories.add(new PSystemDefinitionFactory());
		factories.add(new ListSpriteDiagramFactory());

		factories.add(new PSystemMathFactory(DiagramType.MATH));
		factories.add(new PSystemLatexFactory(DiagramType.LATEX));

		factories.add(new PSystemCreoleFactory());
		factories.add(new PSystemEggFactory());
		factories.add(new PSystemAppleTwoFactory());
		factories.add(new PSystemRIPFactory());

		factories.add(new PSystemOregonFactory());
		factories.add(new PSystemCharlieFactory());

		factories.add(new GanttDiagramFactory());
		factories.add(new FlowDiagramFactory());

		factories.add(new PSystemDedicationFactory());
		factories.add(new TimingDiagramFactory());
		factories.add(new HelpFactory());
		factories.add(new WireDiagramFactory());
		factories.add(new JsonDiagramFactory());
		factories.add(new GitDiagramFactory());
		factories.add(new BoardDiagramFactory());
		factories.add(new YamlDiagramFactory());
		factories.add(new HclDiagramFactory());
		factories.add(new PSystemEbnfFactory());
		factories.add(new PSystemRegexFactory());
	}

	private boolean isOk(Diagram ps) {
		if (ps == null || ps instanceof PSystemError)
			return false;

		return true;
	}

}
