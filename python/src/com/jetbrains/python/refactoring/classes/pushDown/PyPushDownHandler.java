/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.python.refactoring.classes.pushDown;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.util.Query;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.search.PyClassInheritorsSearch;
import com.jetbrains.python.refactoring.classes.PyClassRefactoringHandler;
import com.jetbrains.python.refactoring.classes.PyMemberInfoStorage;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dennis.Ushakov
 */
public class PyPushDownHandler extends PyClassRefactoringHandler {
  public static final String REFACTORING_NAME = RefactoringBundle.message("push.members.down.title");

  @Override
  protected void doRefactorImpl(@NotNull Project project,
                                @NotNull PyClass classUnderRefactoring,
                                @NotNull PyMemberInfoStorage infoStorage,
                                @NotNull Editor editor) {


    final Query<PyClass> query = PyClassInheritorsSearch.search(classUnderRefactoring, false);
    if (query.findFirst() == null) {
      final String message = RefactoringBundle.message("class.0.does.not.have.inheritors", classUnderRefactoring.getName());
      CommonRefactoringUtil.showErrorHint(project, editor, message, getTitle(), getHelpId());
      return;
    }

    final PyPushDownDialog dialog = new PyPushDownDialog(project, classUnderRefactoring, infoStorage);
    dialog.show();
  }

  @Override
  protected String getTitle() {
    return REFACTORING_NAME;
  }

  @Override
  protected String getHelpId() {
    return "members.push.down";
  }

}
