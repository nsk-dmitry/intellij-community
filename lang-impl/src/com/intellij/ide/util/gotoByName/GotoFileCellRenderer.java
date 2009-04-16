package com.intellij.ide.util.gotoByName;

import com.intellij.ide.util.PsiElementListCellRenderer;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.util.ui.FilePathSplittingPolicy;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.File;

public class GotoFileCellRenderer extends PsiElementListCellRenderer<PsiFile>{
  private final int myMaxWidth;
  private static final Logger LOG = Logger.getInstance("#com.intellij.ide.util.gotoByName.GotoFileCellRenderer");

  public GotoFileCellRenderer(int maxSize) {
    myMaxWidth = maxSize;
    EditorColorsScheme scheme = EditorColorsManager.getInstance().getGlobalScheme();
    Font editorFont = new Font(scheme.getEditorFontName(), Font.PLAIN, scheme.getEditorFontSize());
    setFont(editorFont);
  }

  public String getElementText(PsiFile element) {
    return element.getName();
  }

  protected String getContainerText(PsiFile element, String name) {
    final PsiDirectory psiDirectory = element.getContainingDirectory();
    LOG.assertTrue(psiDirectory != null, "File " + element.getName() + " of " + element.getClass().getName() + ", isValid " + element.isValid() + ", isPhysical " + element.isPhysical());
    final VirtualFile virtualFile = psiDirectory.getVirtualFile();
    final String relativePath = getRelativePath(virtualFile, element.getProject());
    if (relativePath == null) return "( " + File.separator + " )";
    String path = FilePathSplittingPolicy.SPLIT_BY_SEPARATOR.getOptimalTextForComponent(name + "          ", new File(relativePath), this, myMaxWidth);
    return "(" + path + ")";
  }

  @Nullable
  private static String getRelativePath(final VirtualFile virtualFile, final Project project) {
    String url = virtualFile.getPresentableUrl();
    if (project == null) {
      return url;
    }
    else {
      final VirtualFile baseDir = project.getBaseDir();
      if (baseDir != null) {
        //noinspection ConstantConditions
        final String projectHomeUrl = baseDir.getPresentableUrl();
        if (url.startsWith(projectHomeUrl)) {
          final String cont = url.substring(projectHomeUrl.length());
          if (cont.length() == 0) return null;
          url = "..." + cont;
        }
      }
    }
    return url;
  }

  protected int getIconFlags() {
    return 0;
  }
}