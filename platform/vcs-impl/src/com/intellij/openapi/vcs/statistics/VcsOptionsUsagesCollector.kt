// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.openapi.vcs.statistics

import com.intellij.internal.statistic.beans.MetricEvent
import com.intellij.internal.statistic.beans.addBoolIfDiffers
import com.intellij.internal.statistic.beans.addMetricIfDiffers
import com.intellij.internal.statistic.beans.newMetric
import com.intellij.internal.statistic.service.fus.collectors.ProjectUsagesCollector
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.VcsConfiguration
import com.intellij.openapi.vcs.VcsShowConfirmationOption
import java.util.*

class VcsOptionsUsagesCollector : ProjectUsagesCollector() {
  override fun getGroupId(): String = "vcs.settings"
  override fun getVersion(): Int = 2

  override fun getMetrics(project: Project): MutableSet<MetricEvent> {
    val set = HashSet<MetricEvent>()

    val conf = VcsConfiguration.getInstance(project)
    val confDefault = VcsConfiguration()

    addBoolIfDiffers(set, conf, confDefault, { it.OFFER_MOVE_TO_ANOTHER_CHANGELIST_ON_PARTIAL_COMMIT }, "offer.move.partially.committed")
    addConfirmationIfDiffers(set, conf, confDefault, { it.MOVE_TO_FAILED_COMMIT_CHANGELIST }, "offer.move.failed.committed")
    addConfirmationIfDiffers(set, conf, confDefault, { it.REMOVE_EMPTY_INACTIVE_CHANGELISTS }, "offer.remove.empty.changelist")

    addBoolIfDiffers(set, conf, confDefault, { it.MAKE_NEW_CHANGELIST_ACTIVE }, "changelist.make.new.active")
    addBoolIfDiffers(set, conf, confDefault, { it.PRESELECT_EXISTING_CHANGELIST }, "changelist.preselect.existing")

    addBoolIfDiffers(set, conf, confDefault, { it.PERFORM_UPDATE_IN_BACKGROUND }, "perform.update.in.background")
    addBoolIfDiffers(set, conf, confDefault, { it.PERFORM_COMMIT_IN_BACKGROUND }, "perform.commit.in.background")
    addBoolIfDiffers(set, conf, confDefault, { it.PERFORM_EDIT_IN_BACKGROUND }, "perform.edit.in.background")
    addBoolIfDiffers(set, conf, confDefault, { it.PERFORM_CHECKOUT_IN_BACKGROUND }, "perform.checkout.in.background")
    addBoolIfDiffers(set, conf, confDefault, { it.PERFORM_ADD_REMOVE_IN_BACKGROUND }, "perform.add_remove.in.background")
    addBoolIfDiffers(set, conf, confDefault, { it.PERFORM_ROLLBACK_IN_BACKGROUND }, "perform.rollback.in.background")

    addBoolIfDiffers(set, conf, confDefault, { it.CHECK_CODE_SMELLS_BEFORE_PROJECT_COMMIT }, "commit.before.check.code.smell")
    addBoolIfDiffers(set, conf, confDefault, { it.CHECK_CODE_CLEANUP_BEFORE_PROJECT_COMMIT }, "commit.before.check.code.cleanup")
    addBoolIfDiffers(set, conf, confDefault, { it.CHECK_NEW_TODO }, "commit.before.check.todo")
    addBoolIfDiffers(set, conf, confDefault, { it.FORCE_NON_EMPTY_COMMENT }, "commit.before.check.non.empty.comment")
    addBoolIfDiffers(set, conf, confDefault, { it.OPTIMIZE_IMPORTS_BEFORE_PROJECT_COMMIT }, "commit.before.optimize.imports")
    addBoolIfDiffers(set, conf, confDefault, { it.REFORMAT_BEFORE_PROJECT_COMMIT }, "commit.before.reformat.project")
    addBoolIfDiffers(set, conf, confDefault, { it.REARRANGE_BEFORE_PROJECT_COMMIT }, "commit.before.rearrange")

    addBoolIfDiffers(set, conf, confDefault, { it.CLEAR_INITIAL_COMMIT_MESSAGE }, "commit.clear.initial.comment")
    addBoolIfDiffers(set, conf, confDefault, { it.USE_COMMIT_MESSAGE_MARGIN }, "commit.use.right.margin")
    addBoolIfDiffers(set, conf, confDefault, { it.SHOW_UNVERSIONED_FILES_WHILE_COMMIT }, "commit.show.unversioned")

    addBoolIfDiffers(set, conf, confDefault, { it.LOCAL_CHANGES_DETAILS_PREVIEW_SHOWN }, "show.changes.preview")
    addBoolIfDiffers(set, conf, confDefault, { it.INCLUDE_TEXT_INTO_SHELF }, "include.text.into.shelf")
    addBoolIfDiffers(set, conf, confDefault, { it.CHECK_LOCALLY_CHANGED_CONFLICTS_IN_BACKGROUND }, "check.conflicts.in.background")

    return set
  }

  companion object {
    private fun <T> addConfirmationIfDiffers(set: MutableSet<in MetricEvent>, settingsBean: T, defaultSettingsBean: T,
                                             valueFunction: Function1<T, VcsShowConfirmationOption.Value>, eventId: String) {
      addMetricIfDiffers(set, settingsBean, defaultSettingsBean, valueFunction) {
        val value = when (it) {
          VcsShowConfirmationOption.Value.SHOW_CONFIRMATION -> "ask"
          VcsShowConfirmationOption.Value.DO_NOTHING_SILENTLY -> "disabled"
          VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY -> "silently"
          else -> "unknown"
        }
        return@addMetricIfDiffers newMetric(eventId, value)
      }
    }
  }
}
