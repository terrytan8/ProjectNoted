package com.example.projectnoted.activity

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.GridLayout
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.projectnoted.R
import com.example.projectnoted.adapter.CardMemberListItemsAdapter
import com.example.projectnoted.dialog.LabelColorListDialog
import com.example.projectnoted.dialog.MembersListDialog
import com.example.projectnoted.firebase.FireStoreClass
import com.example.projectnoted.models.*
import com.example.projectnoted.utils.Constants
import kotlinx.android.synthetic.main.activity_card_details.*
import kotlinx.android.synthetic.main.activity_create_board.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CardDetailsActivity : BaseActivity() {

    private lateinit var mBoardDetails: Board
    private var mTaskListPosition = -1
    private var mCardListPosition = -1
    private var mSelectedColor = ""
    private lateinit var mMemberDetailList: ArrayList<User>
    private var mSelectedDueDateMilliSeconds:Long = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_details)
        getIntentData()
        setupActionBar()

        et_name_card_details.setText(
            mBoardDetails
                .taskList[mTaskListPosition].cards[mCardListPosition].name
        )
        et_name_card_details.setSelection(et_name_card_details.text.toString().length)


        mSelectedColor =
            mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].labelColor

        if (mSelectedColor.isNotEmpty()) {
            setColor()
        }

        setupSelectedMembersList()
        mSelectedDueDateMilliSeconds = mBoardDetails.taskList[mTaskListPosition]
            .cards[mCardListPosition].dueDate
        if(mSelectedDueDateMilliSeconds>0){
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy",Locale.ENGLISH)
            val selectedDate = simpleDateFormat.format(Date(mSelectedDueDateMilliSeconds))
            tv_select_due_date.text = selectedDate

        }

        tv_select_due_date.setOnClickListener{
            showDataPicker()
        }





        btn_update_card_details.setOnClickListener {
            if (et_name_card_details.text.toString().isNotEmpty())
                updateCardDetails()
            else {
                Toast.makeText(this, "Please Enter a card name", Toast.LENGTH_LONG).show()
            }
        }

        tv_select_label_color.setOnClickListener {
            labelColorListDialog()
        }
        tv_select_members.setOnClickListener{
            membersListDialog()
        }
    }

    fun addUpdateTaskListSuccess() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }


    private fun setupActionBar() {
        setSupportActionBar(toolbar_card_activity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.icon_back_arrow)
            actionBar.title = mBoardDetails.taskList[mTaskListPosition]
                .cards[mCardListPosition].name

        }
        toolbar_card_activity.setNavigationOnClickListener { onBackPressed() }
    }

    // FROM TASK LIST ACTIVITY TASK DETAIL FUN
    private fun getIntentData() {

        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            mBoardDetails = intent.getParcelableExtra<Board>(Constants.BOARD_DETAIL) as Board
        }


        if (intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)) {
            mTaskListPosition = intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION, -1)
        }
        if (intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)) {
            mCardListPosition = intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION, -1)
        }
        if (intent.hasExtra(Constants.BOARD_MEMBERS_LIST)) {
            mMemberDetailList = intent.getParcelableArrayListExtra(Constants.BOARD_MEMBERS_LIST)!!
        }


    }

    private fun membersListDialog() {

        // Here we get the updated assigned members list
        val cardAssignedMembersList =
            mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo

        if (cardAssignedMembersList.size > 0) {
            // Here we got the details of assigned members list from the global members list which is passed from the Task List screen.
            for (i in mMemberDetailList.indices) {
                for (j in cardAssignedMembersList) {
                    if (mMemberDetailList[i].id == j) {
                        mMemberDetailList[i].selected = true
                    }
                }
            }
        } else {
            for (i in mMemberDetailList.indices) {
                mMemberDetailList[i].selected = false
            }
        }

        val listDialog = object : MembersListDialog(
            this@CardDetailsActivity,
            mMemberDetailList,
            resources.getString(R.string.str_select_member)
        ) {
            override fun onItemSelected(user: User, action: String) {

                if (action == Constants.SELECT) {
                    if (!mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo.contains(
                            user.id
                        )
                    ) {
                        mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo.add(
                            user.id
                        )
                    }
                } else {
                    mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo.remove(
                        user.id
                    )

                    for (i in mMemberDetailList.indices) {
                        if (mMemberDetailList[i].id == user.id) {
                            mMemberDetailList[i].selected = false
                        }
                    }
                }

                setupSelectedMembersList()
            }
        }
        listDialog.show()
    }
    private  fun setupSelectedMembersList(){
        val cardAssignedMemberList =mBoardDetails.taskList[mTaskListPosition]
            .cards[mCardListPosition].assignedTo

        val selectedMemberList:ArrayList<SelectedMembers> = ArrayList()

        for (i in mMemberDetailList.indices) {
            for (j in cardAssignedMemberList) {
                if (mMemberDetailList[i].id == j) {
                    val selectedMember = SelectedMembers(
                        mMemberDetailList[i].id,
                        mMemberDetailList[i].image
                    )
                    selectedMemberList.add(selectedMember)
                }
            }
        }
        if(selectedMemberList.size>0){
            selectedMemberList.add(SelectedMembers("",""))
            tv_select_members.visibility = View.GONE
            rv_selected_members_list.visibility=View.VISIBLE
            rv_selected_members_list.layoutManager = GridLayoutManager(this,6)
            val adapter =
                CardMemberListItemsAdapter(this,selectedMemberList,true)
            rv_selected_members_list.adapter = adapter
            adapter.setOnClickListener(object : CardMemberListItemsAdapter.OnClickListener {
                override fun onClick() {
                    membersListDialog()
                }

            })




        }else{
            tv_select_members.visibility = View.VISIBLE
            rv_selected_members_list.visibility= View.GONE
        }
    }
        private fun updateCardDetails() {

            val card = Card(
                et_name_card_details.text.toString(),
                mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].createdBy,
                mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo,
                mSelectedColor,mSelectedDueDateMilliSeconds
            )
            // when update tasklist wont create new
            val taskList: ArrayList<Task> = mBoardDetails.taskList
            taskList.removeAt(taskList.size - 1)
            // Here we have assigned the update card details to the task list using the card position.
            mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition] = card

            // Show the progress dialog.
            showProgressDialog(resources.getString(R.string.please_wait))
            FireStoreClass().addUpdateTaskList(this@CardDetailsActivity, mBoardDetails)
        }

        private fun deleteCard() {
            //current card
            val cardList: ArrayList<Card> = mBoardDetails.taskList[mTaskListPosition].cards

            cardList.removeAt(mCardListPosition)

            //Remove the add card button to up
            val taskList: ArrayList<Task> = mBoardDetails.taskList
            taskList.removeAt(taskList.size - 1)

            taskList[mTaskListPosition].cards = cardList

            showProgressDialog(resources.getString(R.string.please_wait))
            FireStoreClass().addUpdateTaskList(this, mBoardDetails)
        }

        private fun alertDialogForDeleteCard(cardName: String) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(resources.getString(R.string.alert))
            builder.setMessage(
                resources.getString(
                    R.string.confirmation_message_to_delete_card,
                    cardName
                )
            )
            builder.setIcon(android.R.drawable.ic_dialog_alert)
            builder.setPositiveButton("Yes") { dialogInterface, which ->
                dialogInterface.dismiss()
                deleteCard()
            }
            builder.setNegativeButton("No") { dialogInterface, which ->
                dialogInterface.dismiss()
            }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()
        }

        private fun colorList(): ArrayList<String> {
            val colorList: ArrayList<String> = ArrayList()
            colorList.add("#ff4d4d")
            colorList.add("#18dcff")
            colorList.add("#ffcccc")
            colorList.add("#cd84f1")
            colorList.add("#4b4b4b")
            colorList.add("#32ff7e")
            colorList.add("#7158e2")
            colorList.add("#fffa65")

            return colorList
        }

        private fun setColor() {
            tv_select_label_color.text = ""
            tv_select_label_color.setBackgroundColor(Color.parseColor(mSelectedColor))
        }

        private fun labelColorListDialog() {
            val colorList: ArrayList<String> = colorList()
            val listDialog = object : LabelColorListDialog(
                this,
                colorList,
                resources.getString(R.string.str_Select_label_color), mSelectedColor
            ) {
                override fun onItemSelected(color: String) {
                    mSelectedColor = color
                    setColor()
                }

            }
            listDialog.show()
        }

    //DATE PICKER
    private fun showDataPicker() {
        /**
         * This Gets a calendar using the default time zone and locale.
         * The calender returned is based on the current time
         * in the default time zone with the default.
         */
        val c = Calendar.getInstance()
        val year =
            c.get(Calendar.YEAR) // Returns the value of the given calendar field. This indicates YEAR
        val month = c.get(Calendar.MONTH) // This indicates the Month
        val day = c.get(Calendar.DAY_OF_MONTH) // This indicates the Day

        /**
         * Creates a new date picker dialog for the specified date using the parent
         * context's default date picker dialog theme.
         */
        val dpd = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                /*
                  The listener used to indicate the user has finished selecting a date.
                 Here the selected date is set into format i.e : day/Month/Year
                  And the month is counted in java is 0 to 11 so we need to add +1 so it can be as selected.

                 Here the selected date is set into format i.e : day/Month/Year
                  And the month is counted in java is 0 to 11 so we need to add +1 so it can be as selected.*/

                // Here we have appended 0 if the selected day is smaller than 10 to make it double digit value.
                val sDayOfMonth = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
                // Here we have appended 0 if the selected month is smaller than 10 to make it double digit value.
                val sMonthOfYear =
                    if ((monthOfYear + 1) < 10) "0${monthOfYear + 1}" else "${monthOfYear + 1}"

                val selectedDate = "$sDayOfMonth/$sMonthOfYear/$year"
                // Selected date it set to the TextView to make it visible to user.
                tv_select_due_date.text = selectedDate

                /**
                 * Here we have taken an instance of Date Formatter as it will format our
                 * selected date in the format which we pass it as an parameter and Locale.
                 * Here I have passed the format as dd/MM/yyyy.
                 */
                /**
                 * Here we have taken an instance of Date Formatter as it will format our
                 * selected date in the format which we pass it as an parameter and Locale.
                 * Here I have passed the format as dd/MM/yyyy.
                 */
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)

                // The formatter will parse the selected date in to Date object
                // so we can simply get date in to milliseconds.
                val theDate = sdf.parse(selectedDate)

                /** Here we have get the time in milliSeconds from Date object
                 */

                /** Here we have get the time in milliSeconds from Date object
                 */
                mSelectedDueDateMilliSeconds = theDate!!.time
            },
            year,
            month,
            day
        )
        dpd.show() // It is used to show the datePicker Dialog.
    }

        override fun onCreateOptionsMenu(menu: Menu?): Boolean {
            menuInflater.inflate(R.menu.menu_delete_card, menu)
            return super.onCreateOptionsMenu(menu)
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {

            when (item.itemId) {
                R.id.action_delete_card -> {
                    alertDialogForDeleteCard(
                        mBoardDetails.taskList
                                [mTaskListPosition].cards[mCardListPosition].name
                    )
                    return true
                }

            }
            return super.onOptionsItemSelected(item)
        }


    }
