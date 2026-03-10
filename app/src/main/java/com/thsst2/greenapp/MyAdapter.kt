package com.thsst2.greenapp
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class MyAdapter(private val items: List<ChatMessage>, private val onSuggestionClick: (String) -> Unit
) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textViewItem)
        val messageContainer: LinearLayout = itemView.findViewById(R.id.messageContainer)
        val suggestionsContainer: LinearLayout = itemView.findViewById(R.id.suggestionsContainer)
        val buttonSuggestion1: Button = itemView.findViewById(R.id.buttonSuggestion1)
        val buttonSuggestion2: Button = itemView.findViewById(R.id.buttonSuggestion2)
        val buttonSuggestion3: Button = itemView.findViewById(R.id.buttonSuggestion3)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_row, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = items[position]

        holder.textView.text = item.text

        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )

        val marginHorizontal =
            (holder.itemView.context.resources.displayMetrics.density * 12).toInt()
        val marginVertical =
            (holder.itemView.context.resources.displayMetrics.density * 4).toInt()

        params.setMargins(marginHorizontal, marginVertical, marginHorizontal, marginVertical)

        if (item.isUser) {
            params.gravity = Gravity.END
            holder.messageContainer.layoutParams = params
            holder.textView.setBackgroundResource(R.drawable.chat_bubble_user)
            holder.textView.setTextColor(Color.parseColor("#000000"))
            holder.suggestionsContainer.visibility = View.GONE
        } else {
            params.gravity = Gravity.START
            holder.messageContainer.layoutParams = params
            holder.textView.setBackgroundResource(R.drawable.chat_bubble_bot)
            holder.textView.setTextColor(Color.parseColor("#000000"))

            if (item.suggestions.isNotEmpty()) {
                holder.suggestionsContainer.visibility = View.VISIBLE

                val buttons = listOf(
                    holder.buttonSuggestion1,
                    holder.buttonSuggestion2,
                    holder.buttonSuggestion3
                )

                buttons.forEachIndexed { index, button ->
                    if (index < item.suggestions.size) {
                        button.visibility = View.VISIBLE
                        button.text = item.suggestions[index]

                        button.setOnClickListener {
                            onSuggestionClick(item.suggestions[index])
                        }
                    } else {
                        button.visibility = View.GONE
                    }
                }
            } else {
                holder.suggestionsContainer.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int = items.size
}
