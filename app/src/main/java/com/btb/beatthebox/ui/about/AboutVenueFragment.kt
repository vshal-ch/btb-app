package com.btb.beatthebox.ui.about

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.btb.beatthebox.R
import com.btb.beatthebox.activities.MediaPreviewActivity
import com.btb.beatthebox.ui.shared.MainViewModel
import com.btb.beatthebox.utils.ApiResult
import java.util.Calendar


class AboutVenueFragment : Fragment() {

    private val mainViewModel: MainViewModel by activityViewModels()

    private lateinit var viewPagerCarousel: ViewPager2
    private lateinit var btnPrev: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var tvVenueName: TextView
    private lateinit var tvVenueStatus: TextView
    private lateinit var tvVenueTimings: TextView
    private lateinit var tvAddress: TextView
    private lateinit var btnSeeLocation: Button
    private lateinit var tvDimensions: TextView
    private lateinit var containerCosts: LinearLayout

    private lateinit var loaderContainer: View
    private lateinit var progressBar: ProgressBar
    private lateinit var errorText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_about_venue, container, false)

        viewPagerCarousel = root.findViewById(R.id.viewPagerCarousel)
        btnPrev = root.findViewById(R.id.btnPrev)
        btnNext = root.findViewById(R.id.btnNext)
        tvVenueName = root.findViewById(R.id.tvVenueName)
        tvVenueStatus = root.findViewById(R.id.tvVenueStatus)
        tvVenueTimings = root.findViewById(R.id.tvVenueTimings)
        tvAddress = root.findViewById(R.id.tvAddress)
        btnSeeLocation = root.findViewById(R.id.btnSeeLocation)
        tvDimensions = root.findViewById(R.id.tvVenueDimens)
        containerCosts = root.findViewById(R.id.containerCosts)

        loaderContainer = root.findViewById(R.id.loader_container)
        progressBar = root.findViewById(R.id.progress_bar)
        errorText = root.findViewById(R.id.error_text)

        observeVenue()
        mainViewModel.fetchVenueDetails()

        btnPrev.setOnClickListener {
            val current = viewPagerCarousel.currentItem
            if (current > 0) viewPagerCarousel.currentItem = current - 1
        }
        btnNext.setOnClickListener {
            val current = viewPagerCarousel.currentItem
            if (current < (viewPagerCarousel.adapter?.itemCount ?: 0) - 1)
                viewPagerCarousel.currentItem = current + 1
        }

        viewPagerCarousel.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
//                (viewPagerCarousel.adapter as? CarouselAdapter)?.releaseAllExcept(position)
            }
        })

        return root
    }

    private fun observeVenue() {
        mainViewModel.venueDetails.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ApiResult.Loading -> {
                    loaderContainer.visibility = View.VISIBLE
                    progressBar.visibility = View.VISIBLE
                    errorText.visibility = View.GONE
                }

                is ApiResult.Success -> {
                    loaderContainer.visibility = View.GONE
                    val venue = result.data

                    tvVenueName.text = venue.name
                    val drawable = tvVenueStatus.compoundDrawablesRelative[0]

                    if (venue.isOpen) {
                        val calendar = Calendar.getInstance()
                        val currentHour = calendar.get(Calendar.HOUR_OF_DAY) // 24-hour format

                        if (currentHour < 6) { // Between 12 AM and 6 AM
                            tvVenueStatus.text = "Closed"
                            tvVenueStatus.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.closed_status_color
                                )
                            )
                            drawable?.setTint(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.closed_status_color
                                )
                            )
                            tvVenueTimings.text = "Opens at 6 AM"
                        } else {
                            tvVenueStatus.text = "Open"
                            tvVenueStatus.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.open_status_color
                                )
                            )
                            drawable?.setTint(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.open_status_color
                                )
                            )
                            tvVenueTimings.text = venue.timings
                        }
                    } else {
                        tvVenueStatus.text = "Closed"
                        tvVenueStatus.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.closed_status_color
                            )
                        )
                        drawable?.setTint(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.closed_status_color
                            )
                        )
                        tvVenueTimings.visibility = View.GONE
                    }
                    tvAddress.text = venue.address
                    tvDimensions.text = venue.dimensions

                    btnSeeLocation.setOnClickListener {
                        val gmapsUrl = venue.gmapsUrl
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

                    // Costs
                    containerCosts.removeAllViews()
                    venue.costs.forEach { cost ->
                        val view = layoutInflater.inflate(R.layout.item_cost, containerCosts, false)

                        val tvTiming = view.findViewById<TextView>(R.id.tvTiming)
                        val tvCost = view.findViewById<TextView>(R.id.tvCost)

                        tvTiming.text = cost.timings
                        tvCost.text = getString(R.string.cost_per_hour_label, cost.cost.toString())
                        containerCosts.addView(view)
                    }

                    val adapter = CarouselAdapter(venue.media.map { it.srcUrl })
                    adapter.onMediaClick = { url, type ->
                        if (type == 0) {
                            // Image preview
                            val intent = Intent(requireContext(), MediaPreviewActivity::class.java)
                            intent.putExtra("MEDIA_URL", url)
                            intent.putExtra("MEDIA_TYPE", "image")
                            startActivity(intent)
                        } else {
                            // Video preview
                            stopCurrent()
                            val intent = Intent(requireContext(), MediaPreviewActivity::class.java)
                            intent.putExtra("MEDIA_URL", url)
                            intent.putExtra("MEDIA_TYPE", "video")
                            startActivity(intent)
                        }
                    }
                    // Carousel
                    viewPagerCarousel.adapter = adapter
                }

                is ApiResult.Error -> {
                    loaderContainer.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    errorText.visibility = View.VISIBLE
                    errorText.text = result.message
                }

                else -> {}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
//        (viewPagerCarousel.adapter as? CarouselAdapter)?.releaseAllPlayers()
    }

    override fun onDestroy() {
        super.onDestroy()
//        (viewPagerCarousel.adapter as? CarouselAdapter)?.releaseAllPlayers()
    }

    override fun onPause() {
        super.onPause()
//        (viewPagerCarousel.adapter as? CarouselAdapter)?.pauseAll()
    }

    override fun onStop() {
        val current = viewPagerCarousel.currentItem
//        (viewPagerCarousel.adapter as? CarouselAdapter)?.releaseAllExcept(current)
        super.onStop()
    }

    private fun stopCurrent() {
        val current = viewPagerCarousel.currentItem
//        (viewPagerCarousel.adapter as? CarouselAdapter)?.pause(current)
    }
}
