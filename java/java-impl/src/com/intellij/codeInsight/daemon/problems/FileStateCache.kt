// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.codeInsight.daemon.problems

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.LowMemoryWatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMember
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.util.containers.SLRUMap

class FileStateCache : Disposable {

  private val cache: SLRUMap<SmartPsiElementPointer<PsiFile>, PrivateFileState> = SLRUMap(100, 50)

  object SERVICE {
    fun getInstance(project: Project): FileStateCache = ServiceManager.getService(project, FileStateCache::class.java)
  }

  init {
    LowMemoryWatcher.register(Runnable { cache.clear() }, this)
  }

  internal fun getState(psiFile: PsiFile): FileState? {
    return cache.get(SmartPointerManager.createPointer(psiFile))?.toFileState()
  }

  internal fun setState(psiFile: PsiFile, snapshot: Snapshot, changes: Map<PsiMember, ScopedMember?>) {
    return cache.put(SmartPointerManager.createPointer(psiFile), PrivateFileState.create(snapshot, changes))
  }

  private data class PrivateFileState(
    val snapshot: Snapshot,
    val changePointers: Map<SmartPsiElementPointer<PsiMember>, ScopedMember?>
  ) {

    fun toFileState(): FileState {
      val changes: Map<PsiMember, ScopedMember?> = changePointers.asSequence()
        .mapNotNull { (memberPointer, prevMember) -> memberPointer.element?.let { it to prevMember } }
        .toMap()
      return FileState(snapshot, changes)
    }

    companion object {
      fun create(snapshot: Snapshot, changes: Map<PsiMember, ScopedMember?>): PrivateFileState {
        val changePointers: Map<SmartPsiElementPointer<PsiMember>, ScopedMember?> = changes.entries.asSequence()
          .map { (psiMember, prevMember) -> SmartPointerManager.createPointer(psiMember) to prevMember }
          .toMap()
        return PrivateFileState(snapshot, changePointers)
      }
    }
  }

  override fun dispose() {}
}