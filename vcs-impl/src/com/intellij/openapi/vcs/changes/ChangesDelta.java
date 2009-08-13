package com.intellij.openapi.vcs.changes;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsKey;
import com.intellij.openapi.vcs.impl.CollectionsDelta;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class ChangesDelta {
  private final PlusMinus<Pair<String, AbstractVcs>> myDeltaListener;
  private ProjectLevelVcsManager myVcsManager;
  private boolean myInitialized;

  public ChangesDelta(final Project project, final PlusMinus<Pair<String, AbstractVcs>> deltaListener) {
    myDeltaListener = deltaListener;
    myVcsManager = ProjectLevelVcsManager.getInstance(project);
  }

  public void step(final ChangeListsIndexes was, final ChangeListsIndexes became) {
    List<Pair<String, VcsKey>> wasAffected = was.getAffectedFilesUnderVcs();
    if (! myInitialized) {
      sendPlus(wasAffected);
      myInitialized = true;
      return;
    }
    final List<Pair<String, VcsKey>> becameAffected = became.getAffectedFilesUnderVcs();

    final Set<Pair<String,VcsKey>> toRemove = CollectionsDelta.notInSecond(wasAffected, becameAffected);
    final Set<Pair<String, VcsKey>> toAdd = CollectionsDelta.notInSecond(becameAffected, wasAffected);

    if (toRemove != null) {
      for (Pair<String, VcsKey> pair : toRemove) {
        myDeltaListener.minus(convertPair(pair));
      }
    }
    sendPlus(toAdd);
  }

  private void sendPlus(final Collection<Pair<String, VcsKey>> toAdd) {
    if (toAdd != null) {
      for (Pair<String, VcsKey> pair : toAdd) {
        myDeltaListener.plus(convertPair(pair));
      }
    }
  }

  private Pair<String, AbstractVcs> convertPair(final Pair<String, VcsKey> pair) {
    final VcsKey vcsKey = pair.getSecond();
    return new Pair<String, AbstractVcs>(pair.getFirst(), (vcsKey == null) ? null : myVcsManager.findVcsByName(vcsKey.getName()));
  }
}
