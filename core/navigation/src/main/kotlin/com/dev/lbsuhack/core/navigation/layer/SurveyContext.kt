package com.dev.lbsuhack.core.navigation.layer

import kotlinx.coroutines.flow.StateFlow

data class SurveyProgress(
    val currentStep: Int,
    val totalSteps: Int,
)

interface SurveyContext {
    val answers: StateFlow<Map<String, String>>
    val progress: StateFlow<SurveyProgress>
    fun submit(stepId: String, answer: String)
    fun reset()
}
