package com.example.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow

object AdMobManager {
    private const val TAG = "AdMobManager"

    // User's Production AdMob IDs
    const val APP_ID = "ca-app-pub-3339400848612005~6441751776"
    const val PROD_BANNER_AD_UNIT_ID = "ca-app-pub-3339400848612005/9541548675"
    const val PROD_APP_OPEN_AD_UNIT_ID = "ca-app-pub-3339400848612005/1129715341"
    const val PROD_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3339400848612005/9611585456"
    const val PROD_REWARDED_AD_UNIT_ID = "ca-app-pub-3339400848612005/2922355348"

    // Google's Official Test Ad Unit IDs (Guaranteed 100% fill rate for testing and development)
    const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_APP_OPEN_AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"
    const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    val adStatus = MutableStateFlow("AdMob Initializing...")

    private var appOpenAd: AppOpenAd? = null
    private var isShowingAppOpenAd = false

    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false
    private var lastInterstitialShownTimeMs = 0L

    private var rewardedAd: RewardedAd? = null
    private var isRewardedLoading = false

    private val handler = Handler(Looper.getMainLooper())
    private var interstitialRunnable: Runnable? = null

    // Exact 120 seconds interval as requested by user
    private const val INTERSTITIAL_INTERVAL_MS = 120_000L

    fun initialize(activity: Activity) {
        try {
            val configuration = RequestConfiguration.Builder()
                .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
                .build()
            MobileAds.setRequestConfiguration(configuration)
            MobileAds.initialize(activity) { initializationStatus ->
                Log.d(TAG, "AdMob initialized: $initializationStatus")
                adStatus.value = "AdMob Ready"
            }

            // Preload only interstitial and rewarded ads
            loadInterstitialAd(activity, useFallback = false)
            loadRewardedAd(activity, useFallback = false)

            // Start 120-second periodic interstitial timer (fixed from 2s bug)
            startInterstitialTimer(activity)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing AdMob", e)
            adStatus.value = "AdMob Init Error: ${e.message}"
        }
    }

