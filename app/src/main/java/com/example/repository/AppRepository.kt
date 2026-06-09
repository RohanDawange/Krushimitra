package com.example.repository

import com.example.BuildConfig
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppRepository(private val dao: AppDao) {

    // --- Local Database Mappings (Room) ---
    val farmerProfile: Flow<FarmerProfile?> = dao.getFarmerProfile()
    val allCrops: Flow<List<CropRecord>> = dao.getAllCrops()
    val allFinancials: Flow<List<FinancialLog>> = dao.getAllFinancials()
    val allCommunityPosts: Flow<List<CommunityPost>> = dao.getAllPosts()

    suspend fun saveFarmerProfile(profile: FarmerProfile) = withContext(Dispatchers.IO) {
        dao.saveFarmerProfile(profile)
    }

    suspend fun insertCrop(crop: CropRecord) = withContext(Dispatchers.IO) {
        dao.insertCrop(crop)
    }

    suspend fun deleteCrop(crop: CropRecord) = withContext(Dispatchers.IO) {
        dao.deleteCrop(crop)
    }

    suspend fun insertFinancial(log: FinancialLog) = withContext(Dispatchers.IO) {
        dao.insertFinancial(log)
    }

    suspend fun createCommunityPost(authorName: String, authorVillage: String, question: String) = withContext(Dispatchers.IO) {
        val newPost = CommunityPost(
            authorName = authorName,
            authorVillage = authorVillage,
            questionText = question,
            timestamp = System.currentTimeMillis()
        )
        dao.insertPost(newPost)
    }

    // --- Gemini AI Chatbot Support (Feature 2) ---
    suspend fun askGeminiAgent(prompt: String, languageCode: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getMockAnswer(prompt, languageCode)
        }

        val systemPromptStr = when (languageCode) {
            "mr" -> "तुम्ही 'कृषिमित्र' नावाचे एआय शेती मार्गदर्शक आहात. शेतकऱ्यांना पिकांवरील रोग, खते, कीटकनाशके, सिंचन आणि आधुनिक शेती तंत्रज्ञानाबद्दल सोप्या मराठी भाषेत अचूक आणि व्यवहारी सल्ला द्या."
            "hi" -> "आप 'कृषिमित्र' नामक एआई कृषि सहायक हैं। किसानों को फसलों की बीमारियों, सुरक्षा, खादों, सिंचाई और नवीनतम सरकारी योजनाओं पर हिंदी भाषा में सरल और उपयोगी सलाह दें।"
            else -> "You are 'KrushiMitra', a premier agricultural AI expert. Assist Indian farmers with precise, practical solutions about fertilizers, pest controls, crop rotations, water management, and government schemes in plain English."
        }

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(parts = listOf(GeminiPart(text = prompt)))
            ),
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPromptStr)))
        )

        return@withContext try {
            val response = RetrofitClient.geminiService.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: getMockAnswer(prompt, languageCode)
        } catch (e: Exception) {
            // Fallback gracefully to offline rule-based Q&A if network fails or API quota exceeded
            getMockAnswer(prompt, languageCode)
        }
    }

    // --- Multimodal Leaf Disease Detection (Feature 3) ---
    suspend fun detectLeafDisease(base64Image: String, languageCode: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getMockDiseaseReport(languageCode)
        }

        val analysisPrompt = "You are an expert plant pathologist. This leaf image belongs to an Indian farm crop. Look at it carefully and output: 1. Main Suspected Disease, 2. Key Symptoms Observed, 3. Comprehensive Remedies (Chemical and Organic), 4. Preventive Measures for the next cycle. Present the response beautifully in simple paragraphs using the language code: $languageCode."

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(
                        GeminiPart(text = analysisPrompt),
                        GeminiPart(inlineData = GeminiInlineData(mimeType = "image/jpeg", data = base64Image))
                    )
                )
            )
        )

        return@withContext try {
            val response = RetrofitClient.geminiService.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: getMockDiseaseReport(languageCode)
        } catch (e: Exception) {
            getMockDiseaseReport(languageCode)
        }
    }

    // --- Interactive OTP Simulation (Feature 1) ---
    suspend fun sendOtpToMobile(mobile: String): String = withContext(Dispatchers.IO) {
        // Simulates real SMS gateway call, returns the generated otp
        val generatedOtp = (100000..999999).random().toString()
        return@withContext generatedOtp
    }

    // --- Realistic Mandi Rates Dataset (Feature 5) ---
    fun getMandiPrices(cropType: String, searchQuery: String): List<MandiItem> {
        val masterList = listOf(
            MandiItem("1", "Soyabean", "Akola APMC", "Akola", 4100.0, 4650.0, 4420.0, 4390.0, true),
            MandiItem("2", "Soyabean", "Latur Mandi", "Latur", 4200.0, 4750.0, 4510.0, 4520.0, false),
            MandiItem("3", "Cotton", "Wardha APMC", "Wardha", 6700.0, 7400.0, 7120.0, 6980.0, true),
            MandiItem("4", "Cotton", "Amravati APMC", "Amravati", 6800.0, 7550.0, 7250.0, 7200.0, true),
            MandiItem("5", "Onion", "Lasalgaon APMC", "Nashik", 1800.0, 2400.0, 2150.0, 2200.0, false),
            MandiItem("6", "Onion", "Pimpalgaon Mandi", "Nashik", 1850.0, 2450.0, 2210.0, 2100.0, true),
            MandiItem("7", "Wheat", "Pune APMC", "Pune", 2300.0, 2800.0, 2550.0, 2560.0, false),
            MandiItem("8", "Wheat", "Nagpur APMC", "Nagpur", 2400.0, 2900.0, 2680.0, 2620.0, true),
            MandiItem("9", "Rice", "Gondia Mandi", "Gondia", 2500.0, 3100.0, 2850.0, 2800.0, true)
        )

        return masterList.filter {
            (cropType == "AL" || it.cropName.equals(cropType, ignoreCase = true)) &&
            (searchQuery.isEmpty() || it.mandiName.contains(searchQuery, ignoreCase = true) || it.district.contains(searchQuery, ignoreCase = true))
        }
    }

    // --- Weather Advisory Service (Feature 4) ---
    fun getCropWeatherForecast(district: String): WeatherInfo {
        val condition = if (district.contains("Nashik", true) || district.contains("Pune", true)) "Cloudy with Rain" else "Sunny"
        val rainAlert = if (condition.contains("Rain")) "ALERT: Heavy rain showers expected in 24 hours. Postpone chemical sprays." else null

        val dayNames = listOf("Fri", "Sat", "Sun", "Mon", "Tue", "Wed", "Thu")
        val tempsMax = listOf(32, 31, 29, 31, 33, 34, 34)
        val tempsMin = listOf(22, 21, 20, 21, 22, 23, 23)
        val conditions = listOf("Sunny", "Partly Cloudy", "Heavy Rain", "Light Showers", "Sunny", "Sunny", "Sunny")

        val days = List(7) { i ->
            ForecastDay(dayNames[i], tempsMax[i], tempsMin[i], conditions[i])
        }

        return WeatherInfo(
            temp = if (condition.contains("Rain")) 28 else 34,
            condition = condition,
            humidity = if (condition.contains("Rain")) 85 else 48,
            windSpeed = 12.5,
            rainProbability = if (condition.contains("Rain")) 90 else 10,
            rainAlert = rainAlert,
            dailyForecast = days
        )
    }

    // --- High-Value Government Schemes (Feature 6) ---
    fun getGovernmentSchemes(isMarathi: Boolean): List<GovScheme> {
        return listOf(
            GovScheme(
                title = if (isMarathi) "PM किसान सन्मान निधी योजना" else "PM Kisan Samman Nidhi",
                desc = if (isMarathi) "शेतकऱ्यांना जोड देण्यासाठी प्रतिवर्ष ६,००० रुपयांचे आर्थिक सहाय्य थेट बँक खात्यात वर्ग केले जाते." else "Central welfare giving standard income support of ₹6,000 per year in 3 direct bank installments to all landholding farmer families.",
                eligibility = if (isMarathi) "सर्व लहान व सीमांत शेतकरी कुटुंब (जमीन मालक)." else "Small and marginal farmers holding cultivable agricultural land under their name.",
                applyGuide = if (isMarathi) "१. पीएम किसान पोर्टल वर जा.\n२. आधार कार्ड आणि जमीन सातबारा उतारा जोडा.\n३. बँक खाते पडताळणी पूर्ण करा." else "1. Visit pmkisan.gov.in\n2. Open 'New Farmer Registration'\n3. Fill Aadhaar and upload land registry papers (7/12 land records).\n4. Submit bank account details."
            ),
            GovScheme(
                title = if (isMarathi) "मागेल त्याला शेततळे योजना" else "Magel Tyala Shettale (Maharashtra)",
                desc = if (isMarathi) "कोरडवाहू शेतीला संरक्षणात्मक पाणी देण्यासाठी शासनाकडून शेततळे बांधण्यासाठी ५०,००० हजार रुपयांचे थेट अनुदान." else "Maharashtra State scheme giving complete financial aid of ₹50,000 for building local farm ponds to protect dry crops.",
                eligibility = if (isMarathi) "किमान ०.६० हेक्टर जमीन मालकी आणि दारिद्र्यरेषेखालील (BPL) शेतकरी." else "Farmers in Maharashtra with minimum 0.60 hectares land size who own registered cultivable soil.",
                applyGuide = if (isMarathi) "१. 'महाडीबीटी' शेतकरी पोर्टल वर अर्ज करा.\n२. शेत जमीन नकाशा अपलोड करा.\n३. मंजुरी मिळाल्यानंतर शेततळ्याचे काम सुरू करा." else "1. Apply directly on MahaDBT Portal (mahadbt.maharashtra.gov.in).\n2. Register and upload land map verification records.\n3. Construction starts upon site inspection by agricultural extension officer."
            )
        )
    }

    // --- Fallbacks / Simulation Helper Methods ---
    private fun getMockAnswer(prompt: String, languageCode: String): String {
        return when (languageCode) {
            "mr" -> {
                if (prompt.contains("खत", true) || prompt.contains("fertilizer", true)) {
                    "खत सल्ला: पेरणीच्या वेळी प्रति एकर ५० किलो डीएपी (DAP) आणि २५ किलो म्युरेट ऑफ पोटॅश (MOP) द्या. कापूस उभा असताना ३० दिवसांनी युरिया टप्प्याटप्प्याने द्यावा.\nशेणखत किंवा गांडूळ खत जमिनीत वापरल्यास पिकाची रोगप्रतिकारक शक्ती आणि जमिनीची आर्द्रता टिकवून ठेवणारी क्षमता लक्षणीय वाढते."
                } else if (prompt.contains("कीड", true) || prompt.contains("pest", true)) {
                    "कीड नियंत्रण सल्ला: मावा व तुडतुडे नियंत्रणासाठी ५% निंबोळी अर्क फवारा. तीव्र प्रादुर्भावासाठी इमिडाक्लोप्रिड १७.८% एसएल ५ मिली प्रति १० लिटर पाण्यात मिसळून फवारणी करा.\nअति रासायनिक औषध फवारणी टाळा जेणेकरून मित्र कीटक जिवंत राहतील."
                } else {
                    "नमस्कार! कृषिमित्र एआय मध्ये आपले स्वागत आहे. मी आपल्याला पिकांवरील रोग नियंत्रण, योग्य खत व्यवस्थापन, आणि बाजार भावाबद्दल अचूक माहिती देण्यास सज्ज आहे. कृपया सविस्तर प्रश्न विचारा जेणेकरून मी उत्तम सल्ला देऊ शकेन!"
                }
            }
            "hi" -> {
                if (prompt.contains("खाद", true) || prompt.contains("fertilizer", true)) {
                    "उर्वरक सलाह: बुवाई के समय प्रति एकड़ 50 किलोग्राम डीएपी (DAP) और 25 किलोग्राम एमओपी (MOP) का छिड़काव करें। सोयाबीन में सल्फर युक्त जिप्सम का प्रयोग तिलहन क्षमता और तेल की मात्रा बढ़ाता है। जैविक खाद का नियमित उपयोग करें।"
                } else if (prompt.contains("कीट", true) || prompt.contains("pest", true)) {
                    "कीटनाशक सलाह: सफेद मक्खी और चूसने वाले कीटों के लिए नीम तेल (10,000 PPM) का छिड़काव करें। रासायनिक नियंत्रण हेतु थायामेथोक्सम 25% WG का 40 ग्राम प्रति एकड़ छिड़काव करना अत्यंत कारगर सिद्ध होता है।"
                } else {
                    "नमस्कार! कृषिमित्र एआई में आपका स्वागत है। मैं फसलों में रोग उपचार, उचित जल प्रबंधन, बाजार भाव और जैविक खेती के बारे में उत्कृष्ट जानकारी प्रदान कर सकता हूँ।"
                }
            }
            else -> {
                if (prompt.contains("fertilizer", true)) {
                    "Fertilizer Advice: Give 50 kg DAP and 25 kg Potash per acre during initial sowing. Integrate organic vermicompost to double microbial activity and soil aeration."
                } else if (prompt.contains("pest", true) || prompt.contains("disease", true)) {
                    "Pest Control: Keep crop clear of weed host plants. Apply chlorantraniliprole 18.5% SC at 60ml per acre to eradicate fall armyworm or stem borers."
                } else {
                    "Hello! Welcome to KrushiMitra Farmers' AI Assistant. Ask me questions about soils, fertilizers, pest control measures, or weather advisories."
                }
            }
        }
    }

    private fun getMockDiseaseReport(languageCode: String): String {
        return when (languageCode) {
            "mr" -> """
                📑 **एआय पीक रोग निदान अहवाल**
                
                ■ **संशयित रोग:** तांबेरा (Rust Disease) - सोयाबीन किंवा गहू
                ■ **निरीक्षण आणि लक्षणे:** पानांच्या खालच्या बाजूला आणि फांद्यांवर विटकरी तांबड्या रंगाचे बारीक ठिपके बुरशी स्वरूपात निर्माण झाले आहेत. यामुळे पानांचे अन्न बनविणे थांबून पाने लवकर वाळत आहेत.
                
                ■ **जैविक व सेंद्रिय उपाय:**
                १. प्रति एकर ५०० ग्रॅम ट्रायकोडर्मा व्हिरिडी बुरशीनाशक २०० लिटर पाण्यात मिसळून संपूर्ण पिकावर हलकी फवारणी करावी.
                २. पीक फेरपालट पद्धती अंमलात आणा जेणेकरून पुढील हंगामात याचा प्रादुर्भाव होणार नाही.
                
                ■ **रासायनिक उपाय (तातडीचे नियंत्रण):**
                १. प्रादुर्भाव वाढल्यास टेब्युकोनॅझोल २५.९ % ईसी हे बुरशीनाशक १० ते १५ मिली प्रति १० लिटर पाण्यात मिसळून स्वच्छ हवामानात फवारणी करा.
            """.trimIndent()
            "hi" -> """
                📑 **एआई फसल रोग निदान रिपोर्ट**
                
                ■ **संभावित रोग:** रतुआ (Rust Disease) - सोयाबीन या गेहूं
                ■ **लक्षण:** पत्तियों के निचले भाग में भूरे रंग के छोटे धब्बे उभर आते हैं। प्रकाश संश्लेषण प्रभावित होने के कारण फसल कमजोर और पीली पड़ने लगती है।
                
                ■ **जैविक उपाय:**
                १. ट्राइकोडर्मा विरिडी जैव कवकनाशी (500 ग्राम प्रति एकड़) को पानी में मिलाकर पत्तियों पर छिड़काव करें।
                
                ■ **रासायनिक उपाय:**
                १. टेबुकोनाज़ोल 25.9% EC का 10-15 मिली प्रति 10 लीटर पानी के अनुपात में पत्तियों पर तत्काल छिड़काव करें।
            """.trimIndent()
            else -> """
                📑 **AI Plant Pathology Diagnostics**
                
                ■ **Suspected Disease:** Soybean/Wheat Leaf Rust (Puccinia species)
                ■ **Symptoms:** Powdery rust-colored pustules appearing on the lower surfaces of mature leaves, causing premature yellowing and drying.
                
                ■ **Organic/Biological Remedy:**
                1. Foliar spray of Trichoderma Viride (5g/Litre) to build fungal resilience.
                
                ■ **Chemical Action Plan:**
                1. Spray Tebuconazole 25.9% EC @ 10ml to 12ml in 10 Litre of fresh water immediately under clear dry skies.
            """.trimIndent()
        }
    }
}

data class GovScheme(
    val title: String,
    val desc: String,
    val eligibility: String,
    val applyGuide: String
)
