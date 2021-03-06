package com.intellij.refactoring;

import com.intellij.JavaTestUtil;
import com.intellij.codeInsight.TargetElementUtilBase;
import com.intellij.openapi.roots.LanguageLevelProjectExtension;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.refactoring.safeDelete.SafeDeleteHandler;
import com.intellij.testFramework.PsiTestUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.jps.model.java.JavaSourceRootType;
import org.jetbrains.jps.model.java.JpsJavaExtensionService;

public class SafeDeleteTest extends MultiFileTestCase {
  @Override
  protected String getTestDataPath() {
    return JavaTestUtil.getJavaTestDataPath();
  }

  @Override
  protected String getTestRoot() {
    return "/refactoring/safeDelete/";
  }

  public void testImplicitCtrCall() throws Exception {
    try {
      doTest("Super");
      fail();
    }
    catch (BaseRefactoringProcessor.ConflictsInTestsException e) {
      String message = e.getMessage();
      assertTrue(message, message.startsWith("constructor <b><code>Super.Super()</code></b> has 1 usage that is not safe to delete"));
    }
  }

  public void testImplicitCtrCall2() throws Exception {
    try {
      doTest("Super");
      fail();
    }
    catch (BaseRefactoringProcessor.ConflictsInTestsException e) {
      String message = e.getMessage();
      assertTrue(message, message.startsWith("constructor <b><code>Super.Super()</code></b> has 1 usage that is not safe to delete"));
    }
  }

  public void testMultipleInterfacesImplementation() throws Exception {
    doTest("IFoo");
  }

  public void testMultipleInterfacesImplementationThroughCommonInterface() throws Exception {
    doTest("IFoo");
  }

  public void testUsageInExtendsList() throws Exception {
    doSingleFileTest();
  }

  public void testParameterInHierarchy() throws Exception {
    doTest("C2");
  }


  public void testTopLevelDocComment() throws Exception {
    doTest("foo.C1");
  }

  public void testTopParameterInHierarchy() throws Exception {
    doTest("I");
  }

  public void testExtendsList() throws Exception {
    doTest("B");
  }

  public void testJavadocParamRef() throws Exception {
    doTest("Super");
  }

  public void testEnumConstructorParameter() throws Exception {
    doTest("UserFlags");
  }

  public void testSafeDeleteStaticImports() throws Exception {
    doTest("A");
  }

  public void testRemoveOverridersInspiteOfUnsafeUsages() throws Exception {
    try {
      BaseRefactoringProcessor.ConflictsInTestsException.setTestIgnore(true);
      doTest("A");
    }
    finally {
      BaseRefactoringProcessor.ConflictsInTestsException.setTestIgnore(false);
    }
  }

  public void testLocalVariable() throws Exception {
    doTest("Super");
  }

  public void testOverrideAnnotation() throws Exception {
    doTest("Super");
  }

  public void testSuperCall() throws Exception {
    try {
      doTest("Super");
      fail("Conflict was not detected");
    }
    catch (BaseRefactoringProcessor.ConflictsInTestsException e) {
      String message = e.getMessage();
      assertEquals("method <b><code>Super.foo()</code></b> has 1 usage that is not safe to delete.", message);
    }
  }

  public void testMethodDeepHierarchy() throws Exception {
    doTest("Super");
  }

  public void testInterfaceAsTypeParameterBound() throws Exception {
    doSingleFileTest();   
  }

  public void testLocalVariableSideEffect() throws Exception {
    try {
      doTest("Super");
      fail("Side effect was ignored");
    }
    catch (BaseRefactoringProcessor.ConflictsInTestsException e) {
      String message = e.getMessage();
      assertEquals("local variable <b><code>varName</code></b> has 1 usage that is not safe to delete.", message);
    }
  }

  public void testUsageInGenerated() throws Exception {
    doTest("A");
  }

  public void testLastResourceVariable() throws Exception {
    LanguageLevelProjectExtension.getInstance(getProject()).setLanguageLevel(LanguageLevel.JDK_1_7);
    doSingleFileTest();
  }

  public void testLastResourceVariableWithFinallyBlock() throws Exception {
    LanguageLevelProjectExtension.getInstance(getProject()).setLanguageLevel(LanguageLevel.JDK_1_7);
    doSingleFileTest();
  }

  public void testLastTypeParam() throws Exception {
    LanguageLevelProjectExtension.getInstance(getProject()).setLanguageLevel(LanguageLevel.JDK_1_7);
    doSingleFileTest();
  }

  public void testTypeParamFromDiamond() throws Exception {
    LanguageLevelProjectExtension.getInstance(getProject()).setLanguageLevel(LanguageLevel.JDK_1_7);
    doSingleFileTest();
  }

  public void testStripOverride() throws Exception {
    doSingleFileTest();
  }

  public void testEmptyIf() throws Exception {
    doSingleFileTest();
  }
  
  private void doTest(@NonNls final String qClassName) throws Exception {
    doTest(new PerformAction() {
      @Override
      public void performAction(VirtualFile rootDir, VirtualFile rootAfter) throws Exception {
        SafeDeleteTest.this.performAction(qClassName);
      }
    });
  }

  @Override
  protected void prepareProject(VirtualFile rootDir) {
    VirtualFile src = rootDir.findChild("src");
    if (src == null) {
      super.prepareProject(rootDir);
    }
    else {
      PsiTestUtil.addContentRoot(myModule, rootDir);
      PsiTestUtil.addSourceRoot(myModule, src);
    }
    VirtualFile gen = rootDir.findChild("gen");
    if (gen != null) {
      PsiTestUtil.addSourceRoot(myModule, gen, JavaSourceRootType.SOURCE, JpsJavaExtensionService.getInstance().createSourceRootProperties("", true));
    }
  }

  private void doSingleFileTest() throws Exception {
    configureByFile(getTestRoot() + getTestName(false) + ".java");
    performAction();
    checkResultByFile(getTestRoot() + getTestName(false) + "_after.java");
  }

  private void performAction(final String qClassName) throws Exception {
    final PsiClass aClass = myJavaFacade.findClass(qClassName, GlobalSearchScope.allScope(getProject()));
    assertNotNull("Class " + qClassName + " not found", aClass);
    configureByExistingFile(aClass.getContainingFile().getVirtualFile());

    performAction();
  }

  private void performAction() {
    final PsiElement psiElement = TargetElementUtilBase
      .findTargetElement(myEditor, TargetElementUtilBase.ELEMENT_NAME_ACCEPTED | TargetElementUtilBase.REFERENCED_ELEMENT_ACCEPTED);
    assertNotNull("No element found in text:\n" + getFile().getText(), psiElement);
    SafeDeleteHandler.invoke(getProject(), new PsiElement[]{psiElement}, true);
  }
}