package com.btb.beatthebox.ui.home

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.widget.ViewPager2
import com.btb.beatthebox.R
import com.btb.beatthebox.constants.DATE_FORMAT
import com.btb.beatthebox.data.model.Slot
import com.btb.beatthebox.data.model.Venue
import com.btb.beatthebox.data.model.home.HomeResponse
import com.btb.beatthebox.data.model.home.UpcomingGame
import com.btb.beatthebox.ui.availability.SlotAvailabilityViewModel
import com.btb.beatthebox.ui.home.adapter.AmenitiesAdapter
import com.btb.beatthebox.ui.home.adapter.BannerAdapter
import com.btb.beatthebox.ui.shared.MainViewModel
import com.btb.beatthebox.ui.slot_selection.DateSelectorView
import com.btb.beatthebox.utils.ApiResult
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

class HomeFragment : Fragment() {
    private val homeViewModel: HomeViewModel by activityViewModels()

    private lateinit var bannerAdapter: BannerAdapter
    private lateinit var amenitiesAdapter: AmenitiesAdapter

    private lateinit var tvVenueName: TextView
    private lateinit var tvVenueStatus: TextView
    private lateinit var tvVenueTimings: TextView

    private lateinit var loaderContainer: View
    private lateinit var progressBar: ProgressBar
    private lateinit var errorText: TextView
    private lateinit var viewPager: ViewPager2
    private lateinit var bannerDots: LinearLayout
    private lateinit var swipeLayout: SwipeRefreshLayout
    private lateinit var btnCtaAvailability: MaterialButton
    private lateinit var btnBookNow: MaterialButton
    private lateinit var tvViewAll: TextView


