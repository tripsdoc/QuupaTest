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
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.hsc.quupa.data.model.quupa.position.Tag
import com.hsc.quupa.data.model.quupa.qda.QdaPositionResponse
import com.hsc.quupa.data.network.QuupaClient
import com.hsc.quupa.listener.OnFinishedSingleRequest
import com.hsc.quupa.listener.OnWifiChanged
import com.hsc.quupa.utilities.*
import com.qozix.tileview.TileView
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_response.view.*
import kotlinx.android.synthetic.main.tag_marker.view.*

class MainActivity : AppCompatActivity(), OnWifiChanged, OnFinishedSingleRequest {

    private lateinit var tagViewList: HashMap<String, View>
    private lateinit var tileView: TileView
    private lateinit var imgMapPath: String
    private lateinit var connectedText: String
    private lateinit var userData: UserPreference
    private lateinit var mainHandler: Handler
    lateinit var availableTags: String
    lateinit var dataAddress: String
    private var mapText: HashMap<String, String> = HashMap()
    private var isRunning = false
    private var isLandscape = false
    private var mode: Int = 0
    private var isShownTag = false
    var dataResponse: ArrayList<String> = ArrayList()
    var r = 0
    var l1 = 0
    var l2 = 0
    var s = 0
    var timeDelay: Long = 1000
    var disposable: Disposable? = null
    private var startTime: Long = 0
    private var endTime: Long = 0

    private var changeText = object: Runnable {
        override fun run() {
            if (isShownTag) {
                detailText.setText(mapText["Connected"])
            } else {
                detailText.setText(mapText["Tag"])
            }
            Log.d("Data", "Running")
            isShownTag = !isShownTag
            mainHandler.postDelayed(this, 1000 * 5)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mainHandler = Handler(Looper.getMainLooper())
        WifiReceiver.bindListener(this)
        isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        tagViewList = HashMap()
        userData = UserPreference(this)

        detailText.setInAnimation(this, android.R.anim.slide_in_left)
        detailText.setOutAnimation(this, android.R.anim.slide_out_right)

        availableTags = userData.getTag(UserPreference.AVAILABLE_TAG).toString()

        setupView()

        textRequest.setOnClickListener {
            showDialogResponse()
        }

        commandBtn.setOnClickListener {
            if (!::availableTags.isInitialized || availableTags == "") {
                Toast.makeText(this, "Please set tag first!", Toast.LENGTH_SHORT).show()
            } else {
                if (isRunning) {
                    commandBtn.setBackgroundResource(R.drawable.ic_start)
                    disposable?.dispose()
                } else {
                    ResponseWriter.newResponse("response.txt")
                    ResponseWriter.newResponse("tag.txt")
                    commandBtn.setBackgroundResource(R.drawable.ic_pause)
                    if (mode == 0) {
                        QuupaPositioningEngine.bindListener(this)
                        QuupaPositioningEngine(this).observePositions()
                    } else {
                        QuupaDataAggregator(this).observePositions()
                    }
                }
                isRunning = !isRunning
            }
        }

        commandTag.setOnClickListener {
            showEditTag()
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
            val response = "(${tag.location[0]},${tag.location[1]}) --> Start : $startTime End : $endTime Diff : ${endTime-startTime}ms"
            ResponseWriter.writeResponse(response, "tag.txt")
            if (tagViewList.contains(tag.tagId)) {
                tileView.moveMarker(
                    tagViewList[tag.tagId],
                    tag.location[1],
                    tag.location[0]
                )
            } else {
                tagViewList[tag.tagId] = addTagMarker(
                    tag.tagId,
                    tag.location[1],
                    tag.location[0]
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
                    tag.smoothedPositionY,
                    tag.smoothedPositionX
                )
            } else {
                tagViewList[tag.id] = addTagMarker(
                    tag.id,
                    tag.smoothedPositionY,
                    tag.smoothedPositionX
                )
            }
        }
    }

    private fun showEditTag() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Tag Data")
        val input = EditText(this)
        val lp = LinearLayout.LayoutParams (
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
                )
        input.setText(availableTags)
        input.layoutParams = lp
        alertDialog.setView(input)
        alertDialog.setPositiveButton("OK") { _, _ ->
            availableTags = input.text.toString()
            mapText["Tag"] = availableTags
            userData.setTag(availableTags)
        }
        alertDialog.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }
        alertDialog.show()
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
        builder.setNeutralButton("Open Folder") { dialog, _ ->
            dialog.dismiss()
            ResponseWriter.openResponseFolder(this)
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

    private fun initializeIpAddress() {
        val userPreference = UserPreference(this)
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        val ip = wifiInfo.ipAddress
        val ipAddress = String.format(
            "%d.%d.%d.",
            ip and 0xff,
            ip shr 8 and 0xff,
            ip shr 16 and 0xff
        )
        if (QuupaClient.QDA_URL.contains(ipAddress)) {
            mode = 1
            dataAddress = QuupaClient.QDA_URL
            QuupaClient.QDA_URL
        } else {
            mode = 0
            dataAddress = QuupaClient.QPA_URL
            QuupaClient.QPA_URL
        }
        connectedText = "You're connected to $dataAddress"
        val userTagData = userPreference.getTag(UserPreference.AVAILABLE_TAG).toString()
        val tagData: String = if (userTagData == "") {
            "Tag data not set"
        } else {
            userTagData
        }
        mapText["Connected"] = connectedText
        mapText["Tag"] = tagData
        detailText.setText(mapText["Connected"])
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

    override fun onWifiChanged(ipAddress: String) {
        if (QuupaClient.QDA_URL.contains(ipAddress)) {
            mode = 1
            dataAddress = QuupaClient.QDA_URL
            QuupaClient.QDA_URL
        } else {
            mode = 0
            dataAddress = QuupaClient.QPA_URL
            QuupaClient.QPA_URL
        }
        commandBtn.setBackgroundResource(R.drawable.ic_start)
        connectedText = "You're connected to $dataAddress"
        mapText["Connected"] = connectedText
        detailText.setText(mapText["Connected"])
        isShownTag = false
        disposable?.dispose()
        isRunning = false
    }

    override fun onResume() {
        super.onResume()
        initializeIpAddress()
        mainHandler.post(changeText)
    }

    override fun onPause() {
        super.onPause()
        mainHandler.removeCallbacks(changeText)
    }

    override fun onFinishedSingleRequest(start: Long, end: Long) {
        startTime = start
        endTime = end
    }
}