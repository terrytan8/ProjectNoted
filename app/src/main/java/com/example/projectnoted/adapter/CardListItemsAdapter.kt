package com.example.projectnoted.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projectnoted.R
import com.example.projectnoted.activity.TaskListActivity
import com.example.projectnoted.models.Board
import com.example.projectnoted.models.Card
import com.example.projectnoted.models.SelectedMembers
import kotlinx.android.synthetic.main.item_board.view.*
import kotlinx.android.synthetic.main.item_card.view.*

class CardListItemsAdapter(private val context:Context,private val list:ArrayList<Card>)
    :RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private var onClickListener:CardListItemsAdapter.OnClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CardListItemsAdapter.myViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_card, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]
        if(holder is myViewHolder) {

            if(model.labelColor.isNotEmpty()){
                holder.itemView.view_label_color.visibility = View.VISIBLE
                holder.itemView.view_label_color.setBackgroundColor(Color.parseColor(model.labelColor))

            }else{
                holder.itemView.view_label_color.visibility = View.GONE
            }

            holder.itemView.tv_card_name.text = model.name
            if ((context as TaskListActivity).mAssignMembersDetailList.size > 0) {
                // A instance of selected members list.
                val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()

                // Here we got the detail list of members and add it to the selected members list as required.
                for (i in context.mAssignMembersDetailList.indices) {
                    for (j in model.assignedTo) {
                        if (context.mAssignMembersDetailList[i].id == j) {
                            val selectedMember = SelectedMembers(
                                context.mAssignMembersDetailList[i].id,
                                context.mAssignMembersDetailList[i].image
                            )

                            selectedMembersList.add(selectedMember)
                        }
                    }
                }

                if (selectedMembersList.size > 0) {

                    if (selectedMembersList.size == 1 && selectedMembersList[0].id == model.createdBy) {
                        holder.itemView.rv_card_selected_members_list.visibility = View.GONE
                    } else {
                        holder.itemView.rv_card_selected_members_list.visibility = View.VISIBLE

                        holder.itemView.rv_card_selected_members_list.layoutManager =
                            GridLayoutManager(context, 4)
                        val adapter = CardMemberListItemsAdapter(context, selectedMembersList, false)
                        holder.itemView.rv_card_selected_members_list.adapter = adapter
                        adapter.setOnClickListener(object :
                            CardMemberListItemsAdapter.OnClickListener {
                            override fun onClick() {
                                if (onClickListener != null) {
                                    onClickListener!!.onClick(position)
                                }
                            }
                        })
                    }
                } else {
                    holder.itemView.rv_card_selected_members_list.visibility = View.GONE
                }
            }
            // TO LET THE RECYCLER VIEW CAN BE CLICK
            holder.itemView.setOnClickListener(){

                if(onClickListener!=null){
                    onClickListener!!.onClick(position)
                }

            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface OnClickListener{
        fun onClick(position:Int)
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener= onClickListener
    }

    private class myViewHolder(view: View):RecyclerView.ViewHolder(view){

    }
}