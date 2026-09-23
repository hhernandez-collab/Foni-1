package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.DalefonAiState
import com.example.model.InteractionMode
import com.example.viewmodel.DalefonViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Dalefon", appName)
  }

  @Test
  fun `verify dalefon viewmodel initial state and mode toggle`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = DalefonViewModel(app)

    // Initial mode should be VOICE (Siri-inspired)
    assertEquals(InteractionMode.VOICE, viewModel.interactionMode.value)
    assertEquals(DalefonAiState.IDLE, viewModel.aiState.value)

    // Toggle to CHAT mode
    viewModel.toggleInteractionMode()
    assertEquals(InteractionMode.CHAT, viewModel.interactionMode.value)

    // Toggle back to VOICE mode
    viewModel.toggleInteractionMode()
    assertEquals(InteractionMode.VOICE, viewModel.interactionMode.value)
  }

  @Test
  fun `verify telecom plans catalog loaded`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = DalefonViewModel(app)

    val plans = viewModel.plans.value
    assertTrue("Plans should contain items", plans.isNotEmpty())
    val popularPlan = plans.find { it.isPopular }
    assertNotNull("Should have a popular plan", popularPlan)
    assertEquals("Dale 150 (Promo Portabilidad)", popularPlan?.name)
  }

  @Test
  fun `verify foni knowledge engine telecom responses`() {
    val (greetingResponse, _) = com.example.ai.FoniKnowledgeEngine.generateResponse("Hola")
    assertTrue("Should introduce Foni", greetingResponse.contains("Foni"))

    val (portabilityResponse, _) = com.example.ai.FoniKnowledgeEngine.generateResponse("Quiero hacer portabilidad")
    assertTrue("Should mention NIP and 051", portabilityResponse.contains("051") || portabilityResponse.contains("NIP"))

    val (apnResponse, _) = com.example.ai.FoniKnowledgeEngine.generateResponse("Configurar APN")
    assertTrue("Should mention APN", apnResponse.contains("APN"))
  }


  @Test
  fun `verify dalefon ai character states`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = DalefonViewModel(app)

    viewModel.setAiState(DalefonAiState.IDLE)
    assertEquals(DalefonAiState.IDLE, viewModel.aiState.value)
    assertEquals("Base", viewModel.aiState.value.label)

    viewModel.setAiState(DalefonAiState.LISTENING)
    assertEquals(DalefonAiState.LISTENING, viewModel.aiState.value)
    assertEquals("Escuchando", viewModel.aiState.value.label)

    viewModel.setAiState(DalefonAiState.PROCESSING)
    assertEquals(DalefonAiState.PROCESSING, viewModel.aiState.value)
    assertEquals("Procesando", viewModel.aiState.value.label)

    viewModel.setAiState(DalefonAiState.THINKING)
    assertEquals(DalefonAiState.THINKING, viewModel.aiState.value)
    assertEquals("Pensando", viewModel.aiState.value.label)

    viewModel.setAiState(DalefonAiState.RESPONDING)
    assertEquals(DalefonAiState.RESPONDING, viewModel.aiState.value)
    assertEquals("Respondiendo", viewModel.aiState.value.label)
  }

  @Test
  fun `verify voice conversation mode activation on mic click`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = DalefonViewModel(app)

    assertEquals(false, viewModel.isVoiceConversationActive.value)
    assertEquals(false, viewModel.isLiveAudioMode.value)

    viewModel.startVoiceListening()
    assertEquals(true, viewModel.isVoiceConversationActive.value)
    assertEquals(true, viewModel.isLiveAudioMode.value)
    assertEquals(DalefonAiState.LISTENING, viewModel.aiState.value)

    viewModel.stopVoiceListening()
    assertEquals(false, viewModel.isVoiceConversationActive.value)
    assertEquals(false, viewModel.isLiveAudioMode.value)
    assertEquals(DalefonAiState.IDLE, viewModel.aiState.value)
  }

  @Test
  fun `verify assistant interruption returns to listening state`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = DalefonViewModel(app)

    viewModel.startVoiceListening()
    viewModel.setAiState(DalefonAiState.RESPONDING)
    assertEquals(DalefonAiState.RESPONDING, viewModel.aiState.value)

    // User interrupts Foni
    viewModel.interruptAssistant()
    assertEquals(DalefonAiState.LISTENING, viewModel.aiState.value)
    assertTrue("Transcript should indicate listening", viewModel.currentVoiceTranscript.value.contains("Te escucho"))
  }
}


