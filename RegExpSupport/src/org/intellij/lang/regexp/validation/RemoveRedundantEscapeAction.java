/*
 * Copyright 2006 Sascha Weinreuter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.lang.regexp.validation;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlElement;
import com.intellij.util.IncorrectOperationException;
import com.intellij.xml.util.XmlStringUtil;
import org.intellij.lang.regexp.RegExpTT;
import org.intellij.lang.regexp.psi.RegExpChar;
import org.intellij.lang.regexp.psi.impl.RegExpElementImpl;
import org.jetbrains.annotations.NotNull;

class RemoveRedundantEscapeAction implements IntentionAction {
  private final RegExpChar myChar;

  RemoveRedundantEscapeAction(@NotNull RegExpChar ch) {
    myChar = ch;
  }

  @Override
  @NotNull
  public String getText() {
    return "Remove redundant escape";
  }

  @Override
  @NotNull
  public String getFamilyName() {
    return "Redundant character escape";
  }

  @Override
  public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
    return myChar.isValid() && myChar.getUnescapedText().startsWith("\\");
  }

  @Override
  public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
    final int v = myChar.getValue();
    assert v != -1;

    final ASTNode node = myChar.getNode().getFirstChildNode();
    final ASTNode parent = node.getTreeParent();
    parent.addLeaf(RegExpTT.CHARACTER, replacement(v), node);
    parent.removeChild(node);
  }

  @NotNull
  private String replacement(int codePoint) {
    final PsiElement context = myChar.getContainingFile().getContext();
    String s = Character.isSupplementaryCodePoint(codePoint)
               ? Character.toString(Character.highSurrogate(codePoint)) + Character.toString(Character.lowSurrogate(codePoint))
               : Character.toString((char)codePoint);
    if (RegExpElementImpl.isLiteralExpression(context)) {
      return StringUtil.escapeStringCharacters(s);
    }
    else if (context instanceof XmlElement) {
      return XmlStringUtil.escapeString(s);
    }
    else {
      return s;
    }
  }

  @Override
  public boolean startInWriteAction() {
    return true;
  }
}
