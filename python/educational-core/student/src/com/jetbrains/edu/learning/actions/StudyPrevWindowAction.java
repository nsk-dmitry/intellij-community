package com.jetbrains.edu.learning.actions;

import com.intellij.icons.AllIcons;
import com.jetbrains.edu.learning.StudyUtils;
import com.jetbrains.edu.learning.courseFormat.AnswerPlaceholder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * author: liana
 * data: 6/30/14.
 */
public class StudyPrevWindowAction extends StudyWindowNavigationAction {
  public static final String ACTION_ID = "PrevWindowAction";
  public static final String SHORTCUT = "ctrl shift pressed COMMA";

  public StudyPrevWindowAction() {
    super("Navigate to the Previous Answer Placeholder", "Navigate to the previous answer placeholder",
          AllIcons.Actions.Back);
  }


  @Nullable
  @Override
  protected AnswerPlaceholder getNextAnswerPlaceholder(@NotNull final AnswerPlaceholder window) {
    int prevIndex = window.getIndex() - 1;
    List<AnswerPlaceholder> windows = window.getTaskFile().getAnswerPlaceholders();
    if (StudyUtils.indexIsValid(prevIndex, windows)) {
      return windows.get(prevIndex);
    }
    return null;
  }

  @NotNull
  @Override
  public String getActionId() {
    return ACTION_ID;
  }

  @Nullable
  @Override
  public String[] getShortcuts() {
    return new String[]{SHORTCUT};
  }
}
