package com.intellij.codeInsight.intention.impl.config;

import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.codeInsight.daemon.impl.quickfix.*;
import com.intellij.codeInsight.daemon.impl.*;
import com.intellij.codeInsight.daemon.HighlightDisplayKey;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.IntentionManager;
import com.intellij.codeInsight.intention.impl.*;
import com.intellij.openapi.project.Project;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiElement;
import com.intellij.codeInspection.ex.EditInspectionToolsSettingsAction;

import java.util.ArrayList;
import java.util.List;

/**
 *  @author dsl
 */
public class IntentionManagerImpl extends IntentionManager {
  private List<IntentionAction> myActions = new ArrayList<IntentionAction>();
  private Project myProject;
  private IntentionManagerSettings mySettings;

  public IntentionManagerImpl(Project project, IntentionManagerSettings intentionManagerSettings) {
    myProject = project;
    mySettings = intentionManagerSettings;

    addAction(new QuickFixAction());
    addAction(new PostIntentionsQuickFixAction());

    String[] CONTROL_FLOW_CATEGORY = new String[]{CodeInsightBundle.message("intentions.category.control.flow")};
    registerIntentionAndMetaData(new SplitIfAction(), CONTROL_FLOW_CATEGORY);
    registerIntentionAndMetaData(new InvertIfConditionAction(), CONTROL_FLOW_CATEGORY);
    registerIntentionAndMetaData(new RemoveRedundantElseAction(), CONTROL_FLOW_CATEGORY);

    String[] DECLARATION_CATEGORY = new String[]{CodeInsightBundle.message("intentions.category.declaration")};
    registerIntentionAndMetaData(new CreateFieldFromParameterAction(), DECLARATION_CATEGORY);
    registerIntentionAndMetaData(new AssignFieldFromParameterAction(), DECLARATION_CATEGORY);
    registerIntentionAndMetaData(new CreateLocalVarFromInstanceofAction(), DECLARATION_CATEGORY);
    registerIntentionAndMetaData(new ImplementAbstractClassAction(), DECLARATION_CATEGORY);
    registerIntentionAndMetaData(new ImplementAbstractMethodAction(), DECLARATION_CATEGORY);
    registerIntentionAndMetaData(new SplitDeclarationAction(), DECLARATION_CATEGORY);
    registerIntentionAndMetaData(new MoveInitializerToConstructorAction(), DECLARATION_CATEGORY);
    registerIntentionAndMetaData(new AddRuntimeExceptionToThrowsAction(), DECLARATION_CATEGORY);

    registerIntentionAndMetaData(new SimplifyBooleanExpressionAction(), CodeInsightBundle.message("intentions.category.boolean"));

    registerIntentionAndMetaData(new ConcatenationToMessageFormatAction(), CodeInsightBundle.message("intentions.category.i18n"));
  }

  public void registerIntentionAndMetaData(IntentionAction action, String... category) {
    registerIntentionAndMetaData(action, category, getDescriptionDirectoryName(action));
  }

  private static String getDescriptionDirectoryName(final IntentionAction action) {
    final String fqn = action.getClass().getName();
    return fqn.substring(fqn.lastIndexOf('.') + 1);
  }

  public void registerIntentionAndMetaData(IntentionAction action, String[] category, String descriptionDirectoryName) {
    addAction(action);
    mySettings.registerIntentionMetaData(action, category, descriptionDirectoryName);
  }

  public List<IntentionAction> getStandardIntentionOptions(final HighlightDisplayKey displayKey, final PsiElement context) {
    List<IntentionAction> options = new ArrayList<IntentionAction>();
    options.add(new EditInspectionToolsSettingsAction(displayKey));
    options.add(new AddNoInspectionCommentAction(displayKey, context));
    options.add(new AddNoInspectionDocTagAction(displayKey, context));
    options.add(new AddNoInspectionForClassAction(displayKey, context));
    options.add(new AddNoInspectionAllForClassAction(context));
    options.add(new AddSuppressWarningsAnnotationAction(displayKey, context));
    options.add(new AddSuppressWarningsAnnotationForClassAction(displayKey, context));
    options.add(new AddSuppressWarningsAnnotationForAllAction(context));
    return options;
  }

  public void initComponent() { }

  public void disposeComponent(){
  }

  public String getComponentName(){
    return "IntentionManager";
  }

  public void projectOpened(){
    if (LanguageLevel.JDK_1_5.compareTo(PsiManager.getInstance(myProject).getEffectiveLanguageLevel()) <= 0) {
      registerIntentionAndMetaData(new MakeTypeGeneric(), CodeInsightBundle.message("intentions.category.declaration"));
      registerIntentionAndMetaData(new AddOverrideAnnotationAction(), CodeInsightBundle.message("intentions.category.declaration"));

      registerIntentionAndMetaData(new AddOnDemandStaticImportAction(), CodeInsightBundle.message("intentions.category.imports"));
      registerIntentionAndMetaData(new AddSingleMemberStaticImportAction(), CodeInsightBundle.message("intentions.category.imports"));
    }
  }

  public void projectClosed(){
  }

  public void addAction(IntentionAction action) {
    myActions.add(action);
  }

  public IntentionAction[] getIntentionActions() {
    return myActions.toArray(new IntentionAction[myActions.size()]);
  }
}
