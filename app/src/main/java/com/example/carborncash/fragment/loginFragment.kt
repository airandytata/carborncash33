package com.example.carborncash.fragment

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.ServiceWorkerClient
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.example.carborncash.R
import com.example.carborncash.databinding.FragmentLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

/**
 * A simple [Fragment] subclass.
 * Use the [loginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class loginFragment() : Fragment(), Parcelable {
    // TODO: Rename and change types of parameters

    private var _binding : FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var client: GoogleSignInClient

    private lateinit var database : DatabaseReference

    constructor(parcel: Parcel) : this() {

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        binding.Logingoogle.setOnClickListener(){
            val intent = client.signInIntent
            startActivityForResult(intent, 10001)
        }

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val options =GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        client = GoogleSignIn.getClient(requireActivity(), options)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<loginFragment> {
        override fun createFromParcel(parcel: Parcel): loginFragment {
            return loginFragment(parcel)
        }

        override fun newArray(size: Int): Array<loginFragment?> {
            return arrayOfNulls(size)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==10001){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken,null)



            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val useremail: String = account.email.toString().replace('.', '_')

                        database = FirebaseDatabase.getInstance().getReference("Users")
                        database.child(useremail).addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    // 이메일이 이미 존재하므로 메인 페이지로 이동
                                    findNavController().navigate(R.id.action_loginFragment_to_mainFragment)
                                } else {
                                    // 이메일이 존재하지 않으므로 새로운 사용자를 생성
                                    val user = User(useremail, 0, 0, 0)
                                    database.child(useremail).setValue(user).addOnSuccessListener {
                                        findNavController().navigate(R.id.action_loginFragment_to_mainFragment)
                                    }.addOnFailureListener {
                                        Toast.makeText(
                                            requireActivity(),
                                            "Failed Email Saved",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // 에러 처리
                            }
                        })

                    }
                }
        }
    }



    override fun onStart() {
        super.onStart()
        if(FirebaseAuth.getInstance().currentUser != null){
            findNavController().navigate(
                R.id.action_loginFragment_to_mainFragment
            )
        }
    }


}