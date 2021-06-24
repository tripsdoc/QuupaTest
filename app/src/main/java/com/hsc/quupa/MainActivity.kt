package com.hsc.quupa

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.hsc.quupa.data.model.quupa.position.Tag
import com.hsc.quupa.data.model.quupa.qda.QdaPositionResponse
import com.hsc.quupa.data.network.QuupaClient
import com.hsc.quupa.utilities.*
import com.qozix.tileview.TileView
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_response.view.*
import kotlinx.android.synthetic.main.tag_marker.view.*

class MainActivity : AppCompatActivity() {

    private lateinit var tagViewList: HashMap<String, View>
    private lateinit var tileView: TileView
    private lateinit var imgMapPath: String
    private lateinit var mainHandler: Handler
    private lateinit var userData: UserPreference
    lateinit var availableTags: String
    lateinit var dataAddress: String
    private var isRunning = false
    private var isLandscape = false
    private var mode: Int = 0
    var dataResponse: ArrayList<String> = ArrayList()
    var r = 0
    var l1 = 0
    var l2 = 0
    var s = 0
    var timeDelay: Long = 1000
    var disposable: Disposable? = null

    private val checkIPAddress = object: Runnable {
        override fun run() {
            dataAddress = getIpAddress()
            detailText.text = "You're connected to $dataAddress"
            mainHandler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        tagViewList = HashMap()
        userData = UserPreference(this)
        initializeIpAddress()
        setupView()
        availableTags = "a1a110500104"

        mainHandler = Handler(Looper.getMainLooper())

        textRequest.setOnClickListener {
            showDialogResponse()
        }

        commandBtn.setOnClickListener {
            if (isRunning) {
                commandBtn.setBackgroundResource(R.drawable.ic_start)
                disposable?.dispose()
            } else {
                commandBtn.setBackgroundResource(R.drawable.ic_pause)
                if (mode == 0) {
                    QuupaPositioningEngine(this).observePositions()
                } else {
                    QuupaDataAggregator(this).observePositions()
                }
            }
            isRunning = !isRunning
        }

        addBtn.setOnClickListener {
            when (timeDelay) {
                in 1000..4999 -> {
                    timeDelay += 1000
                    timeLabel.text = "${timeDelay / 1000}s"
                    disposable?.dispose()
                    commandBtn.setBackgroundResource(R.drawable.ic_start)
                }
                100.toLong() -> {
                    timeDelay = 1000
                    timeLabel.text = "${timeDelay / 1000}s"
                    disposable?.dispose()
                    commandBtn.setBackgroundResource(R.drawable.ic_start)
                }
                else -> {
                    timeDelay *= 10
                    timeLabel.text = "${timeDelay.toDouble()/1000}s"
                    disposable?.dispose()
                    commandBtn.setBackgroundResource(R.drawable.ic_start)
                }
            }
        }

        minBtn.setOnClickListener {
            when {
                timeDelay > 1000 -> {
                    timeDelay -= 1000
                    timeLabel.text = "${timeDelay/1000}s"
                    disposable?.dispose()
                    commandBtn.setBackgroundResource(R.drawable.ic_start)
                }
                timeDelay == 10.toLong() -> {
                    timeDelay = 1
                    timeLabel.text = "${timeDelay.toDouble()/1000}s"
                    disposable?.dispose()
                    commandBtn.setBackgroundResource(R.drawable.ic_start)
                }
                else -> {
                    if (timeDelay > 1) {
                        timeDelay /= 10
                        timeLabel.text = "${timeDelay.toDouble()/1000}s"
                        disposable?.dispose()
                        commandBtn.setBackgroundResource(R.drawable.ic_start)
                    }
                }
            }
        }
    }

    private fun setupView() {
        tileViewMap.removeAllViews()

        tagViewList.clear()

        imgMapPath = "map_01_111/125/%d_%d.jpg"
        val specs = RoomSpecs().getRoomSpec("111")

        tileView = TileView(this)
        Log.d("Data", "Is Landscape = $isLandscape")
        tileView.apply {
            setSize(specs.imageWidth, specs.imageHeight)
            addDetailLevel(1F, imgMapPath, 256, 256)
            defineBounds(specs.west, specs.north, specs.east, specs.south)
            setScaleLimits(0F, 3F)
            scale = 1F
            setShouldRenderWhilePanning(true)
            setMarkerAnchorPoints(-0.5F, -0.5F)
        }
        tileView.setViewportPadding(256)
        tileViewMap.addView(tileView)
    }

    @SuppressLint("InflateParams")
    private fun addTagMarker(tagID: String, x: Double, y: Double): View {
        return try {
            val tagView = LayoutInflater.from(this).inflate(R.layout.tag_forklift, null, false)
            tagView.inventory_id.text = tagID

            tileView.addMarker(tagView, x, y, -0.5F, -1.0F)
        } catch (e: Exception) {
            View(this)
        }
    }

    fun updateText() {
        textRequest.text = "R:$r, S:$s, L:${l1+l2}"
    }

    fun updatePosition(data: List<Tag>) {
        s += 1
        updateText()
        for (tag in data) {
            if (tagViewList.contains(tag.id)) {
                tileView.moveMarker(
                    tagViewList[tag.id],
                    tag.smoothedPosition[0] * 10,
                    tag.smoothedPosition[1] * 10
                )
            } else {
                tagViewList[tag.id] = addTagMarker(
                    tag.id,
                    tag.smoothedPosition[0] * 10,
                    tag.smoothedPosition[1] * 10
                )
            }
        }
    }

    fun updateQdaPosition(data: List<QdaPositionResponse>) {
        s += 1
        updateText()
        for (tag in data) {
            if (tagViewList.contains(tag.id)) {
                tileView.moveMarker(
                    tagViewList[tag.id],
                    tag.smoothedPositionX * 10,
                    tag.smoothedPositionY * 10
                )
            } else {
                tagViewList[tag.id] = addTagMarker(
                    tag.id,
                    tag.smoothedPositionX * 10,
                    tag.smoothedPositionY * 10
                )
            }
        }
    }

    private fun showDialogResponse() {
        val builder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.layout_response, null)
        builder.setTitle("Responses")
        builder.setView(dialogView)

        val adapterResponse = ResponseAdapter()
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        dialogView.responseList.apply {
            layoutManager = linearLayoutManager
            adapter = adapterResponse
        }
        builder.setPositiveButton("Clear") { dialog, _ ->
            dataResponse.clear()
            adapterResponse.clear()
            dialog.dismiss()
        }
        adapterResponse.clear()
        adapterResponse.addResponse(dataResponse)
        builder.create()
        builder.show()
    }

    private fun getIpAddress(): String {
        val ipAddress = userData.getIP(UserPreference.IP_ADDRESS).toString()

        return if (QuupaClient.QDA_URL.contains(ipAddress)) {
            mode = 1
            QuupaClient.QDA_URL
        } else {
            mode = 0
            QuupaClient.QPA_URL
        }
    }

    private fun initializeIpAddress() {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        val ip = wifiInfo.ipAddress
        val ipAddress = String.format(
            "%d.%d.%d.",
            ip and 0xff,
            ip shr 8 and 0xff,
            ip shr 16 and 0xff
        )
        userData.setIP(ipAddress)
        if ("192.168.1.10".contains(ipAddress)) {
            mode = 1
            QuupaClient.QDA_URL
        } else {
            mode = 0
            QuupaClient.QPA_URL
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            isLandscape = true
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            isLandscape = false
        }
        setupView()
    }

    override fun onResume() {
        super.onResume()
        mainHandler.post(checkIPAddress)
    }

    override fun onPause() {
        super.onPause()
        mainHandler.removeCallbacks(checkIPAddress)
    }
}