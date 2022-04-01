package com.example.projectnoted.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.projectnoted.activity.*
import com.example.projectnoted.models.Board
import com.example.projectnoted.models.User
import com.example.projectnoted.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FireStoreClass {

    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity:SignUpActivity,userInfo: User){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId()).set(userInfo,SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            }.addOnFailureListener{
                e->
                Log.e(activity.javaClass.simpleName,"Error Writing Document")
            }
    }

    fun getBoardDetails(activity:TaskListActivity,documentId:String){
        mFireStore.collection(Constants.BOARD)
            .document(documentId)
            .get()
            .addOnSuccessListener {
                    document->
                Log.i(activity.javaClass.simpleName,document.toString())
                val board = document.toObject(Board::class.java)!!
                board.documentId =document.id
                activity.boardDetails(board)

            }
            .addOnFailureListener{
                    e->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"Error while creating Board",e)
            }
    }

    fun createBoard(activity: CreateBoardActivity,board: Board){
      mFireStore.collection(Constants.BOARD)
          .document()
          .set(board, SetOptions.merge())
          .addOnSuccessListener {
              Log.i(activity.javaClass.simpleName,"Board Create succesful")
              Toast.makeText(activity,"Board create successfully",Toast.LENGTH_LONG).show()
              activity.boardCreatedSuccessfully()

          }.addOnFailureListener{
                  exception->
              activity.hideProgressDialog()
              Log.e(activity.javaClass.simpleName,"Error while creating a board",exception)
              Toast.makeText(activity,"Board created Error",Toast.LENGTH_LONG).show()
          }

    }

    fun getBoardList(activity: MainActivity){
        mFireStore.collection(Constants.BOARD)
            .whereArrayContains(Constants.ASSIGNED_TO,getCurrentUserId())
            .get()
            .addOnSuccessListener {
              document->
                Log.i(activity.javaClass.simpleName,document.documents.toString())
                val boardList:ArrayList<Board> = ArrayList()
                for(i in document.documents){
                    val board = i.toObject((Board::class.java))!!
                    board.documentId = i.id
                    boardList.add(board)
                }

                activity.populateBoardsListToUI(boardList)

            }
            .addOnFailureListener{
                e->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"Error while creating Board",e)
            }
    }

    fun deleteBoard(board: Board){
        mFireStore.collection((Constants.BOARD))
            .document(board.documentId)
            .delete()

    }



    fun addUpdateTaskList(activity: Activity, board: Board) {

        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList

        mFireStore.collection(Constants.BOARD)
            .document(board.documentId)
            .update(taskListHashMap)
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "TaskList updated successfully.")

                if (activity is TaskListActivity) {
                    activity.addUpdateTaskListSuccess()
                } else if (activity is CardDetailsActivity) {
                    activity.addUpdateTaskListSuccess()
                }
            }
            .addOnFailureListener { e ->
                if (activity is TaskListActivity) {
                    activity.hideProgressDialog()
                } else if (activity is TaskListActivity) {
                    activity.hideProgressDialog()
                }
                Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
            }
    }


    fun updateUserProfileData(activity: MyProfileActivity,
                              userHashMap:HashMap<String,Any>){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId()).update(userHashMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName,"Profile Data Updated succesful")
                Toast.makeText(activity,"Profile update successfully",Toast.LENGTH_LONG).show()
                activity.profileUpdateSuccess()
            }.addOnFailureListener{
                e->
                activity.hideProgressDialog()
                Log.i(activity.javaClass.simpleName,"Error while creating",e)
                Toast.makeText(activity,"Profile Update Error",Toast.LENGTH_LONG).show()

            }
    }
    fun loadUserData(activity: Activity,readBoardlist:Boolean= false){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId()).get()
            .addOnSuccessListener { document ->
                val loggedInUser = document.toObject(User::class.java)!!

                when(activity){
                    is SignInActivity->{
                        activity.signInSuccess(loggedInUser)
                    }
                    is MainActivity->{
                        activity.updateNavigationUserDetails(loggedInUser,readBoardlist)
                    }
                    is MyProfileActivity->{
                        activity.setUserDataInUI(loggedInUser)

                    }
                }




            }.addOnFailureListener{
                    e->
                        when(activity){
                            is SignInActivity->{
                                activity.hideProgressDialog()
                            }
                             is MainActivity->{
                                activity.hideProgressDialog()
                         }
                        }


                Log.e(activity.javaClass.simpleName,"Error Writing Document")
            }

    }

    //Get UUID from database
    fun getCurrentUserId():String{

        var currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID=""
        if(currentUser!=null){
            currentUserID= currentUser.uid
        }
        return currentUserID
    }

    fun getAssignedMembersListDetails(activity:Activity,assignedTo:ArrayList<String>){
        mFireStore.collection(Constants.USERS)
                //Check id assigned to is equal or multiple
            .whereIn(Constants.ID,assignedTo) //Request
            .get()
            .addOnSuccessListener {
                document-> //Result
                Log.e(activity.javaClass.simpleName,document.documents.toString())

                val userList : ArrayList<User> = ArrayList()
                for(i in document.documents){
                    val user = i.toObject(User::class.java)!!
                    userList.add(user)
                }
                if(activity is MembersActivity)
                    activity.setupMemberList(userList)
                else if(activity is TaskListActivity)
                    activity.boardMembersDetailList(userList)
            }
            .addOnFailureListener{
                    e->
                if(activity is MembersActivity)
                    activity.hideProgressDialog()
                else if(activity is TaskListActivity)
                    activity.hideProgressDialog()

                Log.i(activity.javaClass.simpleName,"Error while get member",e)
                Toast.makeText(activity,"Error assigned member",Toast.LENGTH_LONG).show()
            }
    }
    //FOR GET THE MEMBER DETAIL IN SEARCH MEMBER
    fun getMemberDetails(activity: MembersActivity,email:String){

        mFireStore.collection((Constants.USERS))
            .whereEqualTo(Constants.EMAIL,email)
            .get()
            .addOnSuccessListener {
                    document-> //Result
                Log.e(activity.javaClass.simpleName,document.documents.toString())
                if(document.documents.size>0){
                    val user = document.documents[0].toObject(User::class.java)!!
                    activity.memberDetails(user)
                }else{
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar("No such member found")
                }
            }
            .addOnFailureListener{
                e->
                activity.hideProgressDialog()
                Log.i(activity.javaClass.simpleName,"Error In getting mmb details",e)
                Toast.makeText(activity,"Member detail get error",Toast.LENGTH_LONG).show()
            }
    }
    //UPDATE NEED HASHMAP
    fun assignMemberToBoard(activity:
                            MembersActivity,board: Board,user:User){
    val assignToHashMap = HashMap<String,Any>()
        assignToHashMap[Constants.ASSIGNED_TO] = board.assignedTo

        mFireStore.collection(Constants.BOARD)
            .document(board.documentId)
            .update(assignToHashMap)
            .addOnSuccessListener {
                activity.memberAssignSuccess(user)

            }
            .addOnFailureListener{
                    e->
                activity.hideProgressDialog()
                Log.i(activity.javaClass.simpleName,"Failed to assign mmb to board",e)
                Toast.makeText(activity,"Failed to assign member to board",Toast.LENGTH_LONG).show()
            }

    }
}