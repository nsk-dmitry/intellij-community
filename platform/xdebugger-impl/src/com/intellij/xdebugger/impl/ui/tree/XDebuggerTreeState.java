/*
 * Copyright 2000-2016 JetBrains s.r.o.
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
package com.intellij.xdebugger.impl.ui.tree;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import com.intellij.xdebugger.impl.ui.tree.nodes.RestorableStateNode;
import com.intellij.xdebugger.impl.ui.tree.nodes.XDebuggerTreeNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.Collection;
import java.util.List;

/**
 * @author nik
 */
public class XDebuggerTreeState {
  private final NodeInfo myRootInfo;
  private Rectangle myLastVisibleNodeRect;

  private XDebuggerTreeState(@NotNull XDebuggerTree tree) {
    ApplicationManager.getApplication().assertIsDispatchThread();
    XDebuggerTreeNode root = tree.getRoot();
    myRootInfo = root != null ? new NodeInfo("", "", tree.isPathSelected(root.getPath())) : null;
    if (root != null) {
      addChildren(tree, myRootInfo, root);
    }
  }

  public XDebuggerTreeRestorer restoreState(@NotNull XDebuggerTree tree) {
    ApplicationManager.getApplication().assertIsDispatchThread();
    XDebuggerTreeRestorer restorer = null;
    if (myRootInfo != null) {
      restorer = new XDebuggerTreeRestorer(tree, myLastVisibleNodeRect);
      restorer.restore(tree.getRoot(), myRootInfo);
    }
    return restorer;
  }

  public static XDebuggerTreeState saveState(@NotNull XDebuggerTree tree) {
    return new XDebuggerTreeState(tree);
  }

  private void addChildren(final XDebuggerTree tree, final NodeInfo nodeInfo, final XDebuggerTreeNode treeNode) {
    if (tree.isExpanded(treeNode.getPath())) {
      List<? extends XDebuggerTreeNode> children = treeNode.getLoadedChildren();
      nodeInfo.myExpanded = true;
      for (XDebuggerTreeNode child : children) {
        TreePath path = child.getPath();
        Rectangle bounds = tree.getPathBounds(path);
        if (bounds != null) {
          Rectangle treeVisibleRect =
            tree.getParent() instanceof JViewport ? ((JViewport)tree.getParent()).getViewRect() : tree.getVisibleRect();
          if (treeVisibleRect.contains(bounds)) {
            myLastVisibleNodeRect = bounds;
          }
        }
        NodeInfo childInfo = createNode(child, tree.isPathSelected(path));
        if (childInfo != null) {
          nodeInfo.addChild(childInfo);
          addChildren(tree, childInfo, child);
        }
      }
    }
  }

  @Nullable
  private static NodeInfo createNode(final XDebuggerTreeNode node, boolean selected) {
    if (node instanceof RestorableStateNode) {
      RestorableStateNode valueNode = (RestorableStateNode)node;
      if (valueNode.isComputed()) {
        return new NodeInfo(valueNode.getName(), valueNode.getRawValue(), selected);
      }
    }
    return null;
  }

  public static class NodeInfo {
    private final String myName;
    private final String myValue;
    private boolean myExpanded;
    private final boolean mySelected;
    private MultiMap<String, NodeInfo> myChildren; // MultiMap to allow several nodes with the same name

    public NodeInfo(final String name, final String value, boolean selected) {
      myName = name;
      myValue = value;
      mySelected = selected;
    }

    public void addChild(@NotNull NodeInfo child) {
      if (myChildren == null) {
        myChildren = new MultiMap<>();
      }
      myChildren.putValue(child.myName, child);
    }

    public boolean isExpanded() {
      return myExpanded;
    }

    public boolean isSelected() {
      return mySelected;
    }

    public String getValue() {
      return myValue;
    }

    @Nullable
    public NodeInfo removeChild(String name) {
      if (myChildren == null) {
        return null;
      }
      Collection<NodeInfo> infos = myChildren.get(name);
      NodeInfo item = ContainerUtil.getFirstItem(infos);
      if (item != null) {
        infos.remove(item);
      }
      return item;
    }
  }
}
