package com.example.model

data class MalaMantra(
    val id: String,
    val name: String,
    val devanagari: String,
    val englishChant: String,
    val meaning: String
)

val DEFAULT_MANTRAS = listOf(
    MalaMantra(
        id = "om_namah_shivaya",
        name = "Om Namah Shivaya",
        devanagari = "ॐ नमः शिवाय",
        englishChant = "Om Namah Shivaya",
        meaning = "I bow to Shiva, the supreme auspicious and peaceful consciousness."
    ),
    MalaMantra(
        id = "gayatri",
        name = "Gayatri Mantra",
        devanagari = "ॐ भूर्भुवः स्वः तत्सवितुर्वरेण्यं भर्गो देवस्य धीमहि धियो यो नः प्रचोदयात्",
        englishChant = "Om Bhur Bhuvaḥ Swaḥ Tat-Savitur Vareṇyaṁ Bhargo Devasya Dheemahi Dhiyo Yo Naḥ Prachodayāt",
        meaning = "We meditate upon the glory of the Creator who illuminates our intellect."
    ),
    MalaMantra(
        id = "maha_mrityunjaya",
        name = "Maha Mrityunjaya",
        devanagari = "ॐ त्र्यम्बकं यजामहे सुगन्धिं पुष्टिवर्धनम् उर्वारुकमिव बन्धनान् मृत्योर्मुक्षीय मामृतात्",
        englishChant = "Om Tryambakaṁ Yajāmahe Sugandhiṁ Puṣṭi-vardhanam Urvārukam-iva Bandhanān Mṛtyor-mukṣīya Māmṛtāt",
        meaning = "We worship the Three-Eyed One, who nourishes all beings and frees us from bondages."
    ),
    MalaMantra(
        id = "hare_krishna",
        name = "Hare Krishna Mahamantra",
        devanagari = "हरे कृष्ण हरे कृष्ण कृष्ण कृष्ण हरे हरे । हरे राम हरे राम राम राम हरे हरे ॥",
        englishChant = "Hare Krishna Hare Krishna Krishna Krishna Hare Hare | Hare Rama Hare Rama Rama Rama Hare Hare",
        meaning = "Calling upon the supreme divine joy and all-attractive energy."
    ),
    MalaMantra(
        id = "om_mani_padme_hum",
        name = "Om Mani Padme Hum",
        devanagari = "ॐ मणि पद्मे हूँ",
        englishChant = "Om Mani Padme Hum",
        meaning = "Praise to the jewel in the lotus of the compassionate heart."
    ),
    MalaMantra(
        id = "ram_naam",
        name = "Sri Ram Jai Ram",
        devanagari = "श्री राम जय राम जय जय राम",
        englishChant = "Sri Ram Jai Ram Jai Jai Ram",
        meaning = "Victory to the righteous, inner divinity within."
    )
)

data class MalaJaapState(
    val currentBead: Int = 0, // 0 to 108
    val completedMalas: Int = 0,
    val totalChantsToday: Int = 0,
    val totalChantsLifetime: Long = 0L,
    val targetMalas: Int = 1,
    val selectedMantraId: String = "om_namah_shivaya",
    val isVibrationEnabled: Boolean = true,
    val isSoundChimeEnabled: Boolean = true,
    val dailyJaapStreak: Int = 0
)
