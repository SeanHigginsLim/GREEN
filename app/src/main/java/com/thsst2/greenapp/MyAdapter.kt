package com.thsst2.greenapp
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyAdapter(private val items: List<String>) :
    RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textViewItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_row, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val raw = items[position] ?: ""
        // detect who sent it
        val trimmed = raw.trim()
        val isBot = trimmed.startsWith("Bot:", ignoreCase = true)
        val isUser = trimmed.startsWith("You:", ignoreCase = true) || trimmed.startsWith("User:", ignoreCase = true)

        val displayText = when {
            isBot -> trimmed.removePrefix("Bot:").trim()
            isUser -> trimmed.removePrefix("You:").removePrefix("User:").trim()
            else -> trimmed
        }

        holder.textView.text = displayText
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )

        val marginHorizontal = (holder.itemView.context.resources.displayMetrics.density * 12).toInt()
        val marginVertical = (holder.itemView.context.resources.displayMetrics.density * 4).toInt()
        params.setMargins(marginHorizontal, marginVertical, marginHorizontal, marginVertical)

        if (isUser) {
            // Align to right
            params.gravity = Gravity.END
            holder.textView.layoutParams = params
            holder.textView.setBackgroundResource(R.drawable.chat_bubble_user)
            holder.textView.setTextColor(Color.parseColor("#000000"))
        } else {
            // Default to left
            params.gravity = Gravity.START
            holder.textView.layoutParams = params
            holder.textView.setBackgroundResource(R.drawable.chat_bubble_bot)
            holder.textView.setTextColor(Color.parseColor("#000000"))
        }
    }

    override fun getItemCount(): Int = items.size
}
