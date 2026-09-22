package com.example.shuttlecock_frontend.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.shuttlecock_frontend.R
import com.example.shuttlecock_frontend.models.Brand
import com.example.shuttlecock_frontend.models.cart.CartItem

data class BrandGroup(
    val brand: Brand?,
    val items: List<CartItem>
)

class CartBrandGroupAdapter(
    private var groups: List<BrandGroup>,
    private val selectedIds: MutableSet<Int>,
    private val onSelectionChanged: () -> Unit,
    private val onQuantityChange: (item: CartItem, newQuantity: Int) -> Unit,
    private val onDelete: (item: CartItem) -> Unit
) : RecyclerView.Adapter<CartBrandGroupAdapter.GroupViewHolder>() {

    class GroupViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val headerRow: LinearLayout = view.findViewById(R.id.brandHeaderRow)
        val checkboxBrand: CheckBox = view.findViewById(R.id.checkboxBrand)
        val brandName: TextView = view.findViewById(R.id.tvBrandName)
        val itemsContainer: LinearLayout = view.findViewById(R.id.itemsContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart_brand_group, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groups[position]
        holder.brandName.text = group.brand?.name?.uppercase() ?: "OTHERS"

        val allSelected = group.items.all { selectedIds.contains(it.id) }
        holder.checkboxBrand.isChecked = allSelected

        holder.headerRow.setOnClickListener {
            val newState = !allSelected
            group.items.forEach { item ->
                if (newState) selectedIds.add(item.id) else selectedIds.remove(item.id)
            }
            onSelectionChanged()
        }

        holder.itemsContainer.removeAllViews()
        val inflater = LayoutInflater.from(holder.itemsContainer.context)

        group.items.forEach { item ->
            val itemView = inflater.inflate(R.layout.item_cart_dark, holder.itemsContainer, false)
            bindItemRow(itemView, item)
            holder.itemsContainer.addView(itemView)
        }
    }

    private fun bindItemRow(itemView: View, item: CartItem) {
        val product = item.product

        val checkbox = itemView.findViewById<CheckBox>(R.id.checkboxSelect)
        val img = itemView.findViewById<ImageView>(R.id.imgProduct)
        val title = itemView.findViewById<TextView>(R.id.tvTitle)
        val description = itemView.findViewById<TextView>(R.id.tvDescription)
        val price = itemView.findViewById<TextView>(R.id.tvPrice)
        val qty = itemView.findViewById<TextView>(R.id.quantity_item)
        val delete = itemView.findViewById<ImageButton>(R.id.delete_button)
        val plus = itemView.findViewById<ImageButton>(R.id.btnPlus)
        val minus = itemView.findViewById<ImageButton>(R.id.btnMinus)

        title.text = product?.name ?: "Product #${item.productId}"
        description.text = product?.description ?: ""
        price.text = "RM %.2f".format(product?.price ?: 0.0)
        qty.text = item.quantity.toString()

        checkbox.setOnCheckedChangeListener(null)
        checkbox.isChecked = selectedIds.contains(item.id)
        checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedIds.add(item.id) else selectedIds.remove(item.id)
            onSelectionChanged()
        }

        val imageUrl = product?.image?.url
        if (!imageUrl.isNullOrBlank()) {
            img.load(imageUrl) {
                placeholder(R.drawable.image_background)
                error(R.drawable.image_background)
            }
        } else {
            img.setImageResource(R.drawable.image_background)
        }

        plus.setOnClickListener { onQuantityChange(item, item.quantity + 1) }
        minus.setOnClickListener {
            if (item.quantity > 1) onQuantityChange(item, item.quantity - 1)
        }
        delete.setOnClickListener { onDelete(item) }
    }

    override fun getItemCount(): Int = groups.size

    fun updateGroups(newGroups: List<BrandGroup>) {
        groups = newGroups
        notifyDataSetChanged()
    }
}