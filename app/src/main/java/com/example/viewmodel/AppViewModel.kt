package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.repository.AppRepository
import com.example.ui.locale.AppLanguage
import com.example.repository.GovScheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application.applicationContext, viewModelScope)
    private val repository = AppRepository(database.dao())

    // --- State Observables ---
    val userProfile: StateFlow<FarmerProfile?> = repository.farmerProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val cropsList: StateFlow<List<CropRecord>> = repository.allCrops
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val financialsList: StateFlow<List<FinancialLog>> = repository.allFinancials
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val communityPosts: StateFlow<List<CommunityPost>> = repository.allCommunityPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Local Live UI State Managers ---
    private val _currentLanguage = MutableStateFlow(AppLanguage.MARATHI)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // --- Login Flow State --
    val authProgress = MutableStateFlow(false)
    val authSmsSent = MutableStateFlow(false)
    val verifiedOtp = MutableStateFlow("")
    val generatedOtpCode = MutableStateFlow("")

    // --- Chatbot Screen States ---
    data class ChatMessage(val text: String, val isUser: Boolean, val timestamp: Long = System.currentTimeMillis())
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()
    val chatLoading = MutableStateFlow(false)

    // --- Leaf Scan States ---
    val leafReport = MutableStateFlow<String?>(null)
    val leafScanLoading = MutableStateFlow(false)
    val selectedBitmapBase64 = MutableStateFlow<String?>(null)

    // --- Mandi Filtering live states ---
    val selectedCropType = MutableStateFlow("AL") // 'AL' stands for All Crops
    val mandiSearchQuery = MutableStateFlow("")

    init {
        // Automatically set the app's initial language based on profile if saved
        viewModelScope.launch {
            userProfile.collect { profile ->
                if (profile != null) {
                    _isLoggedIn.value = true
                    val lang = when (profile.selectedLanguage) {
                        "ENGLISH" -> AppLanguage.ENGLISH
                        "HINDI" -> AppLanguage.HINDI
                        else -> AppLanguage.MARATHI
                    }
                    _currentLanguage.value = lang
                }
            }
        }
        
        // Add default chat greeting on startup
        resetChatMessages()
    }

    fun resetChatMessages() {
        val welcome = when (_currentLanguage.value) {
            AppLanguage.MARATHI -> "नमस्कार शेतकरी बंधूंनो! मी आपला डिजीटल कृषिमित्र सल्लागार आहे. मला आपल्या पिकांबद्दल, खतांविषयी किंवा येणाऱ्या रोगराई बद्दल विचारा."
            AppLanguage.HINDI -> "नमस्कार किसान भाइयों! मैं आपका 'कृषिमित्र' एआई सहायक हूँ। अपनी फसल, खाद, सिंचाई या बीमारी के उपचार से संबंधित कोई भी सवाल पूछें।"
            AppLanguage.ENGLISH -> "Hello Farmers! I am your KrushiMitra AI assistant. Ask me questions about crops, organic manure, fertilizers, pest control & mandi rates."
        }
        _chatMessages.value = listOf(ChatMessage(welcome, isUser = false))
    }

    fun toggleLanguage(lang: AppLanguage) {
        _currentLanguage.value = lang
        // Save to DB if profile exists
        viewModelScope.launch {
            val current = database.dao().getFarmerProfileSync()
            if (current != null) {
                repository.saveFarmerProfile(
                    current.copy(selectedLanguage = lang.name)
                )
            }
        }
        resetChatMessages()
    }

    // --- Simulation of Authentication ---
    fun sendLoginOtp(mobile: String) {
        if (mobile.length == 10) {
            viewModelScope.launch {
                authProgress.value = true
                val code = repository.sendOtpToMobile(mobile)
                generatedOtpCode.value = code
                authSmsSent.value = true
                authProgress.value = false
            }
        }
    }

    fun verifyEnteredOtp(otp: String, mobileNumber: String) {
        if (otp == generatedOtpCode.value || otp == "123456" /* Backdoor demo bypass */) {
            verifiedOtp.value = otp
            // Login profile exists check
            viewModelScope.launch {
                val existing = database.dao().getFarmerProfileSync()
                if (existing != null) {
                    _isLoggedIn.value = true
                }
            }
        }
    }

    fun createNewProfile(name: String, mobile: String, village: String, district: String, size: Double, crops: String) {
        viewModelScope.launch {
            val prof = FarmerProfile(
                fullName = name,
                mobileNumber = mobile,
                village = village,
                district = district,
                landArea = size,
                primaryCrops = crops,
                selectedLanguage = _currentLanguage.value.name
            )
            repository.saveFarmerProfile(prof)
            _isLoggedIn.value = true
        }
    }

    // --- Crop Records & Income/Expense Actions ---
    fun addCropCycle(cropName: String, sowDate: String, predictedHarvest: Double) {
        viewModelScope.launch {
            repository.insertCrop(
                CropRecord(
                    cropName = cropName,
                    sowingDate = sowDate,
                    expectedHarvest = predictedHarvest
                )
            )
        }
    }

    fun deleteCropCycle(record: CropRecord) {
        viewModelScope.launch {
            repository.deleteCrop(record)
        }
    }

    fun addFinancialLog(category: String, amt: Double, type: String, description: String) {
        viewModelScope.launch {
            val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            repository.insertFinancial(
                FinancialLog(
                    category = category,
                    amount = amt,
                    type = type,
                    date = todayDate,
                    description = description
                )
            )
        }
    }

    // --- Chatbot Support Actions ---
    fun submitAgentQuery(question: String) {
        if (question.isBlank()) return

        val userMsg = ChatMessage(question, isUser = true)
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            chatLoading.value = true
            val response = repository.askGeminiAgent(question, _currentLanguage.value.code)
            
            _chatMessages.value = _chatMessages.value + ChatMessage(response, isUser = false)
            chatLoading.value = false
        }
    }

    // --- Diseased Leaf Scan Diagnosis ---
    fun triggerImageScan(base64Image: String) {
        selectedBitmapBase64.value = base64Image
        viewModelScope.launch {
            leafScanLoading.value = true
            val diagnosticResult = repository.detectLeafDisease(base64Image, _currentLanguage.value.code)
            leafReport.value = diagnosticResult
            leafScanLoading.value = false
        }
    }

    fun clearLeafScan() {
        selectedBitmapBase64.value = null
        leafReport.value = null
    }

    // --- Community Forum Actions ---
    fun postCommunityQuestion(questionText: String) {
        if (questionText.isBlank()) return
        viewModelScope.launch {
            val currentProf = database.dao().getFarmerProfileSync()
            val author = currentProf?.fullName ?: "Krishi Mitra"
            val village = currentProf?.village ?: "Maharashtra"
            repository.createCommunityPost(author, village, questionText)
        }
    }

    // --- Data Fetch Methods ---
    fun fetchMandiPrices(): List<MandiItem> {
        return repository.getMandiPrices(selectedCropType.value, mandiSearchQuery.value)
    }

    fun fetchWeatherDetails(): WeatherInfo {
        val currentDistrict = userProfile.value?.district ?: "Nashik"
        return repository.getCropWeatherForecast(currentDistrict)
    }

    fun fetchAvailableSchemes(): List<GovScheme> {
        val isMarathi = _currentLanguage.value == AppLanguage.MARATHI || _currentLanguage.value == AppLanguage.HINDI
        return repository.getGovernmentSchemes(isMarathi)
    }

    // Logout
    fun logoutFarmer() {
        viewModelScope.launch {
            _isLoggedIn.value = false
            authSmsSent.value = false
            verifiedOtp.value = ""
            // Clear current database profile for a clear fresh login demo
            val db = AppDatabase.getDatabase(getApplication(), viewModelScope)
            db.clearAllTables()
            resetChatMessages()
        }
    }
}

// Factor Companion helper
class AppViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
