package com.example.projectnoted.activity

import android.app.Activity
import android.app.AlertDialog

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.SyncStateContract
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout.HORIZONTAL
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projectnoted.R
import com.example.projectnoted.adapter.TaskListItemsAdapter
import com.example.projectnoted.firebase.FireStoreClass
import com.example.projectnoted.models.Board
import com.example.projectnoted.models.Card
import com.example.projectnoted.models.Task
import com.example.projectnoted.models.User
import com.example.projectnoted.utils.Constants
import com.google.firestore.v1.FirestoreGrpc
import kotlinx.android.synthetic.main.activity_my_profile.*
import kotlinx.android.synthetic.main.activity_task_list.*

class TaskListActivity : BaseActivity() {

    companion object{
        const val MEMBERS_REQUEST_CODE:Int = 13
        const val CARD_DETAILS_REQUEST_CODE:Int = 14
    }

    private lateinit var mBoardDetails:Board
    private lateinit var mboardDocumentId:String
     lateinit var mAssignMembersDetailList:ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)


        if(intent.hasExtra(Constants.DOCUMENT_ID)){
            mboardDocumentId= intent.getStringExtra(Constants.DOCUMENT_ID).toString()
        }

        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().getBoardDetails(this,mboardDocumentId)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== Activity.RESULT_OK && requestCode == MEMBERS_REQUEST_CODE||requestCode== CARD_DETAILS_REQUEST_CODE){
            showProgressDialog(resources.getString(R.string.please_wait))
            FireStoreClass().getBoardDetails(this,mboardDocumentId)
        }else{
            Log.e("Cancel","Cancelled")
        }
    }

    fun boardDetails(board:Board){

        mBoardDetails = board
        hideProgressDialog()
        setupActionBar()


        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().getAssignedMembersListDetails(this,mBoardDetails
            .assignedTo)
    }

    fun addUpdateTaskListSuccess(){
        hideProgressDialog()

        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().getBoardDetails(this,mBoardDetails.documentId)
    }

    fun createTaskList(tasklistName:String){
        val task= Task(tasklistName,FireStoreClass().getCurrentUserId())
        mBoardDetails.taskList.add(0,task)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)

        showProgressDialog(resources.getString(R.string.please_wait))

        FireStoreClass().addUpdateTaskList(this,mBoardDetails)
    }

    fun updateTaskList(position:Int,listName:String,model:Task){
        val task = Task(listName,model.createdBy)

        mBoardDetails.taskList[position]= task
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)

        showProgressDialog(resources.getString(R.string.please_wait))

        FireStoreClass().addUpdateTaskList(this,mBoardDetails)
    }
    fun deleteTaskList(position: Int){
        mBoardDetails.taskList.removeAt(position)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)
        showProgressDialog(resources.getString(R.string.please_wait))

        FireStoreClass().addUpdateTaskList(this,mBoardDetails)


    }

    fun addCardToTaskList(position: Int,cardName:String){
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)

        val cardAssignedUserList: ArrayList<String> = ArrayList()
        cardAssignedUserList.add(FireStoreClass().getCurrentUserId())

        val card = Card(cardName,FireStoreClass().getCurrentUserId()
            ,cardAssignedUserList)

        val cardList = mBoardDetails.taskList[position].cards

        cardList.add(card)

        val task = Task(mBoardDetails.taskList[position].title,
            mBoardDetails.taskList[position].createdBy,cardList)

        mBoardDetails.taskList[position] = task

        showProgressDialog(resources.getString(R.string.please_wait))

        FireStoreClass().addUpdateTaskList(this,mBoardDetails)
    }

    fun cardDetails(taskListPosition:Int,cardListPosition:Int){

        val intent = Intent(this,CardDetailsActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAIL,mBoardDetails)
        intent.putExtra(Constants.TASK_LIST_ITEM_POSITION,taskListPosition)
        intent.putExtra(Constants.CARD_LIST_ITEM_POSITION,cardListPosition)
        intent.putExtra(Constants.BOARD_MEMBERS_LIST,mAssignMembersDetailList)

        startActivityForResult(intent, CARD_DETAILS_REQUEST_CODE)

    }

    fun boardMembersDetailList(list:ArrayList<User>){
        mAssignMembersDetailList = list
        hideProgressDialog()
        val addTaskList = Task(resources.getString(R.string.add_list))
        mBoardDetails.taskList.add(addTaskList)

        rv_task_list.layoutManager =
            LinearLayoutManager(this@TaskListActivity, LinearLayoutManager.HORIZONTAL, false)
        rv_task_list.setHasFixedSize(true)

        // Create an instance of TaskListItemsAdapter and pass the task list to it.
        val adapter = TaskListItemsAdapter(this@TaskListActivity, mBoardDetails.taskList)
        rv_task_list.adapter = adapter // Attach the adapter to the recyclerView.
    }
    // OPTION ON RIGHT TOP
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_members->{
                val intent = Intent(this,MembersActivity::class.java)
                intent.putExtra(Constants.BOARD_DETAIL,mBoardDetails)
                startActivityForResult(intent, MEMBERS_REQUEST_CODE)
                return true
            }
            R.id.action_delete_board->{
                val builder = AlertDialog.Builder(this)

                builder.setTitle("Alert")
                builder.setMessage("Are you sure you want to delete ${mBoardDetails.name} ?")
                builder.setIcon(R.drawable.ic_alert)

                builder.setPositiveButton("Yes"){
                        dialogInterface,which->
                    dialogInterface.dismiss()

                    FireStoreClass().deleteBoard(mBoardDetails)
                    startActivity(Intent(this,MainActivity::class.java))

                }
                builder.setNegativeButton("No"){dialogInterface, which ->
                    dialogInterface.dismiss()
                }
                val alertDialog: AlertDialog = builder.create()
                alertDialog.setCancelable(false)
                alertDialog.show()
                //Show the delete notification



            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar_task_list_activity)
        val actionBar = supportActionBar
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.icon_back_arrow)
            actionBar.title= mBoardDetails.name
        }
        toolbar_task_list_activity.setNavigationOnClickListener{onBackPressed()}
    }



}