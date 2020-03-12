/*-
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

package net.imagej.legacy.plugin;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import org.fife.rsta.ac.AbstractLanguageSupport;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.LanguageAwareCompletionProvider;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Token;
import org.scijava.module.ModuleService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.swing.script.LanguageSupportPlugin;

/**
 * {@link LanguageSupportPlugin} making basic auto-completion available for IJ
 * macro scripts. It offers all commands and additional help copied from the
 * <a href="https://imagej.net/developer/macro/functions.html">ImageJ macro
 * functions documentation</a>.
 *
 * @author Robert Haase
 */
@Plugin(type = LanguageSupportPlugin.class)
public class MacroLanguageSupportPlugin extends AbstractLanguageSupport
	implements LanguageSupportPlugin
{
	@Parameter
	ModuleService moduleService;

	@Parameter
	MacroExtensionAutoCompletionService macroExtensionAutoCompletionService;

	private final static int MINIMUM_WORD_LENGTH_TO_OPEN_PULLDOWN = 2;

	@Override
	public String getLanguageName() {
		return "IJ1 Macro";
	}

	@Override
	public void install(final RSyntaxTextArea rSyntaxTextArea) {
		final AutoCompletion ac = createAutoCompletion(getCompletionProvider());
		ac.setAutoActivationDelay(100);
		ac.setAutoActivationEnabled(true);
		ac.setShowDescWindow(true);
		ac.install(rSyntaxTextArea);
		installImpl(rSyntaxTextArea, ac);

		rSyntaxTextArea.addKeyListener(new MacroAutoCompletionKeyListener(ac,
			rSyntaxTextArea));

		rSyntaxTextArea.setToolTipSupplier(getMacroAutoCompletionProvider());
	}

	private CompletionProvider getCompletionProvider() {
		CompletionProvider provider = new CustomLanguageAwareCompletionProvider(getMacroAutoCompletionProvider());
		return provider;
	}

	// this class is necessary to prevent the language aware provider to sort the result list; we have our own sorting
	private class CustomLanguageAwareCompletionProvider extends LanguageAwareCompletionProvider{
		public CustomLanguageAwareCompletionProvider(CompletionProvider provider){
			super(provider);
		}

		@Override
		public List<Completion> getCompletions(JTextComponent comp) {
			List<Completion> completions = this.getCompletionsImpl(comp);
			return completions;
		}
	}

	private MacroAutoCompletionProvider getMacroAutoCompletionProvider() {
		MacroAutoCompletionProvider provider = MacroAutoCompletionProvider
				.getInstance();
		provider.addModuleCompletions(moduleService);
		provider.addMacroExtensionAutoCompletions(macroExtensionAutoCompletionService);
		provider.sort();
		return provider;
	}

	@Override
	public void uninstall(final RSyntaxTextArea rSyntaxTextArea) {
		uninstallImpl(rSyntaxTextArea);

		final ArrayList<KeyListener> toRemove = new ArrayList<>();
		for (final KeyListener keyListener : rSyntaxTextArea.getKeyListeners()) {
			if (keyListener instanceof MacroAutoCompletionKeyListener) {
				toRemove.add(keyListener);
			}
		}
		for (final KeyListener keyListener : toRemove) {
			rSyntaxTextArea.removeKeyListener(keyListener);
		}

	}

	private class MacroAutoCompletionKeyListener implements KeyListener {

		AutoCompletion ac;
		RSyntaxTextArea textArea;
		ArrayList<Character> disabledChars;

		public MacroAutoCompletionKeyListener(final AutoCompletion ac,
			final RSyntaxTextArea textArea)
		{
			this.ac = ac;
			this.ac.setAutoCompleteSingleChoices(false);
			this.textArea = textArea;

			disabledChars = new ArrayList<>();
			disabledChars.add(' ');
			disabledChars.add('\n');
			disabledChars.add('\t');
			disabledChars.add(';');
		}

		@Override
		public void keyTyped(final KeyEvent e) {

		}

		@Override
		public void keyPressed(final KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER && !ac.isPopupVisible()) { // Enter has been pressed, but autocompletion is not active
				try {
					int caretPosition = textArea.getCaretPosition();
					int lineNumber = textArea.getLineOfOffset(caretPosition);
					int lineOffset = textArea.getLineStartOffset(lineNumber);
					String partialLineToCaret = textArea.getText(lineOffset, caretPosition - lineOffset);
					int innerCaretPosition = lineOffset + partialLineToCaret.replaceAll("\\s+$","").length(); // using right trim hack
					
					Token tkn = textArea.getTokenListForLine(lineNumber);
					Token lasttkn = tkn.getLastNonCommentNonWhitespaceToken();

					if (tkn.getType() != Token.NULL && lasttkn != null) { // not an empty/comment only line
													
						if (innerCaretPosition <= lasttkn.getEndOffset() && // not behind trailing comments
						    !(lasttkn.isLeftCurly() || lasttkn.isRightCurly()) && // no curly braces at the end
						    !partialLineToCaret.trim().endsWith(";") && // there's no semicolon already in place
						    caretPosition > textArea.getLineStartOffset(lineNumber) && // not at the very line start
						    !partialLineToCaret.startsWith("#@") && // excluding script parameter declarations
						    partialLineToCaret.replaceFirst("\\s*(for|if|while)\\s*\\(.+?\\)(\\s+|$)", "").length()!=0  // exclude split-line for/if/while statements 
							) {
							textArea.insert(";", innerCaretPosition);
						}
					}
				} catch (BadLocationException e2) {
					e2.printStackTrace();
				}		
			}
		}

		@Override
		public void keyReleased(final KeyEvent e) {
			SwingUtilities.invokeLater(() -> {
				if (disabledChars.contains(e.getKeyChar())) {
					if (!e.isControlDown()) {
						// the pulldown should not be hidden if CTRL+SPACE are pressed
						ac.hideChildWindows();
					}
				} else if ((e.isControlDown() && e.getKeyCode() != KeyEvent.VK_SPACE) || // control pressed but not space
					e.getKeyCode() == KeyEvent.VK_LEFT || // arrow keys left/right were pressed
					e.getKeyCode() == KeyEvent.VK_RIGHT
				) {
					ac.hideChildWindows();
				} else if (e.getKeyCode() >= 65 // a
				&& e.getKeyCode() <= 90 // z
				) {
					if (MacroAutoCompletionProvider.getInstance().getAlreadyEnteredText(
						textArea).length() >= MINIMUM_WORD_LENGTH_TO_OPEN_PULLDOWN &&
						MacroAutoCompletionProvider.getInstance()
							.getCompletions(textArea).size() > 0)
					{
						ac.doCompletion();
					}
				}
			});
		}
	}

}
