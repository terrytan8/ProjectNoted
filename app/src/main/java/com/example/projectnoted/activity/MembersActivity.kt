package com.example.projectnoted.activity

import android.app.Activity
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projectnoted.R
import com.example.projectnoted.adapter.MembersListItemAdapter
import com.example.projectnoted.firebase.FireStoreClass
import com.example.projectnoted.models.Board
import com.example.projectnoted.models.User
import com.example.projectnoted.utils.Constants
import kotlinx.android.synthetic.main.activity_members.*
import kotlinx.android.synthetic.main.activity_task_list.*
import kotlinx.android.synthetic.main.dialog_search_member.*

class MembersActivity : BaseActivity() {
    private var anyChangeMade:Boolean = false
    private lateinit var mBoardDetails:Board
    private lateinit var mAssignedMembersList:ArrayList<User>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_members)

        if(intent.hasExtra(Constants.BOARD_DETAIL)){
            mBoardDetails = intent.getParcelableExtra<Board>(Constants.BOARD_DETAIL)!!
        }

        setupActionBar()

        showProgressDialog(resources.getString((R.string.please_wait)))
        FireStoreClass().getAssignedMembersListDetails(this,
            mBoardDetails.assignedTo)
    }

//SETUP ADAPTER 1//2 SETUP MEMBER LIST//3
// Firestore class call this func//4 put the fire store func to
// ONcreate
    fun setupMemberList(list:ArrayList<User>){

       hideProgressDialog()
    mAssignedMembersList = list
    rv_members_list.layoutManager = LinearLayoutManager(this)
    rv_members_list.setHasFixedSize(true)

    val adapter = MembersListItemAdapter(this,list)
    rv_members_list.adapter = adapter
    }

    fun memberAssignSuccess(user:User){
        hideProgressDialog()
        mAssignedMembersList.add(user)
        //RELOAD IF ANY CHANGES
        anyChangeMade = true
        setupMemberList(mAssignedMembersList)
    }



    private fun setupActionBar(){
        setSupportActionBar(toolbar_members_activity)
        val actionBar = supportActionBar
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.icon_back_arrow)
            actionBar.title= resources.getString(R.string.members)
        }
        toolbar_members_activity.setNavigationOnClickListener{onBackPressed()}
    }

    override fun onBackPressed() {
        if(anyChangeMade){
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_member,menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_add_member->{
                dialogSearchMember()
            }
        }
        return super.onOptionsItemSelected(item)
    }
    fun memberDetails(user:User){
        mBoardDetails.assignedTo.add(user.id)
        FireStoreClass().assignMemberToBoard(this,mBoardDetails,user)

    }

    private fun dialogSearchMember(){
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_search_member)
        dialog.tv_add.setOnClickListener{
            val email = dialog.et_email_search_member.text.toString()

            if(email.isNotEmpty()){
                dialog.dismiss()
                showProgressDialog(resources.getString((R.string.please_wait)))
                FireStoreClass().getMemberDetails(this,email)
            }else{
                Toast.makeText(this,"Please enter member email address",
                Toast.LENGTH_SHORT).show()
            }
        }
        dialog.tv_cancel.setOnClickListener{
            dialog.dismiss()
        }
        dialog.show()
    }
}