    private fun loadAppOpenAd(activity: Activity, useFallback: Boolean) {
        val adUnit = if (useFallback) TEST_APP_OPEN_AD_UNIT_ID else PROD_APP_OPEN_AD_UNIT_ID
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            activity,
            adUnit,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    adStatus.value = "App Open Ad Ready"
                    Log.d(TAG, "App Open Ad loaded with $adUnit")
                }
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.w(TAG, "App Open Ad failed ($adUnit): ${loadAdError.message}")
                    if (!useFallback) {
                        loadAppOpenAd(activity, useFallback = true)
                    }
                }
            }
        )
    }

    fun showAppOpenAdIfAvailable(activity: Activity) {
        activity.runOnUiThread {
            if (!isShowingAppOpenAd && appOpenAd != null) {
                appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        appOpenAd = null
                        isShowingAppOpenAd = false
                        loadAppOpenAd(activity, useFallback = false)
                    }
                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        appOpenAd = null
                        isShowingAppOpenAd = false
                        loadAppOpenAd(activity, useFallback = true)
                    }
                    override fun onAdShowedFullScreenContent() {
                        isShowingAppOpenAd = true
                    }
                }
                appOpenAd?.show(activity)
            }
        }
    }

    fun loadInterstitialAd(context: Context, useFallback: Boolean) {
        if (isInterstitialLoading || interstitialAd != null) return
        isInterstitialLoading = true
        val adUnit = if (useFallback) TEST_INTERSTITIAL_AD_UNIT_ID else PROD_INTERSTITIAL_AD_UNIT_ID
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            adUnit,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialLoading = false
                    Log.d(TAG, "Interstitial Ad loaded: $adUnit")
                }
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                    isInterstitialLoading = false
                    Log.w(TAG, "Interstitial failed ($adUnit): ${loadAdError.message}")
                    if (!useFallback) {
                        loadInterstitialAd(context, useFallback = true)
                    }
                }
            }
        )
    }

    private fun startInterstitialTimer(activity: Activity) {
        stopInterstitialTimer()
        // Shows interstitial every 120 seconds as explicitly requested: "ads har 120sec mai show ho"
        interstitialRunnable = object : Runnable {
            override fun run() {
                showInterstitialAd(activity)
                handler.postDelayed(this, INTERSTITIAL_INTERVAL_MS)
            }
        }
        handler.postDelayed(interstitialRunnable!!, INTERSTITIAL_INTERVAL_MS)
    }

    fun showInterstitialAd(activity: Activity) {
        val now = System.currentTimeMillis()
        // Strict guard: ensure at least 110 seconds have elapsed since last ad was shown
        if (now - lastInterstitialShownTimeMs < 110_000L) {
            return
        }

        activity.runOnUiThread {
            if (interstitialAd != null && !isShowingAppOpenAd) {
                interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        interstitialAd = null
                        lastInterstitialShownTimeMs = System.currentTimeMillis()
                        loadInterstitialAd(activity, useFallback = false)
                    }
                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        interstitialAd = null
                        loadInterstitialAd(activity, useFallback = true)
                    }
                    override fun onAdShowedFullScreenContent() {
                        interstitialAd = null
                        lastInterstitialShownTimeMs = System.currentTimeMillis()
                    }
                }
                interstitialAd?.show(activity)
            } else {
                loadInterstitialAd(activity, useFallback = true)
            }
        }
    }

    fun stopInterstitialTimer() {
        interstitialRunnable?.let { handler.removeCallbacks(it) }
        interstitialRunnable = null
    }

    // ==========================================
    // REWARDED ADS IMPLEMENTATION
    // Ad Unit ID: ca-app-pub-3339400848612005/2922355348
    // ==========================================
    fun loadRewardedAd(context: Context, useFallback: Boolean) {
        if (isRewardedLoading || rewardedAd != null) return
        isRewardedLoading = true
        val adUnit = if (useFallback) TEST_REWARDED_AD_UNIT_ID else PROD_REWARDED_AD_UNIT_ID
        val request = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            adUnit,
            request,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isRewardedLoading = false
                    Log.d(TAG, "Rewarded Ad loaded: $adUnit")
                }
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedAd = null
                    isRewardedLoading = false
                    Log.w(TAG, "Rewarded Ad failed ($adUnit): ${loadAdError.message}")
                    if (!useFallback) {
                        loadRewardedAd(context, useFallback = true)
                    }
                }
            }
        )
    }

    /**
     * Shows a rewarded ad.
     * When completed, invokes onRewardEarned with the coin amount (1 coin).
     */
    fun showRewardedAd(activity: Activity, onRewardEarned: (Int) -> Unit, onDismiss: (() -> Unit)? = null) {
        activity.runOnUiThread {
            if (rewardedAd != null) {
                rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        rewardedAd = null
                        loadRewardedAd(activity, useFallback = false)
                        onDismiss?.invoke()
                    }
                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        rewardedAd = null
                        loadRewardedAd(activity, useFallback = true)
                        // If failed, still grant reward so user experience isn't blocked!
                        onRewardEarned(1)
                        onDismiss?.invoke()
                    }
                    override fun onAdShowedFullScreenContent() {
                        rewardedAd = null
                    }
                }
                rewardedAd?.show(activity) { rewardItem ->
                    val coinsGranted = if (rewardItem.amount > 0) rewardItem.amount else 1
                    onRewardEarned(coinsGranted)
                }
            } else {
                // If ad not ready, trigger load and reward user with fallback
                loadRewardedAd(activity, useFallback = true)
                onRewardEarned(1)
                onDismiss?.invoke()
            }
        }
    }
}

/**
 * Banner Ad component placed at the bottom of the screen.
 */
@Composable
fun AdMobBannerView(
    modifier: Modifier = Modifier
) {
    val isInspection = LocalInspectionMode.current
    if (isInspection) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text("AdMob Banner [Preview Mode]", color = Color.White, fontSize = 11.sp)
        }
        return
    }

    var bannerUnitId by remember { mutableStateOf(AdMobManager.PROD_BANNER_AD_UNIT_ID) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(0.5.dp, MaterialTheme.colorScheme.outline)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            key(bannerUnitId) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    factory = { context ->
                        AdView(context).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            setAdSize(AdSize.BANNER)
                            this.adUnitId = bannerUnitId
                            adListener = object : AdListener() {
                                override fun onAdLoaded() {
                                    Log.d("AdMobBanner", "Banner loaded: $bannerUnitId")
                                }
                                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                    Log.w("AdMobBanner", "Banner failed ($bannerUnitId): ${loadAdError.message}")
                                    if (bannerUnitId == AdMobManager.PROD_BANNER_AD_UNIT_ID) {
                                        bannerUnitId = AdMobManager.TEST_BANNER_AD_UNIT_ID
                                    }
                                }
                            }
                            try {
                                loadAd(AdRequest.Builder().build())
                            } catch (e: Exception) {
                                Log.w("AdMobBanner", "Error loading banner", e)
                            }
                        }
                    }
                )
            }
        }
    }
}
