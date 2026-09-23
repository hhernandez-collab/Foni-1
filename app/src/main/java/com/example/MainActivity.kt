package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.InteractionMode
import com.example.ui.components.AccountSummarySheet
import com.example.ui.components.ApnSettingsSheet
import com.example.ui.components.ESimActivationSheet
import com.example.ui.components.PlansCatalogSheet
import com.example.ui.screens.DalefonChatScreen
import com.example.ui.screens.DalefonVoiceScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.DalefonViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: DalefonViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme(darkTheme = true) {
        DalefonApp(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun DalefonApp(viewModel: DalefonViewModel) {
  val context = LocalContext.current
  val interactionMode by viewModel.interactionMode.collectAsStateWithLifecycle()
  val showPlans by viewModel.showPlansSheet.collectAsStateWithLifecycle()
  val showEsim by viewModel.showEsimSheet.collectAsStateWithLifecycle()
  val showAccount by viewModel.showAccountSheet.collectAsStateWithLifecycle()
  val showApn by viewModel.showApnSheet.collectAsStateWithLifecycle()
  val plans by viewModel.plans.collectAsStateWithLifecycle()
  val account by viewModel.userAccount.collectAsStateWithLifecycle()

  // Permiso de micrófono para entrada de voz
  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { _ ->
    // Manejo de resultado del permiso de audio
  }

  LaunchedEffect(Unit) {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
      permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }
  }

  Scaffold(
    modifier = Modifier.fillMaxSize()
  ) { _ ->
    // Transición suave entre Modo Voz (Siri) y Modo Chat Escrito
    Crossfade(
      targetState = interactionMode,
      animationSpec = tween(durationMillis = 350),
      label = "mode_crossfade"
    ) { mode ->
      when (mode) {
        InteractionMode.VOICE -> DalefonVoiceScreen(viewModel = viewModel)
        InteractionMode.CHAT -> DalefonChatScreen(viewModel = viewModel)
      }
    }

    // Modal Bottom Sheets para procesos de telecomunicaciones
    if (showPlans) {
      PlansCatalogSheet(
        plans = plans,
        onDismiss = { viewModel.dismissPlansSheet() },
        onPlanPurchased = { plan ->
          viewModel.onPlanPurchased(plan)
        }
      )
    }

    if (showEsim) {
      ESimActivationSheet(
        onDismiss = { viewModel.dismissEsimSheet() },
        onActivationComplete = { details ->
          viewModel.onSimActivated(details)
        }
      )
    }

    if (showAccount) {
      AccountSummarySheet(
        account = account,
        onDismiss = { viewModel.dismissAccountSheet() },
        onRechargeClick = {
          viewModel.dismissAccountSheet()
          viewModel.showPlansCatalog()
        }
      )
    }

    if (showApn) {
      ApnSettingsSheet(
        onDismiss = { viewModel.dismissApnSheet() }
      )
    }
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Dalefon AI: $name", modifier = modifier)
}

