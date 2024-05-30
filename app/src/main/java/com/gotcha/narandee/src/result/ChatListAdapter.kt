package com.gotcha.narandee.src.result

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gotcha.narandee.databinding.ChatListItemBinding
import com.gotcha.narandee.src.models.ChatMessage

private const val TAG = "ChatListAdapter_싸피"

class ChatListAdapter(private val callback: AdapterCallback) :
    ListAdapter<ChatMessage, ChatListAdapter.CustomViewHolder>(
        ChatComparator
    ) {

    private lateinit var typingHandler: Handler

    fun clearTypingHandlerCallbacks() {
        typingHandler.removeCallbacksAndMessages(null)
    }

    interface AdapterCallback {
        fun onNeedToScroll(position: Int)
    }

    interface ChatClickListener {
        fun onClick(address: String?)
    }

    private var onChatClickListener: ChatClickListener? = null
    fun setChatClickListener(chatClickListener: ChatClickListener) {
        this.onChatClickListener = chatClickListener
    }

    companion object ChatComparator : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem == newItem
        }
    }

    inner class CustomViewHolder(
        val binding: ChatListItemBinding,
        private val callback: AdapterCallback
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage, onComplete: () -> Unit) {
            if (message.type == "user") {
                binding.leftLayout.visibility = View.GONE
                binding.rightLayout.visibility = View.VISIBLE
                binding.myChatTv.text = message.userScript
            } else {
                binding.rightLayout.visibility = View.GONE
                binding.leftLayout.visibility = View.VISIBLE

                if (message.name == "") {
                    binding.gptAnswerNameTv.visibility = View.GONE
                    if (message.isTypingComplete) {
                        binding.gptAnswerReasonTv.text = message.gptScript
                        onComplete()
                    } else {
                        binding.gptAnswerReasonTv.text = ""
                        showTypingEffect(binding.gptAnswerReasonTv, message.gptScript!!) {
                            message.isTypingComplete = true
                            onComplete()
                        }
                    }
                } else {
                    binding.gptAnswerNameTv.visibility = View.VISIBLE
                    if (message.isTypingComplete) {
                        binding.gptAnswerNameTv.text = message.name
                        binding.gptAnswerReasonTv.text = message.gptScript
                        onComplete()
                    } else {
                        binding.gptAnswerNameTv.text = ""
                        showTypingEffect(binding.gptAnswerNameTv, message.name) {
                            binding.gptAnswerReasonTv.text = ""
                            showTypingEffect(binding.gptAnswerReasonTv, message.gptScript ?: "") {
                                message.isTypingComplete = true
                                onComplete()
                            }
                        }
                    }
                }

                itemView.setOnClickListener {
                    if (message.placeAddress != null) {
                        onChatClickListener?.onClick(message.placeAddress)
                    }
                }
            }
        }

        // 타이핑 효과
        private fun showTypingEffect(
            typingTextView: TextView,
            text: String,
            onComplete: Runnable?
        ) {
            typingHandler = Handler(Looper.getMainLooper())
            var typingIndex = 0

            val runnable = object : Runnable {
                @SuppressLint("SetTextI18n")
                override fun run() {
                    if (typingIndex < text.length) {
                        typingTextView.text = typingTextView.text.toString() + text[typingIndex]
                        typingIndex++
                        typingHandler.postDelayed(this, 50)
                        callback.onNeedToScroll(adapterPosition)
                    } else {
                        if (typingTextView.text.length == text.length) {
                            onComplete?.run()
                        } else {
                            // 예상 길이와 일치하지 않는 경우, 남은 텍스트를 추가
                            typingTextView.text = text
                            onComplete?.run()
                        }
                    }
                }
            }
            typingHandler.post(runnable)
        }
    }

    // 아이템 재사용 오류 예방
    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CustomViewHolder {
        return CustomViewHolder(
            ChatListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            callback
        )
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val currentMessage = getItem(position)
        holder.bind(currentMessage) {}
    }
}