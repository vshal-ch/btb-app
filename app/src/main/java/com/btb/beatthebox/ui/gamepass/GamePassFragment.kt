package com.btb.beatthebox.ui.gamepass

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.net.Uri
import com.btb.beatthebox.R
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.TouchDelegate
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.btb.beatthebox.data.model.ConfirmedBookingResponse
import com.btb.beatthebox.ui.slot_selection.DateSelectorView
import com.btb.beatthebox.ui.slot_selection.SlotSelectionFragment.Companion.notesToSpannable
import com.btb.beatthebox.utils.ApiResult
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.core.graphics.createBitmap
import androidx.core.os.bundleOf
import java.time.LocalDateTime
import java.time.LocalTime

class GamePassFragment : Fragment(R.layout.fragment_game_pass) {

    private val viewModel: GamePassViewModel by viewModels()

    private lateinit var tvNotes: TextView
    private var controls = arrayOf<View>()
    private lateinit var btnShare: Button;
    private lateinit var btnReschedule: Button;
    private lateinit var btnDownload: Button;
    private lateinit var dividerView: View
    private lateinit var tvPass: TextView
    private lateinit var gamePassCode: String

    private lateinit var loaderContainer: View
    private lateinit var progressBar: ProgressBar
    private lateinit var errorText: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val bookingId = requireArguments().getString("bookingId") ?: return

        tvPass = view.findViewById(R.id.tvGamePass)
        tvNotes = view.findViewById(R.id.tvNotesGp)
        btnShare = view.findViewById(R.id.btnShare)
        btnReschedule = view.findViewById(R.id.btnReschedule)
        btnDownload = view.findViewById(R.id.btnDownload)
        dividerView = view.findViewById(R.id.dividerView)
        controls = arrayOf(btnShare, btnReschedule, btnDownload, dividerView)

        loaderContainer = view.findViewById(R.id.loader_container)
        progressBar = view.findViewById(R.id.progress_bar)
        errorText = view.findViewById(R.id.error_text)

