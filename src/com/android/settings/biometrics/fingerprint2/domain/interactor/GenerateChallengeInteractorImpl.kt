/*
 * Copyright (C) 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.biometrics.fingerprint2.domain.interactor

import android.content.Intent
import android.hardware.fingerprint.FingerprintManager
import com.android.settings.biometrics.GatekeeperPasswordProvider
import com.android.settings.biometrics.fingerprint2.data.repository.UserRepo
import com.android.settings.biometrics.fingerprint2.lib.domain.interactor.GenerateChallengeInteractor
import com.android.settings.password.ChooseLockSettingsHelper
import kotlinx.coroutines.flow.first
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class GenerateChallengeInteractorImpl(
  private val fingerprintManager: FingerprintManager,
  private val userRepo: UserRepo,
  private val gatekeeperPasswordProvider: GatekeeperPasswordProvider,
) : GenerateChallengeInteractor {

  override suspend fun generateChallenge(gateKeeperPasswordHandle: Long): Pair<Long, ByteArray> {
    val userId = userRepo.currentUser.first()
    return suspendCoroutine {
      val callback =
        FingerprintManager.GenerateChallengeCallback { _, userId, challenge ->
          val intent = Intent()
          intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_GK_PW_HANDLE, gateKeeperPasswordHandle)
          val challengeToken =
            gatekeeperPasswordProvider.requestGatekeeperHat(intent, challenge, userId)

          gatekeeperPasswordProvider.removeGatekeeperPasswordHandle(intent, false)
          val p = Pair(challenge, challengeToken)
          it.resume(p)
        }
      fingerprintManager.generateChallenge(userId, callback)
    }
  }
}
