package hu.mostoha.mobile.android.huki.ui.adapter

import android.content.Context
import android.widget.LinearLayout
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.google.android.material.chip.ChipGroup
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.databinding.ItemPlaceCategoryChipBinding
import hu.mostoha.mobile.android.huki.databinding.ItemPlaceCategoryChipGroupBinding
import hu.mostoha.mobile.android.huki.databinding.ItemPlaceCategorySelectionChipBinding
import hu.mostoha.mobile.android.huki.databinding.ItemPlaceCategoryVerticalChipBinding
import hu.mostoha.mobile.android.huki.extensions.inflater
import hu.mostoha.mobile.android.huki.extensions.setTextOrGone
import hu.mostoha.mobile.android.huki.model.domain.PlaceCategory
import hu.mostoha.mobile.android.huki.model.domain.PlaceCategoryGroup
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.model.ui.resolve
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.util.color
import hu.mostoha.mobile.android.huki.util.colorStateList

class PlaceCategoryAdapter(val context: Context) {

    fun initHikeRecommendations(
        chipGroup: ChipGroup,
        isStroked: Boolean,
        onHikingRoutesClick: () -> Unit,
        onKirandulastippekClick: () -> Unit,
        onTermeszetjaroClick: () -> Unit
    ) {
        chipGroup.addVerticalChip(
            title = R.string.place_category_national_routes_chip_title.toMessage(),
            iconRes = R.drawable.ic_place_category_national_trails,
            isStroked = isStroked,
            onClick = {
                onHikingRoutesClick.invoke()
            }
        )
        chipGroup.addVerticalChip(
            title = R.string.hike_recommender_kirandulastippek_button.toMessage(),
            iconRes = R.drawable.ic_hike_recommender_kirandulastippek,
            isStroked = isStroked,
            onClick = {
                onKirandulastippekClick.invoke()
            }
        )
        chipGroup.addVerticalChip(
            title = R.string.hike_recommender_termeszetjaro_button.toMessage(),
            iconRes = R.drawable.ic_hike_recommender_termeszetjaro,
            isStroked = isStroked,
            onClick = {
                onTermeszetjaroClick.invoke()
            }
        )
    }

    fun initPlaceCategories(
        containerView: LinearLayout,
        isStroked: Boolean,
        onCategoryClick: (PlaceCategory) -> Unit
    ) {
        val context = containerView.context

        containerView.removeAllViews()

        PlaceCategoryGroup.entries.forEach { categoryGroup ->
            val chipGroupBinding = ItemPlaceCategoryChipGroupBinding.inflate(
                context.inflater,
                containerView,
                false
            )
            val chipGroup = chipGroupBinding.placeCategoryChipGroup
            val headerText = chipGroupBinding.placeCategoryChipGroupHeaderText

            headerText.text = context.getString(categoryGroup.title)

            containerView.addView(chipGroupBinding.root)

            PlaceCategory.entries
                .filter { it.categoryGroup == categoryGroup }
                .forEach { category ->
                    chipGroup.addChip(
                        title = category.title,
                        iconRes = category.iconRes,
                        iconColorRes = category.categoryColorRes,
                        isStroked = isStroked,
                        onClick = {
                            onCategoryClick.invoke(category)
                        }
                    )
                }
        }
    }

    companion object {

        fun ChipGroup.addChip(
            title: Message,
            @DrawableRes iconRes: Int? = null,
            @ColorRes iconColorRes: Int? = null,
            isStroked: Boolean,
            onClick: () -> Unit,
        ) {
            val chipBinding = ItemPlaceCategoryChipBinding.inflate(
                context.inflater,
                this,
                false
            )
            with(chipBinding.placeCategoryChip) {
                text = title.resolve(context)
                iconRes?.let { setChipIconResource(it) }
                if (isStroked) {
                    chipBackgroundColor = context.color(R.color.colorBackgroundLight).colorStateList()
                }
                setOnClickListener { onClick.invoke() }
                iconColorRes?.let { setChipIconTintResource(it) }
            }
            addView(chipBinding.root)
        }

        fun ChipGroup.addVerticalChip(
            title: Message,
            subTitle: Message? = null,
            @DrawableRes iconRes: Int,
            isStroked: Boolean,
            onClick: () -> Unit,
        ) {
            val chipBinding = ItemPlaceCategoryVerticalChipBinding.inflate(
                context.inflater,
                this,
                false
            )
            with(chipBinding) {
                placeCategoryChipTitle.text = title.resolve(context)
                placeCategoryChipSubtitle.setTextOrGone(subTitle?.resolve(context))
                placeCategoryChipImage.setImageResource(iconRes)
                with(placeCategoryChipCardView) {
                    if (isStroked) {
                        setCardBackgroundColor(context.color(R.color.colorBackgroundLight).colorStateList())
                    }
                    setOnClickListener { onClick.invoke() }
                }
            }
            addView(chipBinding.root)
        }

        fun ChipGroup.addSelectionChip(
            viewTag: String,
            title: Message,
            @ColorRes backgroundColorRes: Int,
            onClick: () -> Unit
        ) {
            val chipBinding = ItemPlaceCategorySelectionChipBinding.inflate(context.inflater, this, false)
            with(chipBinding.homePlaceCategoryChip) {
                tag = viewTag
                text = title.resolve(context)
                setChipBackgroundColorResource(backgroundColorRes)
                setCloseIconTintResource(R.color.colorOnPrimary)
                setCloseIconSizeResource(R.dimen.icon_size_medium)
                setOnCloseIconClickListener { onClick.invoke() }
                setOnClickListener { onClick.invoke() }
            }
            addView(chipBinding.root)
        }

    }

}