    private lateinit var root: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_home, container, false)

        tvVenueName = root.findViewById(R.id.header_title_home)
        tvVenueStatus = root.findViewById(R.id.tvVenueStatus_home)
        tvVenueTimings = root.findViewById(R.id.tvVenueTimings_home)
        loaderContainer = root.findViewById(R.id.loader_container)
        progressBar = root.findViewById(R.id.progress_bar)
        errorText = root.findViewById(R.id.error_text)
        viewPager = root.findViewById(R.id.bannerCarousel)
        swipeLayout = root.findViewById(R.id.swipeRefresh)
        btnCtaAvailability = root.findViewById(R.id.ctaAvailability)
        btnBookNow = root.findViewById(R.id.ctaBookNow)
        tvViewAll = root.findViewById(R.id.tvViewAll);


        bannerAdapter = BannerAdapter { banner ->
            val bundle = Bundle().apply {
                putString("title", banner.title)
                putString("srcUrl", banner.srcUrl)
                putString("description", banner.description)
            }
            findNavController().navigate(
                R.id.action_home_to_bannerDetail,
                bundle
            )
        }
        viewPager.adapter = bannerAdapter

        bannerDots = root.findViewById(R.id.bannerDots)
        root.findViewById<ViewPager2>(R.id.bannerCarousel)
            .registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    updateBannerDots(position)
                }
            })

        amenitiesAdapter = AmenitiesAdapter()
        root.findViewById<RecyclerView>(R.id.rvAmenities).apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START
            }
            adapter = amenitiesAdapter
        }

        swipeLayout.setOnRefreshListener {
            homeViewModel.refreshHomeDetails()
            homeViewModel.refreshUpcomingGame()
        }

        btnCtaAvailability.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_availability)
        }
        btnBookNow.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_slotSelection)
        }
        tvViewAll.setOnClickListener{
            findNavController().navigate(R.id.action_home_to_userGames)
        }

        observeViewModels()
        homeViewModel.fetchHomeDetailsIfNeeded()
        homeViewModel.fetchUpcomingGameIfNeeded()
        return root
    }

    private fun observeViewModels() {
        // Observe Venue details from MainViewModel
        homeViewModel.homeDetails.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is ApiResult.Success -> {
                    loaderContainer.visibility = View.GONE
                    errorText.visibility = View.GONE
                    swipeLayout.isRefreshing = false

                    val homeResponse = result.data

                    setHeaderUiData(homeResponse)
                    bannerAdapter.submitList(homeResponse.banners)
                    amenitiesAdapter.submitList(homeResponse.amenities)
                    setupBannerDots(homeResponse.banners.size)
                }

                is ApiResult.Loading -> {
                    loaderContainer.visibility = View.VISIBLE
                    swipeLayout.isRefreshing = false
                    errorText.visibility = View.GONE
                }

                is ApiResult.Refreshing -> {
                    loaderContainer.visibility = View.GONE
                    swipeLayout.isRefreshing = true
                    errorText.visibility = View.GONE
                }

                is ApiResult.Error -> {
                    swipeLayout.isRefreshing = false
                    loaderContainer.visibility = View.GONE
                    errorText.text = result.message ?: "Something went wrong"
                    errorText.visibility = View.VISIBLE
                }

                else -> {}
            }
        })

        homeViewModel.upcomingGame.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ApiResult.Success -> {
                    bindNextGame(result.data) // data can be null
                }

                is ApiResult.Loading, is ApiResult.Refreshing -> {
                    showUpcomingGameShimmer()
                    // Optional: you can show a shimmer / placeholder
                }

                is ApiResult.Error -> {
                    // Fail silently for Home
                    showUpcomingGameEmpty()
                }
            }
        }
    }

    private fun setHeaderUiData(homeResponse: HomeResponse) {
        tvVenueName.text = homeResponse.name
        val drawable = tvVenueStatus.compoundDrawablesRelative[0]

        if (homeResponse.isOpen) {
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
                tvVenueTimings.text = homeResponse.timings
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
    }

    val Int.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    private fun bindNextGame(game: UpcomingGame?) {
        val container = root.findViewById<ViewGroup>(R.id.nextGameContainer)
        container.removeAllViews()

        if (game == null) {
            layoutInflater.inflate(
                R.layout.view_next_game_empty,
                container,
                true
            )
            return
        }

        val view = layoutInflater.inflate(
            R.layout.view_next_game,
            container,
            true
        )

        view.setOnClickListener {
            val bundle = Bundle().apply {
                putString("bookingId", game.id)
            }
            findNavController().navigate(R.id.bookingPassFragment, bundle)
        }

        view.findViewById<TextView>(R.id.tvSportBox).text =
            "${game.sport} • ${game.box}"

        view.findViewById<TextView>(R.id.tvGameDate).text = DateSelectorView.formatSelectedDate(
            LocalDate.parse(
                game.date,
                DateTimeFormatter.ISO_DATE
            )
        )

        view.findViewById<TextView>(R.id.tvGameTime).text =
            "${unitToTime(game.startTime)} – ${unitToTime(game.endTime)}"
    }

    private fun showUpcomingGameShimmer() {
        val container = root.findViewById<ViewGroup>(R.id.nextGameContainer)
        container.removeAllViews()
        layoutInflater.inflate(
            R.layout.view_next_game_shimmer,
            container,
            true
        )
    }

    private fun showUpcomingGameEmpty() {
        val container = root.findViewById<ViewGroup>(R.id.nextGameContainer)
        container.removeAllViews()
        layoutInflater.inflate(
            R.layout.view_next_game_empty,
            container,
            true
        )
    }

    private fun setupBannerDots(count: Int) {
        bannerDots.removeAllViews()

        if (count <= 1) {
            bannerDots.visibility = View.GONE
            return
        }

        bannerDots.visibility = View.VISIBLE

        repeat(count) { index ->
            val dot = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(8.dp, 8.dp).apply {
                    marginStart = 4.dp
                    marginEnd = 4.dp
                }
                background = ContextCompat.getDrawable(
                    requireContext(),
                    if (index == 0)
                        R.drawable.dot_selected
                    else
                        R.drawable.dot_unselected
                )
            }
            bannerDots.addView(dot)
        }
    }

    private fun updateBannerDots(position: Int) {
        for (i in 0 until bannerDots.childCount) {
            bannerDots.getChildAt(i).background =
                ContextCompat.getDrawable(
                    requireContext(),
                    if (i == position)
                        R.drawable.dot_selected
                    else
                        R.drawable.dot_unselected
                )
        }
    }

    private fun unitToTime(unit: Int): String {
        val totalMinutes = (unit + 12) * 30
        val hour = totalMinutes / 60
        val minute = totalMinutes % 60

        val formattedHour = if (hour % 12 == 0) 12 else hour % 12
        val amPm = if (hour < 12) "AM" else "PM"

        return String.format("%d:%02d %s", formattedHour, minute, amPm)
    }
}