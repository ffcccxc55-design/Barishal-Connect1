package com.example.data.repository

import com.example.data.local.DirectoryDao
import com.example.data.model.CitizenReport
import com.example.data.model.DirectoryItem
import com.example.data.model.UserActivity
import com.example.data.remote.ApiService
import kotlinx.coroutines.flow.Flow

class BarishalRepository(private val dao: DirectoryDao) {

    private val retrofit = retrofit2.Retrofit.Builder()
        .baseUrl("https://script.google.com/")
        .addConverterFactory(retrofit2.converter.moshi.MoshiConverterFactory.create(
            com.squareup.moshi.Moshi.Builder()
                .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                .build()
        ))
        .client(
            okhttp3.OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .build()
        )
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)

    suspend fun syncDataFromSheets(sheetsUrl: String): Boolean {
        return try {
            val response = apiService.getDirectoryItems(sheetsUrl, "getDirectoryItems")
            if (response.isSuccessful && response.body()?.success == true) {
                val remoteItems = response.body()?.data
                if (!remoteItems.isNullOrEmpty()) {
                    dao.insertItems(remoteItems)
                    return true
                }
            }
            false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun syncRoadsFromSheets(sheetsUrl: String): Boolean {
        return try {
            val response = apiService.getSmartRoads(sheetsUrl, "getRoads")
            if (response.isSuccessful && response.body()?.success == true) {
                val remoteRoads = response.body()?.data
                if (!remoteRoads.isNullOrEmpty()) {
                    remoteRoads.forEach { dao.insertSmartRoad(it) }
                    return true
                }
            }
            false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun postDirectoryItemToSheets(sheetsUrl: String, item: DirectoryItem): Boolean {
        return try {
            val itemMap = mapOf(
                "id" to item.id,
                "category" to item.category,
                "title" to item.title,
                "subtitle" to item.subtitle,
                "description" to item.description,
                "location" to item.location,
                "contactPhone" to item.contactPhone,
                "rating" to item.rating,
                "priceOrFee" to item.priceOrFee,
                "statusOrSchedule" to item.statusOrSchedule,
                "imageUrl" to item.imageUrl,
                "status" to "PENDING",
                "contributor" to item.contributor,
                "district" to item.district,
                "upazila" to item.upazila,
                "unionName" to item.unionName
            )
            val requestBody = mapOf(
                "action" to "addDirectoryItem",
                "item" to itemMap
            )
            val response = apiService.postAction(sheetsUrl, requestBody)
            response.isSuccessful && response.body()?.success == true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun postSmartRoadToSheets(sheetsUrl: String, road: com.example.data.model.SmartRoad): Boolean {
        return try {
            val roadMap = mapOf(
                "id" to road.id,
                "name" to road.name,
                "category" to road.category,
                "width" to road.width,
                "condition" to road.condition,
                "description" to road.description,
                "startPoint" to road.startPoint,
                "endPoint" to road.endPoint,
                "coordinatesJson" to road.coordinatesJson,
                "district" to road.district,
                "upazila" to road.upazila,
                "unionName" to road.unionName,
                "status" to "PENDING",
                "contributor" to road.contributor,
                "distance" to road.distance,
                "durationSeconds" to road.durationSeconds
            )
            val requestBody = mapOf(
                "action" to "addSmartRoad",
                "road" to roadMap
            )
            val response = apiService.postAction(sheetsUrl, requestBody)
            response.isSuccessful && response.body()?.success == true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    val allItems: Flow<List<DirectoryItem>> = dao.getAllItems()
    val favoriteItems: Flow<List<DirectoryItem>> = dao.getFavoriteItems()
    val citizenReports: Flow<List<CitizenReport>> = dao.getAllCitizenReports()
    val activities: Flow<List<UserActivity>> = dao.getAllActivities()
    val allSmartRoads: Flow<List<com.example.data.model.SmartRoad>> = dao.getAllSmartRoads()

    suspend fun insertSmartRoad(road: com.example.data.model.SmartRoad) {
        dao.insertSmartRoad(road)
    }

    suspend fun deleteSmartRoad(id: String) {
        dao.deleteSmartRoad(id)
    }

    suspend fun insertItems(items: List<DirectoryItem>) {
        dao.insertItems(items)
    }

    suspend fun deleteItem(id: String) {
        dao.deleteItem(id)
    }

    fun getItemsByCategory(category: String): Flow<List<DirectoryItem>> {
        return dao.getItemsByCategory(category)
    }

    fun searchItems(query: String): Flow<List<DirectoryItem>> {
        return dao.searchItems(query)
    }

    suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean) {
        dao.updateFavoriteStatus(id, isFavorite)
    }

    suspend fun insertCitizenReport(report: CitizenReport) {
        dao.insertCitizenReport(report)
    }

    suspend fun updateCitizenReport(report: CitizenReport) {
        dao.updateCitizenReport(report)
    }

    suspend fun deleteCitizenReport(id: Int) {
        dao.deleteCitizenReport(id)
    }

    suspend fun insertActivity(activity: UserActivity) {
        dao.insertActivity(activity)
    }

    suspend fun clearActivities() {
        dao.clearActivities()
    }

    suspend fun initializeDataIfNeeded() {
        if (dao.getItemsCount() == 0) {
            val sampleItems = listOf(
                // Road Items
                DirectoryItem(
                    id = "road_1",
                    category = "road",
                    title = "Dhaka - Barishal Highway (N8)",
                    subtitle = "Bhola Mor to Barishal Central Terminal",
                    description = "Four-lane highway connecting Dhaka and Barishal via Padma Bridge. Well paved, fast track.",
                    statusOrSchedule = "সচল ও স্বাভাবিক (Smooth)",
                    location = "N8 Highway, Barishal Division",
                    rating = 4.8f,
                    imageUrl = "https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?auto=format&fit=crop&q=80&w=600"
                ),
                DirectoryItem(
                    id = "road_2",
                    category = "road",
                    title = "Barishal - Kuakata Highway",
                    subtitle = "Near Payra (Lebukhali) Bridge",
                    description = "Scenic single highway but slow near curves and tourist weekends. Payra Bridge section is smooth.",
                    statusOrSchedule = "ধীরগতি (Slow Weekend Traffic)",
                    location = "Payra Bridge Section",
                    rating = 4.5f,
                    imageUrl = "https://images.unsplash.com/photo-1511527661048-7fe73d85e9a4?auto=format&fit=crop&q=80&w=600"
                ),
                DirectoryItem(
                    id = "road_3",
                    category = "road",
                    title = "Sadar Road, Barishal City",
                    subtitle = "Chowmatha to Town Hall Area",
                    description = "Main commercial route inside Barishal. Expect standard urban gridlock during peak hours.",
                    statusOrSchedule = "মাঝারি জ্যাম (Moderate Traffic)",
                    location = "Sadar, Barishal Town",
                    rating = 3.9f,
                    imageUrl = "https://images.unsplash.com/photo-1568605117036-5fe5e7bab0b7?auto=format&fit=crop&q=80&w=600"
                ),
                
                // Worker Directory
                DirectoryItem(
                    id = "worker_1",
                    category = "worker",
                    title = "মো: বেলাল হোসেন (Md. Belal)",
                    subtitle = "অভিজ্ঞ ইলেকট্রিশিয়ান (Electrician)",
                    description = "বাসা-বাড়ির ওয়ারিং, আইপিএস ফিটিং, ফ্যান/এসি সার্ভিসিং ও মোটর মেরামতের নিখুঁত কাজ করা হয়।",
                    contactPhone = "01712-345678",
                    location = "রুপাতলী, বরিশাল (Rupatali)",
                    priceOrFee = "৩৫০ টাকা (ভিজিট ফি)",
                    rating = 4.9f,
                    imageUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&q=80&w=200"
                ),
                DirectoryItem(
                    id = "worker_2",
                    category = "worker",
                    title = "আল-আমীন শেখ (Al-Amin Sheikh)",
                    subtitle = "প্লাম্বার ও স্যানিটারি টেকনিশিয়ান",
                    description = "১০+ বছরের অভিজ্ঞতা। বাথরুম ফিটিংস, টাইলস মেরামত এবং পানির পাইপলাইন ফিক্সিংয়ে পারদর্শী।",
                    contactPhone = "01823-987654",
                    location = "নতুন বাজার, বরিশাল (Natun Bazar)",
                    priceOrFee = "৩০০ টাকা (ভিজিট ফি)",
                    rating = 4.7f,
                    imageUrl = "https://images.unsplash.com/photo-1621905251189-08b45d6a269e?auto=format&fit=crop&q=80&w=200"
                ),
                DirectoryItem(
                    id = "worker_3",
                    category = "worker",
                    title = "মিলন হালদার (Milon Haldar)",
                    subtitle = "প্রফেশনাল রাজমিস্ত্রি ও মেঝের টাইলস মিস্ত্রি",
                    description = "বিল্ডিং কন্সট্রাকশন, ছাদ ঢালাই, ইটের গাথুনি এবং মার্বেল পাথর ও আধুনিক মেঝের ডেকোরেশন স্পেশালিস্ট।",
                    contactPhone = "01911-223344",
                    location = "সাগড়দী, বরিশাল (Sagardi)",
                    priceOrFee = "৬০০ টাকা / দিন (মজুরি)",
                    rating = 4.8f,
                    imageUrl = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?auto=format&fit=crop&q=80&w=200"
                ),

                // Local Jobs
                DirectoryItem(
                    id = "job_1",
                    category = "job",
                    title = "কম্পিউটার অপারেটর ও রিসেপশনিস্ট",
                    subtitle = "বেলভিউ ডায়াগনস্টিক অ্যান্ড মেডিকেল সেন্টার",
                    description = "কম্পিউটার টাইপিং (বাংলা ও ইংরেজি), ইন্টারনেট এবং এক্সেল সিট চালনায় পারদর্শী হতে হবে। ১ বছরের অভিজ্ঞতা আবশ্যক।",
                    contactPhone = "01311-224466",
                    location = "সদর রোড, বরিশাল (Band Road)",
                    priceOrFee = "১৫,০০০ টাকা / মাস",
                    statusOrSchedule = "ফুল-টাইম (৮:০০ AM - ৪:০০ PM)",
                    rating = 4.2f,
                    imageUrl = "https://images.unsplash.com/photo-1521791136368-1a46827d0515?auto=format&fit=crop&q=80&w=200"
                ),
                DirectoryItem(
                    id = "job_2",
                    category = "job",
                    title = "ডেলিভারি ম্যান / সেলস রিপ্রেজেন্টেটিভ",
                    subtitle = "প্রাণ-আরএফএল গ্রুপ (বরিশাল ব্রাঞ্চ)",
                    description = "বরিশাল শহরের বিভিন্ন রিটেইল শপে অর্ডার সংগ্রহ ও ডেলিভারি নিশ্চিত করতে হবে। নিজস্ব বাইসাইকেল বা মোটরসাইকেল থাকতে হবে।",
                    contactPhone = "01722-556677",
                    location = "সিএন্ডবি রোড, বরিশাল",
                    priceOrFee = "১২,০০০ + কমিশন (টাকা)",
                    statusOrSchedule = "ফুল-টাইম (সকাল ৮:৩০ টা থেকে)",
                    rating = 4.4f,
                    imageUrl = "https://images.unsplash.com/photo-1600880292203-757bb62b4baf?auto=format&fit=crop&q=80&w=200"
                ),

                // Hospital
                DirectoryItem(
                    id = "hosp_1",
                    category = "hospital",
                    title = "শের-ই-বাংলা মেডিকেল কলেজ হাসপাতাল (SBMCH)",
                    subtitle = "সরকারি সর্ববৃহৎ জেনারেল ও টিচিং হাসপাতাল",
                    description = "বরিশাল বিভাগের সবচেয়ে বড় সরকারি চিকিৎসা সেবা কেন্দ্র। ২৪ ঘণ্টা জরুরি বিভাগ, ট্রমা সেন্টার, সিসিইউ এবং আইসিইউ সুবিধা সচল।",
                    contactPhone = "0431-2173500",
                    location = "বান্দ রোড, বরিশাল (Band Road)",
                    priceOrFee = "বিনামূল্যে (সরকারি নিয়ম)",
                    statusOrSchedule = "২৪ ঘণ্টা খোলা (Emergency)",
                    rating = 4.1f,
                    imageUrl = "https://images.unsplash.com/photo-1586773860418-d3b3da9601ee?auto=format&fit=crop&q=80&w=400"
                ),
                DirectoryItem(
                    id = "hosp_2",
                    category = "hospital",
                    title = "বেলভিউ মেডিকেল ও ডায়াগনস্টিক সেন্টার",
                    subtitle = "আধুনিক সুযোগ-সুবিধা সম্বলিত বেসরকারী হাসপাতাল",
                    description = "দেশসেরা বিশেষজ্ঞ ডাক্তারদের চেম্বার, আল্ট্রাসনোগ্রাফি, ইসিজি, ২৪ ঘণ্টা প্যাথলজি ও জরুরি এম্বুলেন্স সেবা।",
                    contactPhone = "01713-332211",
                    location = "সদর রোড, বরিশাল",
                    priceOrFee = "ভিজিট ফি প্রযোজ্য",
                    statusOrSchedule = "২৪ ঘণ্টা সচল",
                    rating = 4.6f,
                    imageUrl = "https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?auto=format&fit=crop&q=80&w=400"
                ),

                // Doctors
                DirectoryItem(
                    id = "doc_1",
                    category = "doctor",
                    title = "অধ্যাপক ডা: মো: মতিউর রহমান (Prof. Dr. Matiur)",
                    subtitle = "হৃদরোগ ও মেডিসিন বিশেষজ্ঞ (Cardiologist)",
                    description = "MBBS, FCPS (Cardiology). বিভাগীয় প্রধান ও সিনিয়ির কনসালটেন্ট, কার্ডিওলজি বিভাগ, শের-ই-বাংলা মেডিকেল কলেজ।",
                    contactPhone = "01755-112233",
                    location = "বেলভিউ ডায়াগনস্টিক সেন্টার চেম্বার",
                    priceOrFee = "৮০০ টাকা (ফি)",
                    statusOrSchedule = "বিকাল ৫:০০ টা - রাত ৯:০০ টা (শনি - বৃহস্পতি)",
                    rating = 4.9f,
                    imageUrl = "https://images.unsplash.com/photo-1537368910025-700350fe46c7?auto=format&fit=crop&q=80&w=200"
                ),
                DirectoryItem(
                    id = "doc_2",
                    category = "doctor",
                    title = "ডা: ফারহানা ইয়াসমিন রুমি (Dr. Farhana Yasmin)",
                    subtitle = "স্ত্রী রোগ, প্রসূতি ও ল্যাপারোস্কোপিক সার্জন",
                    description = "MBBS, DGO, MCPS. জটিল গর্ভকালীন মা ও শিশুর চিকিৎসা, সিজারিয়ান অপারেশন ও জরায়ুর ল্যাপারোস্কোপিক সার্জারিতে অভিজ্ঞ।",
                    contactPhone = "01833-445566",
                    location = "সদর ডায়াগনস্টিক ল্যাব চেম্বার",
                    priceOrFee = "৬০০ টাকা (ফি)",
                    statusOrSchedule = "বিকাল ৪:০০ টা - রাত ৮:০০ টা (রবি - শুক্র)",
                    rating = 4.8f,
                    imageUrl = "https://images.unsplash.com/photo-1594824813573-246434de83fb?auto=format&fit=crop&q=80&w=200"
                ),

                // Education
                DirectoryItem(
                    id = "edu_1",
                    category = "school",
                    title = "বরিশাল বিশ্ববিদ্যালয় (University of Barishal)",
                    subtitle = "প্রথম সরকারি গবেষণা বিশ্ববিদ্যালয়",
                    description = "২০১১ সালে কীর্তনখোলা নদীর তীরে প্রতিষ্ঠিত বরিশাল বিভাগের গর্ব। ২৪টি বিভাগ নিয়ে এই সুন্দর সবুজ ক্যাম্পাসটি অবস্থিত।",
                    location = "কর্ণকাঠি, বরিশাল-পটুয়াখালী হাইওয়ে",
                    rating = 4.7f,
                    imageUrl = "https://images.unsplash.com/photo-1523050854058-8df90110c9f1?auto=format&fit=crop&q=80&w=400"
                ),
                DirectoryItem(
                    id = "edu_2",
                    category = "school",
                    title = "সরকারি ব্রজমোহন (বিএম) কলেজ",
                    subtitle = "শতবর্ষী ঐতিহ্যবাহী বিদ্যাপীঠ",
                    description = "১৮৮৯ সালে মহাত্মা অশ্বিনীকুমার দত্ত কর্তৃক প্রতিষ্ঠিত দক্ষিণবঙ্গের অন্যতম প্রাচীন ও ঐতিহ্যবাহী উচ্চ শিক্ষা প্রতিষ্ঠান।",
                    location = "বিএম কলেজ রোড, বরিশাল",
                    rating = 4.5f,
                    imageUrl = "https://images.unsplash.com/photo-1541339907198-e08756dedf3f?auto=format&fit=crop&q=80&w=400"
                ),

                // Tourism
                DirectoryItem(
                    id = "tour_1",
                    category = "tourist",
                    title = "ভীমরুলী ভাসমান পেয়ারা বাজার",
                    subtitle = "ঐতিহ্যবাহী খালের ওপর ভাসমান হাট (ঝালকাঠি)",
                    description = "বাংলার ভেনিস! নদী ও খালের ভেতর হাজার হাজার কাঠের নৌকায় ভরপুর সবুজ পেয়ারা বেচা-কেনার দৃশ্য দেখা যায় জুলাই-অক্টোবর মাসে।",
                    location = "ভীমরুলী, ঝালকাঠি (বরিশাল সদর থেকে কাছে)",
                    rating = 4.9f,
                    imageUrl = "https://images.unsplash.com/photo-1506973035872-a4ec16b8e8d9?auto=format&fit=crop&q=80&w=400"
                ),
                DirectoryItem(
                    id = "tour_2",
                    category = "tourist",
                    title = "গুঠিয়া বায়তুল আমান জামে মসজিদ",
                    subtitle = "অনিন্দ্য সুন্দর ইসলামী স্থাপত্য ও ২০ গম্বুজ",
                    description = "নয়নাভিরাম ২০টি গম্বুজ, বিশাল রিফ্লেক্টিং পুল এবং ক্যালিগ্রাফি করা অনন্য সৌন্দর্যমণ্ডিত দেশের অন্যতম সেরা মসজিদ কমপ্লেক্স।",
                    location = "গুঠিয়া, উজিরপুর, বরিশাল",
                    rating = 4.8f,
                    imageUrl = "https://images.unsplash.com/photo-1564507592333-c60657eea523?auto=format&fit=crop&q=80&w=400"
                ),
                DirectoryItem(
                    id = "tour_3",
                    category = "tourist",
                    title = "দুর্গাসাগর দীঘি (Durga Sagar)",
                    subtitle = "১৭৮০ সালের বিশাল রাজকীয় দিঘি ও পক্ষী অভয়ারণ্য",
                    description = "দক্ষিণবঙ্গের বৃহত্তম ঐতিহাসিক দিঘি। এর ঠিক মাঝখানে একটি সুন্দর সবুজ দ্বীপ রয়েছে যা শীতকালে পরিযায়ী পাখির কলকাকলিতে মুখরিত হয়।",
                    location = "মাধবপাশা, বাবুগঞ্জ, বরিশাল",
                    rating = 4.3f,
                    imageUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?auto=format&fit=crop&q=80&w=400"
                ),

                // Transport (Launch & Bus)
                DirectoryItem(
                    id = "launch_1",
                    category = "launch",
                    title = "এমভি মানামী (MV Manami)",
                    subtitle = "ঢাকা - বরিশাল (বিলাসবহুল ট্রিপল ডেক লঞ্চ)",
                    description = "সবচেয়ে আধুনিক বিলাসবহুল লঞ্চ। ফ্রি ওয়াই-ফাই, উন্নত রেস্টুরেন্ট, মনোরম কেবিন এবং ২৪ ঘণ্টা সিসিটিভি ও সিকিউরিটি সম্পন্ন।",
                    priceOrFee = "ডেক: ৩৫০ টাকা | সিঙ্গেল কেবিন: ১৫০০ | ভিআইপি: ৮০০০ টাকা",
                    statusOrSchedule = "রাত ৯:০০ টা (প্রতিদিন সদরঘাট থেকে ছাড়ে)",
                    contactPhone = "01711-558899",
                    location = "বরিশাল লঞ্চ ঘাট টার্মিনাল",
                    rating = 4.9f,
                    imageUrl = "https://images.unsplash.com/photo-1559136555-9303baea8ebd?auto=format&fit=crop&q=80&w=400"
                ),
                DirectoryItem(
                    id = "launch_2",
                    category = "launch",
                    title = "এমভি কীর্তনখোলা-১০ (MV Kirtankhola-10)",
                    subtitle = "ঢাকা - বরিশাল (প্রিমিয়াম যাত্রী সার্ভিস)",
                    description = "নিখুঁত সেবা ও নিরাপদ লাইফবোট, মনোরম কফি শপ এবং উন্নত বেড সুবিধা সমৃদ্ধ দক্ষিণবঙ্গের অন্যতম সেরা ক্রুজার।",
                    priceOrFee = "ডেক: ৩৫০ টাকা | ডাবল কেবিন: ২৮০০ | ভিআইপি: ৬০০০ টাকা",
                    statusOrSchedule = "রাত ৮:৩০ টা (সদরঘাট টার্মিনাল থেকে প্রস্থান)",
                    contactPhone = "01819-335522",
                    location = "বরিশাল লঞ্চ ঘাট টার্মিনাল",
                    rating = 4.7f,
                    imageUrl = "https://images.unsplash.com/photo-1543872084-c7bd3822856f?auto=format&fit=crop&q=80&w=400"
                ),
                DirectoryItem(
                    id = "bus_1",
                    category = "bus",
                    title = "সাকুরা পরিবহন (Sakura Paribahan AC)",
                    subtitle = "বরিশাল - ঢাকা (পদ্মা সেতু হয়ে দ্রুত সেবা)",
                    description = "অত্যন্ত আরামদায়ক হিঁনো ১জে এসি বাস সার্ভিস। রিক্লাইনিং সিট, দক্ষ ড্রাইভার এবং নিখুঁত সময় সচেতনতা।",
                    priceOrFee = "৮০০ টাকা (টিকিট)",
                    statusOrSchedule = "প্রতি ঘণ্টায় ছাড়ে (সকাল ৬:০০ - রাত ১১:৩০)",
                    contactPhone = "01711-224499",
                    location = "নথুল্লাবাদ সেন্ট্রাল বাস টার্মিনাল",
                    rating = 4.6f,
                    imageUrl = "https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?auto=format&fit=crop&q=80&w=400"
                ),
                DirectoryItem(
                    id = "bus_2",
                    category = "bus",
                    title = "হানিফ এন্টারপ্রাইজ (Hanif Non-AC)",
                    subtitle = "বরিশাল - ঢাকা (পদ্মা সেতু রুট)",
                    description = "সুলভ মূল্যে আরামদায়ক সাধারণ বাস সার্ভিস। অভিজ্ঞ গাইড এবং বিস্তৃত স্টপেজ সুবিধা রয়েছে।",
                    priceOrFee = "৫৫০ টাকা (টিকিট)",
                    statusOrSchedule = "প্রতি ৩০ মিনিট অন্তর ছাড়ে (সকাল ৫:০০ - রাত ১২:০০)",
                    contactPhone = "01819-112233",
                    location = "নথুল্লাবাদ বাস টার্মিনাল, বরিশাল",
                    rating = 4.1f,
                    imageUrl = "https://images.unsplash.com/photo-1570125909232-eb263c188f7e?auto=format&fit=crop&q=80&w=400"
                ),

                // Market Price
                DirectoryItem(
                    id = "market_1",
                    category = "market",
                    title = "বরিশালের তাজা ইলিশ (Hilsa Fish)",
                    subtitle = "কীর্তনখোলা ও পদ্মা মোহনা থেকে তাজা ধরা",
                    description = "তাজা রুপালি ইলিশ মাছ। সাইজ অনুযায়ী দাম পরিবর্তিত হয় (যেমন: ১ কেজি সাইজ প্রায় ১৩০০ টাকা)।",
                    priceOrFee = "১,৩০০ টাকা / কেজি",
                    statusOrSchedule = "বাজার দর স্থিতিশীল (Stable)",
                    location = "পোর্ট রোড ইলিশ মোকাম আড়ত, বরিশাল",
                    rating = 4.9f,
                    imageUrl = "https://images.unsplash.com/photo-1534604973900-c43ab4c2e0ab?auto=format&fit=crop&q=80&w=400"
                ),
                DirectoryItem(
                    id = "market_2",
                    category = "market",
                    title = "মিনিকেট চাল (Miniket Premium Rice)",
                    subtitle = "ঝরঝরে চিকন দৈনিক খাদ্য চাল",
                    description = "বরিশাল চাল মোকাম থেকে সংগৃহীত অত্যন্ত ফ্রেশ ঝরঝরে মিনিকেট বা নাজিরশাইল ব্র্যান্ডের প্রিমিয়াম চাল।",
                    priceOrFee = "৬৮ টাকা / কেজি",
                    statusOrSchedule = "২ টাকা হ্রাস পেয়েছে (Decreased)",
                    location = "নতুন বাজার পাইকারি আড়ত, বরিশাল",
                    rating = 4.3f,
                    imageUrl = "https://images.unsplash.com/photo-1586201375761-83865001e31c?auto=format&fit=crop&q=80&w=400"
                ),
                DirectoryItem(
                    id = "market_3",
                    category = "market",
                    title = "দেশি গোল আলু (Gol Alu)",
                    subtitle = "বাবুগঞ্জের নতুন আলুর ক্ষেত থেকে তাজা",
                    description = "বাবুগঞ্জ কো-অপারেটিভ ফার্ম থেকে সরাসরি সংগৃহীত তাজা খোসাওয়ালা হাইব্রিড লাল গোল আলু।",
                    priceOrFee = "৪২ টাকা / কেজি",
                    statusOrSchedule = "অপরিবর্তিত (Stable)",
                    location = "চৌমাথা খুচরা কাঁচাবাজার, বরিশাল",
                    rating = 4.5f,
                    imageUrl = "https://images.unsplash.com/photo-1518977676601-b53f82aba655?auto=format&fit=crop&q=80&w=400"
                ),

                // Government Offices
                DirectoryItem(
                    id = "gov_1",
                    category = "gov",
                    title = "বরিশাল বিভাগীয় কমিশনার কার্যালয়",
                    subtitle = "বরিশাল বিভাগের প্রশাসনিক সচিবালয়",
                    description = "বিভাগের প্রশাসন, উন্নয়ন কর্মকান্ড তদারকি, সরকারি আদেশ ও আইন-শৃঙ্খলার উচ্চতর প্রশাসনিক কার্যালয়।",
                    contactPhone = "0431-2174005",
                    location = "বান্দ রোড, বরিশাল (Divisional Commissioner Office)",
                    rating = 4.0f,
                    imageUrl = "https://images.unsplash.com/photo-1541829014195-ab19a3b2b640?auto=format&fit=crop&q=80&w=400"
                ),
                DirectoryItem(
                    id = "gov_2",
                    category = "gov",
                    title = "জেলা প্রশাসক কার্যালয় (DC Office Barishal)",
                    subtitle = "ডিস্ট্রিক্ট ম্যাজিস্ট্রেট ও ভূমি রাজস্ব বোর্ড",
                    description = "জেলা ম্যাজিস্ট্রেট কোর্ট, পাসপোর্ট ক্লিয়ারেন্স সাপোর্ট, আগ্নেয়াস্ত্র লাইসেন্স ও সরকারি গেজেট সেবা শাখা।",
                    contactPhone = "0431-61001",
                    location = "কালেক্টরেট ভবন, বরিশাল সদর",
                    rating = 4.2f,
                    imageUrl = "https://images.unsplash.com/photo-1541829014195-ab19a3b2b640?auto=format&fit=crop&q=80&w=400"
                ),

                // Emergency Contacts
                DirectoryItem(
                    id = "emerg_1",
                    category = "emergency",
                    title = "জাতীয় জরুরি সেবা (999)",
                    subtitle = "পুলিশ, ফায়ার ও এ্যাম্বুলেন্স হটলাইন",
                    description = "যেকোনো জরুরি সমস্যায় বাংলাদেশ সরকারের সম্পূর্ণ বিনামূল্যে কল করার হটলাইন সার্ভিস। ২৪ ঘণ্টা সচল।",
                    contactPhone = "999",
                    location = "ঢাকা কেন্দ্রীয় পুলিশ কন্ট্রোল রুম",
                    rating = 5.0f,
                    imageUrl = ""
                ),
                DirectoryItem(
                    id = "emerg_2",
                    category = "emergency",
                    title = "বরিশাল ফায়ার সার্ভিস ও সিভিল ডিফেন্স স্টেশন",
                    subtitle = "অগ্নিনির্বাপক ও উদ্ধারকারী জরুরি সেবা",
                    description = "শহরের যেকোনো স্থানে আগুন বা লঞ্চ দুর্ঘটনা উদ্ধার কার্যে অতি দ্রুত অ্যাক্টিভ রেসপন্স ইউনিট।",
                    contactPhone = "01730-336699",
                    location = "চৌমাথা, বরিশাল (Sadar Fire Station)",
                    rating = 4.8f,
                    imageUrl = ""
                ),
                DirectoryItem(
                    id = "emerg_3",
                    category = "emergency",
                    title = "বরিশাল কোতোয়ালী থানা পুলিশ (Kotwali Thana)",
                    subtitle = "প্রধান আইন শৃঙ্খলা ও থানা সার্ভিস",
                    description = "শহরের নিরাপত্তা রক্ষা, অপরাধ দমনে পুলিশ ফোর্সের পেট্রোলিং বা তাৎক্ষণিক অভিযোগ দায়ের সেন্টার।",
                    contactPhone = "01713-374251",
                    location = "সদর রোড, বরিশাল (Kotwali Thana)",
                    rating = 4.4f,
                    imageUrl = ""
                )
            )
            dao.insertItems(sampleItems)

            val sampleRoads = listOf(
                com.example.data.model.SmartRoad(
                    id = "road_101",
                    name = "Dhaka - Barishal Expressway (N8)",
                    category = "Paved Road",
                    width = "24 feet",
                    condition = "Excellent",
                    description = "The primary arterial highway connecting Dhaka to Barishal with fully smooth asphalt paving and multi-lane dividers.",
                    startPoint = "Bhola Mor",
                    endPoint = "Barishal Central Terminal",
                    coordinatesJson = "[[22.7010, 90.3530], [22.7050, 90.3550], [22.7100, 90.3600], [22.7150, 90.3620], [22.7200, 90.3650]]",
                    district = "Barishal",
                    upazila = "Sadar",
                    unionName = "Kirtankhola Union",
                    status = "APPROVED",
                    contributor = "Sakib Ahmed (Admin)",
                    approvedDate = "2026-05-15",
                    lastUpdated = "2026-06-25",
                    distance = 15.2,
                    durationSeconds = 1200
                ),
                com.example.data.model.SmartRoad(
                    id = "road_102",
                    name = "Sadar Road Market Lane",
                    category = "Brick Road",
                    width = "14 feet",
                    condition = "Good",
                    description = "Traditional brick-laid road passing through Sadar main market. Traffic gets dense in evenings.",
                    startPoint = "Chowmatha",
                    endPoint = "Town Hall",
                    coordinatesJson = "[[22.7015, 90.3540], [22.7025, 90.3542], [22.7035, 90.3545]]",
                    district = "Barishal",
                    upazila = "Sadar",
                    unionName = "Chowmatha Union",
                    status = "APPROVED",
                    contributor = "Tariqul Islam",
                    approvedDate = "2026-06-01",
                    lastUpdated = "2026-06-27",
                    distance = 3.5,
                    durationSeconds = 450
                ),
                com.example.data.model.SmartRoad(
                    id = "road_103",
                    name = "Bhola Ferry Link bypass",
                    category = "Dirt Road",
                    width = "10 feet",
                    condition = "Damaged",
                    description = "A dirt bypass road shortcut leading to the ferry terminal. Severely muddy during heavy rains.",
                    startPoint = "Rupatali Mor",
                    endPoint = "Ferry Ghat Bypass",
                    coordinatesJson = "[[22.6890, 90.3400], [22.6870, 90.3420], [22.6850, 90.3450]]",
                    district = "Bhola",
                    upazila = "Sadar",
                    unionName = "Char Fasson Union",
                    status = "APPROVED",
                    contributor = "Naimur Rahman",
                    approvedDate = "2026-04-10",
                    lastUpdated = "2026-06-15",
                    distance = 5.0,
                    durationSeconds = 900
                ),
                com.example.data.model.SmartRoad(
                    id = "road_104",
                    name = "Nalchity Connecting Bypass",
                    category = "Semi Paved",
                    width = "12 feet",
                    condition = "Under Construction",
                    description = "Semi-paved connection between Jhalakathi and Nalchity. Road expansion and bridge repairs are actively running.",
                    startPoint = "Jhalakathi Boundary",
                    endPoint = "Nalchity High School",
                    coordinatesJson = "[[22.6920, 90.2800], [22.6900, 90.2830], [22.6880, 90.2850]]",
                    district = "Jhalakathi",
                    upazila = "Nalchity",
                    unionName = "Nalchity Union",
                    status = "APPROVED",
                    contributor = "Faruk Hossain",
                    approvedDate = "2026-06-10",
                    lastUpdated = "2026-06-28",
                    distance = 8.4,
                    durationSeconds = 1100
                )
            )
            sampleRoads.forEach { dao.insertSmartRoad(it) }
        }
    }

    // IPTV Support
    val allIptvChannels: Flow<List<com.example.data.model.IptvChannel>> = dao.getAllIptvChannels()
    val allDynamicButtons: Flow<List<com.example.data.model.DynamicButton>> = dao.getAllDynamicButtons()
    val allBookmarks: Flow<List<com.example.data.model.BrowserBookmark>> = dao.getAllBookmarks()
    val allHistory: Flow<List<com.example.data.model.BrowserHistory>> = dao.getAllHistory()
    val allDownloads: Flow<List<com.example.data.model.DownloadTask>> = dao.getAllDownloads()

    suspend fun insertIptvChannels(channels: List<com.example.data.model.IptvChannel>) = dao.insertIptvChannels(channels)
    suspend fun insertIptvChannel(channel: com.example.data.model.IptvChannel) = dao.insertIptvChannel(channel)
    suspend fun deleteIptvChannel(id: Int) = dao.deleteIptvChannel(id)
    suspend fun clearIptvChannels() = dao.clearIptvChannels()

    suspend fun insertDynamicButton(button: com.example.data.model.DynamicButton) = dao.insertDynamicButton(button)
    suspend fun deleteDynamicButton(id: Int) = dao.deleteDynamicButton(id)

    suspend fun insertBookmark(bookmark: com.example.data.model.BrowserBookmark) = dao.insertBookmark(bookmark)
    suspend fun deleteBookmark(id: Int) = dao.deleteBookmark(id)

    suspend fun insertHistory(history: com.example.data.model.BrowserHistory) = dao.insertHistory(history)
    suspend fun clearHistory() = dao.clearHistory()

    suspend fun insertDownload(task: com.example.data.model.DownloadTask) = dao.insertDownload(task)
    suspend fun deleteDownload(id: String) = dao.deleteDownload(id)
    suspend fun updateDownloadProgress(id: String, progress: Float, downloadedBytes: Long, speed: String, status: String) =
        dao.updateDownloadProgress(id, progress, downloadedBytes, speed, status)

    // Custom Map Nodes
    val allCustomMapNodes: Flow<List<com.example.data.model.CustomMapNode>> = dao.getAllCustomMapNodes()
    suspend fun insertCustomMapNode(node: com.example.data.model.CustomMapNode) = dao.insertCustomMapNode(node)
    suspend fun updateCustomMapNode(node: com.example.data.model.CustomMapNode) = dao.updateCustomMapNode(node)
    suspend fun deleteCustomMapNode(id: Int) = dao.deleteCustomMapNode(id)
}
