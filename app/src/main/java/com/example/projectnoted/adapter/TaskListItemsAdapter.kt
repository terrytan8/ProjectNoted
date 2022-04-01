package com.example.projectnoted.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectnoted.R
import com.example.projectnoted.activity.TaskListActivity
import com.example.projectnoted.models.Card
import com.example.projectnoted.models.Task
import com.google.api.Distribution
import kotlinx.android.synthetic.main.item_card.view.*
import kotlinx.android.synthetic.main.item_task.view.*

open class TaskListItemsAdapter (private val context: Context,
                                 private var list:ArrayList<Task>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
      val view = LayoutInflater.from(context).
      inflate(R.layout.item_task,parent,false)

        val layoutParams = LinearLayout.LayoutParams(
            (parent.width*0.7).toInt(),LinearLayout.LayoutParams.WRAP_CONTENT
        )
        //15 MARGIN TOWARD LEFT 40 MARGIN TOWARD RIGHT
        layoutParams.setMargins(
            (15.toDP()).toPX(),0,(40.toDP()).toPX(),0)
        view.layoutParams = layoutParams

        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if(holder is MyViewHolder){
            if(position==list.size -1){ // if nothing in list, display button hide down
                holder.itemView.tv_add_task_list.visibility = View.VISIBLE
                holder.itemView.ll_task_item.visibility= View.GONE
            }else{
                holder.itemView.tv_add_task_list.visibility = View.GONE
                holder.itemView.ll_task_item.visibility= View.VISIBLE
            }

            holder.itemView.tv_task_list_title.text = model.title
            holder.itemView.tv_add_task_list.setOnClickListener{
                holder.itemView.tv_add_task_list.visibility = View.GONE
                holder.itemView.cv_add_task_list_name.visibility = View.VISIBLE
            }

            holder.itemView.ib_close_list_name.setOnClickListener{
                holder.itemView.tv_add_task_list.visibility = View.VISIBLE
                holder.itemView.cv_add_task_list_name.visibility = View.GONE
            }

            holder.itemView.ib_done_list_name.setOnClickListener(){
                val listName = holder.itemView.et_task_list_name.text.toString()

                if(listName.isNotEmpty()){
                    if(context is TaskListActivity){
                        context.createTaskList(listName)
                    }
                }else{
                    Toast.makeText(
                        context,"Please enter List Name",Toast.LENGTH_SHORT).show()
                }
            }
            holder.itemView.ib_edit_list_name.setOnClickListener{
                holder.itemView.et_edit_task_list_name.setText(model.title)
                holder.itemView.ll_title_view.visibility = View.GONE
                holder.itemView.cv_edit_task_list_name.visibility = View.VISIBLE


            }
            holder.itemView.ib_close_editable_view.setOnClickListener{
                holder.itemView.ll_title_view.visibility = View.VISIBLE
                holder.itemView.cv_edit_task_list_name.visibility = View.GONE
            }

            holder.itemView.ib_done_edit_list_name.setOnClickListener(){
                val listName = holder.itemView.et_edit_task_list_name
                    .text.toString()

                if(listName.isNotEmpty()){
                    if(context is TaskListActivity){
                        context.updateTaskList(position,listName,model)
                    }
                }else{
                Toast.makeText(
                    context,"Please enter List Name",Toast.LENGTH_SHORT).show()
            }
            }
            holder.itemView.ib_delete_list.setOnClickListener{
                alertDialogForDeleteList(position,model.title)
            }

            holder.itemView.tv_add_card.setOnClickListener(){
                holder.itemView.tv_add_card.visibility = View.GONE
                holder.itemView.cv_add_card.visibility = View.VISIBLE

            }

            holder.itemView.ib_close_card_name.setOnClickListener(){
                holder.itemView.tv_add_card.visibility = View.VISIBLE
                holder.itemView.cv_add_card.visibility = View.GONE
            }


            holder.itemView.ib_done_cardname.setOnClickListener(){
                val listName = holder.itemView.et_card_name.text.toString()


                if(listName.isNotEmpty()){
                    if(context is TaskListActivity){
                        context.addCardToTaskList(position,listName)
                    }
                }else{
                    Toast.makeText(
                        context,"Please enter Card Name",Toast.LENGTH_SHORT).show()
                }
            }

            holder.itemView.rv_card_list.layoutManager =
                LinearLayoutManager(context)
            holder.itemView.rv_card_list.setHasFixedSize(true)

            val adapter = CardListItemsAdapter(context,model.cards)
            holder.itemView.rv_card_list.adapter = adapter

            // TO LET ALL RECYCLE CAN BE CLICKED
                // TO GET POSITION FROM TASKLIST AND CARDLIST
            adapter.setOnClickListener(

                object :CardListItemsAdapter.OnClickListener{
                    override fun onClick(Cardposition: Int) {
                        if(context is TaskListActivity){
                            context.cardDetails(position,Cardposition)
                        }
                    }
                })




        }
    }


    private fun alertDialogForDeleteList(position: Int,title:String){
        val builder = AlertDialog.Builder(context)

        builder.setTitle("Alert")
        builder.setMessage("Are you sure you want to delete $title ?")
        builder.setIcon(R.drawable.ic_alert)

        builder.setPositiveButton("Yes"){
            dialogInterface,which->
            dialogInterface.dismiss()

            if(context is TaskListActivity){
                context.deleteTaskList(position)
            }
        }
        builder.setNegativeButton("No"){dialogInterface, which ->
            dialogInterface.dismiss()
        }
        val alertDialog:AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    override fun getItemCount(): Int {
        return list.size
    }
    // CONVERT DENSITY PIXEL
    private fun Int.toDP():Int=(
            this/ Resources.getSystem().displayMetrics.density).toInt()

    private fun Int.toPX():Int=(
            this* Resources.getSystem().displayMetrics.density).toInt()

    class MyViewHolder(view: View):RecyclerView.ViewHolder(view)


}
