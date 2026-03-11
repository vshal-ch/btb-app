package com.btb.beatthebox.ui.home

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.btb.beatthebox.R
import com.btb.beatthebox.data.model.Slot
import com.btb.beatthebox.data.model.Venue
import com.btb.beatthebox.data.model.home.HomeResponse
import com.btb.beatthebox.data.model.home.UpcomingGame
import com.btb.beatthebox.ui.availability.SlotAvailabilityViewModel
import com.btb.beatthebox.ui.home.adapter.AmenitiesAdapter
import com.btb.beatthebox.ui.home.adapter.BannerAdapter
import com.btb.beatthebox.ui.shared.MainViewModel
import com.btb.beatthebox.utils.ApiResult
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.util.Calendar

class BannerDetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_banner_detail, container, false)

        val image = root.findViewById<ImageView>(R.id.ivBanner)
        val titleView = root.findViewById<TextView>(R.id.tvTitle)
        val descriptionView = root.findViewById<TextView>(R.id.tvDescription)

        val displayMetrics = resources.displayMetrics
        val halfScreenHeight = displayMetrics.heightPixels / 2

        image.maxHeight = halfScreenHeight

        val title = requireArguments().getString("title")!!
        val srcUrl = requireArguments().getString("srcUrl")!!
        val description = requireArguments().getString("description")!!

        loadBannerImage(image, srcUrl)

        titleView.text = title
        descriptionView.text = description

        return root
    }

    private fun loadBannerImage(imageView: ImageView, imageUrl: String) {
        val screenHeight = resources.displayMetrics.heightPixels
        val maxAllowedHeight = screenHeight / 2

        Glide.with(imageView).asBitmap().load(imageUrl).into(object : CustomTarget<Bitmap>() {

                override fun onResourceReady(
                    resource: Bitmap, transition: Transition<in Bitmap>?
                ) {
                    val imageWidth = resource.width
                    val imageHeight = resource.height

                    // Calculate scaled height based on screen width
                    val screenWidth = resources.displayMetrics.widthPixels
                    val scaledHeight = (imageHeight * screenWidth) / imageWidth

                    val finalHeight = minOf(scaledHeight, maxAllowedHeight)

                    imageView.layoutParams = imageView.layoutParams.apply {
                        height = finalHeight
                    }

                    imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
                    imageView.setImageBitmap(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // no-op
                }
            })
    }

}
