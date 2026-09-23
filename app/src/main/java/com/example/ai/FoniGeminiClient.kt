package com.example.ai

import android.util.Log
import com.example.BuildConfig
import com.example.model.CardActionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Cliente de Inteligencia Artificial para Foni (Dalefon).
 * Se conecta a la API de Gemini (gemini-3.5-flash) inyectando la Base de Conocimiento oficial.
 * Si no hay clave de API configurada o no hay conexión, utiliza FoniKnowledgeEngine local.
 */
object FoniGeminiClient {

    private const val TAG = "FoniGeminiClient"
    private const val MODEL = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun getResponse(userMessage: String): Pair<String, CardActionType> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        // Si la clave no está configurada o es el placeholder por defecto, usamos el motor local
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.d(TAG, "Utilizando FoniKnowledgeEngine local.")
            return@withContext FoniKnowledgeEngine.generateResponse(userMessage)
        }

        try {
            val requestJson = JSONObject().apply {
                // System Instruction con la identidad y base de conocimiento de Foni
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", FoniKnowledgeBase.SYSTEM_PROMPT)
                        })
                    })
                })

                // Contents con el mensaje del usuario
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", userMessage)
                            })
                        })
                    })
                })

                // Generation config para respuestas concisas y dinámicas
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.6)
                    put("topP", 0.95)
                    put("topK", 40)
                })
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = requestJson.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(body)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && !responseBody.isNullOrEmpty()) {
                val json = JSONObject(responseBody)
                val candidates = json.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val text = parts.getJSONObject(0).optString("text")
                        if (text.isNotBlank()) {
                            val cardAction = detectCardAction(text, userMessage)
                            return@withContext Pair(text.trim(), cardAction)
                        }
                    }
                }
            } else {
                Log.w(TAG, "Error en respuesta Gemini: HTTP ${response.code} $responseBody")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Excepción al consultar Gemini API: ${e.message}")
        }

        // Respaldo confiable con el motor oficial de conocimiento
        FoniKnowledgeEngine.generateResponse(userMessage)
    }

    private fun detectCardAction(aiResponse: String, userQuery: String): CardActionType {
        val lower = (aiResponse + " " + userQuery).lowercase()
        return when {
            lower.contains("paquete") || lower.contains("gigas") || lower.contains("dale con todo") -> CardActionType.PLANS_CATALOG
            lower.contains("activar") || lower.contains("esim") || lower.contains("activa tu línea") -> CardActionType.ESIM_ACTIVATION
            lower.contains("saldo") || lower.contains("bolsa") || lower.contains("mi cuenta") -> CardActionType.ACCOUNT_SUMMARY
            lower.contains("apn") || lower.contains("punto de acceso") || lower.contains("configurar internet") -> CardActionType.APN_SETTINGS
            else -> CardActionType.NONE
        }
    }
}