        observeState()
        viewModel.loadBooking(bookingId)
    }

    private fun observeState() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                ApiResult.Loading, ApiResult.Refreshing -> {
                    // show loader if you want
                    loaderContainer.visibility = View.VISIBLE
                    progressBar.visibility = View.VISIBLE
                    errorText.visibility = View.GONE
                }

                is ApiResult.Success -> {
                    loaderContainer.visibility = View.GONE
                    progressBar.visibility = View.GONE
                    errorText.visibility = View.GONE
                    renderBooking(state.data)
                }

                is ApiResult.Error -> {
                    loaderContainer.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    errorText.visibility = View.VISIBLE
                    errorText.text = state.message
                }
            }
        }
    }

    private fun renderBooking(b: ConfirmedBookingResponse) {
        requireView().apply {

            findViewById<TextView>(R.id.tvVenueName).text = b.venue.name

            findViewById<TextView>(R.id.tvVenueAddress).text = b.venue.address

            findViewById<ImageButton>(R.id.btnOpenMap).setOnClickListener {
                val gmapsUrl = b.venue.gmapsUrl
                if (gmapsUrl.isNotEmpty()) {
                    val intent = Intent(Intent.ACTION_VIEW, gmapsUrl.toUri())
                    intent.setPackage("com.google.android.apps.maps") // force open in Maps if available
                    try {
                        startActivity(intent)
                    } catch (e: Exception) {
                        // Fallback: open in browser
                        val browserIntent = Intent(Intent.ACTION_VIEW, gmapsUrl.toUri())
                        startActivity(browserIntent)
                    }
                }
            }

            // Sport chip
            findViewById<TextView>(R.id.tvSport).text = b.booking.sport
            // Status
            val tvStatus = findViewById<TextView>(R.id.tvStatus)
            if (b.booking.attended) {
                tvStatus.text = "Attended"
                tvStatus.alpha = 0.7f
            } else {
                tvStatus.text = "Not Attended"
                tvStatus.alpha = 1f
            }

            // Date
            findViewById<TextView>(R.id.tvDate).text = DateSelectorView.formatSelectedDate(
                LocalDate.parse(b.booking.date, DateTimeFormatter.ISO_DATE)
            )

            // Time
            findViewById<TextView>(R.id.tvTime).text =
                timeLabel(b.booking.startTime, b.booking.endTime)

            findViewById<TextView>(R.id.tvBox).text = b.booking.box

            findViewById<TextView>(R.id.tvDuration).text =
                durationLabel(b.booking.startTime, b.booking.endTime)

            findViewById<TextView>(R.id.tvPrice).text = "₹ ${b.booking.totalAmount}"

            gamePassCode = b.booking.gamePassCode
            val btnTogglePass = findViewById<ImageView>(R.id.btnTogglePass)
            expandTouchArea(btnTogglePass)
            tvPass.text = "••••••••••"

            btnTogglePass.setOnClickListener {
                if (tvPass.text.contains("•")) {
                    tvPass.text = gamePassCode
                    btnTogglePass.setImageResource(R.drawable.ic_eye_closed)
                } else {
                    tvPass.text = "••••••••••"
                    btnTogglePass.setImageResource(R.drawable.ic_eye)
                }

            }
            findViewById<Button>(R.id.btnReschedule).isEnabled =
                !b.booking.attended && b.booking.rescheduledFromId.isNullOrEmpty() && canReschedule(b.booking.date, b.booking.startTime)

            if (!btnReschedule.isEnabled) {
                btnReschedule.alpha = 0.5f
                btnReschedule.setOnClickListener {
                    Toast.makeText(
                        requireContext(),
                        "Rescheduling is allowed only up to 2 hours before start time",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            if (b.policies.isEmpty()) {
                tvNotes.visibility = View.GONE
                return@apply
            }
            tvNotes.visibility = View.VISIBLE
            tvNotes.text = notesToSpannable(b.policies)

            btnShare.setOnClickListener {
                shareBooking()
            }

            btnReschedule.setOnClickListener {
                findNavController().navigate(
                    R.id.action_bookingPassFragment_to_reschedule,
                    bundleOf("game" to b.booking)
                )
            }

            btnDownload.setOnClickListener {
                val passView = requireView().findViewById<View>(R.id.gamePassView)
                val bitmap = captureView(passView)
                val uri = saveBitmapToGallery(
                    requireContext(), bitmap, "BTB_Pass_${System.currentTimeMillis()}"
                )

                if (uri != null) {
                    Toast.makeText(
                        requireContext(), "Pass saved to gallery", Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(), "Failed to save the Pass", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun shareBooking() {
        val root = view ?: return

        val venueName = root.findViewById<TextView>(R.id.tvVenueName).text
        val venueAddress = root.findViewById<TextView>(R.id.tvVenueAddress).text
        val sport = root.findViewById<TextView>(R.id.tvSport).text
        val date = root.findViewById<TextView>(R.id.tvDate).text
        val time = root.findViewById<TextView>(R.id.tvTime).text

        val shareText = """
        🎟️ Booking Confirmed!
        
        📍 $venueName
        $venueAddress
        
        🏏 $sport
        📅 $date
        ⏰ $time
        
        🔑 Game Pass Code:
        $gamePassCode
        
        Please show this pass code at the venue.
        
        — Beat The Box
    """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }

        startActivity(
            Intent.createChooser(intent, "Share booking")
        )
    }

    fun captureView(view: View): Bitmap {
        val prev = tvPass.text
//        tvPass.text = gamePassCode
        controls.forEach { it.visibility = View.GONE }
        view.measure(
            View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        view.layout(
            view.left,
            view.top,
            view.right,
            view.top + view.measuredHeight
        )

        val bitmap = createBitmap(view.width, view.height)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        controls.forEach { it.visibility = View.VISIBLE }
//        tvPass.text = prev
        return bitmap
    }

    fun saveBitmapToGallery(
        context: Context, bitmap: Bitmap, fileName: String
    ): Uri? {

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$fileName.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + "/BeatTheBox"
            )
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
        )

        uri ?: return null

        resolver.openOutputStream(uri).use { out ->
            if (out != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        }

        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)

        return uri
    }

    private fun timeLabel(startTime: Int, endTime: Int): String {
        fun fmt(min: Int) = java.time.LocalTime.of((min / 60) % 24, min % 60)
            .format(DateTimeFormatter.ofPattern("h:mm a"))

        return "${fmt(unitToMinutes(startTime))} – ${fmt(unitToMinutes(endTime))}"
    }

    private fun unitToMinutes(unit: Int): Int {
        return (unit + 12) * 30
    }

    private fun durationLabel(startTime: Int, endTime: Int): String {
        val totalMinutes = unitToMinutes(endTime) - unitToMinutes(startTime)
        if (totalMinutes < 0) {
            return "0 min"
        }

        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        return when {
            hours == 1 && minutes > 0 -> "$hours hour $minutes min"
            hours > 0 && minutes > 0 -> "$hours hours $minutes min"
            hours == 1 && minutes == 0 -> "$hours hour"
            hours > 1 && minutes == 0 -> "$hours hours"
            hours == 0 && minutes > 0 -> "$minutes min"
            else -> "0 min" // Handles case where totalMinutes is 0
        }
    }

    private fun expandTouchArea(view: View, extraPaddingDp: Int = 16) {
        val parent = view.parent as View
        parent.post {
            val rect = Rect()
            view.getHitRect(rect)

            val extra = (extraPaddingDp * view.resources.displayMetrics.density).toInt()

            rect.top -= extra
            rect.bottom += extra
            rect.left -= extra
            rect.right += extra

            parent.touchDelegate = TouchDelegate(rect, view)
        }
    }

    private fun slotStartDateTime(
        date: String,
        startTime: Int
    ): LocalDateTime {

        // Each unit = 30 mins, starting from 6:00 AM
        val minutesFromMidnight = (startTime + 12) * 30

        val localTime = LocalTime.of(
            minutesFromMidnight / 60,
            minutesFromMidnight % 60
        )

        return LocalDate.parse(date, DateTimeFormatter.ISO_DATE)
            .atTime(localTime)
    }

    private fun canReschedule(
        date: String,
        startTime: Int
    ): Boolean {
        val bookingStart = slotStartDateTime(date, startTime)
        val cutoffTime = bookingStart.minusHours(2)
        return LocalDateTime.now().isBefore(cutoffTime)
    }
}
