package com.malaria

import com.malaria.domain.usecases.AnalyzeImageUseCase import com.malaria.data.repository.AnalysisRepository import com.malaria.data.api.MLApiService import io.ktor.client.HttpClient import io.ktor.client.engine.cio.CIO import kotlinx.coroutines.runBlocking import java.io.File

fun main() = runBlocking { val client = HttpClient(CIO) val apiService = MLApiService(client = client) val repository = AnalysisRepository(apiService) val useCase = AnalyzeImageUseCase(repository)

    val imageFile = File("ml/training/data/test/parasitized/C100P61ThinF_IMG_20150918_144104_cell_163.png")
    val result = useCase(imageFile)
    println("Diagnosis: ${result.diagnosisText}, Confidence: ${result.confidencePercentage}%")

}