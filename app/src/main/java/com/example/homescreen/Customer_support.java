package com.example.homescreen;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Rect;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;

public class Customer_support extends AppCompatActivity {

    private LinearLayout faq1, faq2, faq3, faq4;
    private TextView faq1Answer, faq2Answer, faq3Answer, faq4Answer;
    private TextView faq1Question, faq2Question, faq3Question, faq4Question;
    private TextInputEditText nameEditText, emailEditText, subjectEditText, inquiryEditText, messageEditText;
    private Button submitButton;
    private ImageView backarrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_support);

        // Initialize the Views
        faq1 = findViewById(R.id.faq1);
        faq2 = findViewById(R.id.faq2);
        faq3 = findViewById(R.id.faq3);
        faq4 = findViewById(R.id.faq4);

        faq1Answer = findViewById(R.id.faq1_answer);
        faq2Answer = findViewById(R.id.faq2_answer);
        faq3Answer = findViewById(R.id.faq3_answer);
        faq4Answer = findViewById(R.id.faq4_answer);

        // Get the question TextViews from the LinearLayouts
        faq1Question = (TextView) faq1.getChildAt(0);
        faq2Question = (TextView) faq2.getChildAt(0);
        faq3Question = (TextView) faq3.getChildAt(0);
        faq4Question = (TextView) faq4.getChildAt(0);

        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        subjectEditText = findViewById(R.id.subjectEditText);
        inquiryEditText = findViewById(R.id.inquiryEditText);
        messageEditText = findViewById(R.id.messageEditText);
        backarrow=findViewById(R.id.back_arrow);

        submitButton = findViewById(R.id.submitButton);

        // FAQ Click Listeners with arrow icon toggle
        faq1.setOnClickListener(v -> toggleFAQ(faq1Answer, faq1Question));
        faq2.setOnClickListener(v -> toggleFAQ(faq2Answer, faq2Question));
        faq3.setOnClickListener(v -> toggleFAQ(faq3Answer, faq3Question));
        faq4.setOnClickListener(v -> toggleFAQ(faq4Answer, faq4Question));

        // Submit button click listener
        submitButton.setOnClickListener(v -> handleSubmit());

        backarrow.setOnClickListener(v -> {
            Intent intent = new Intent(Customer_support.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            finish();
        });


    }

    // Toggle FAQ answer visibility and arrow icon
    private void toggleFAQ(TextView answer, TextView question) {
        if (answer.getVisibility() == View.GONE) {
            answer.setVisibility(View.VISIBLE);
            question.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    ContextCompat.getDrawable(this, R.drawable.up_arrow), // Replace with your up arrow icon
                    null
            );
        } else {
            answer.setVisibility(View.GONE);
            question.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    ContextCompat.getDrawable(this, R.drawable.down_arrow), // Replace with your down arrow icon
                    null
            );
        }
    }

    // Handle form submission
    private void handleSubmit() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String subject = subjectEditText.getText().toString().trim();
        String inquiry = inquiryEditText.getText().toString().trim();
        String message = messageEditText.getText().toString().trim();

        // Validate the inputs
        if (name.isEmpty()) {
            nameEditText.setError("Name is required");
            return;
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Valid email is required");
            return;
        }

        if (subject.isEmpty()) {
            subjectEditText.setError("Subject is required");
            return;
        }

        if (message.isEmpty()) {
            messageEditText.setError("Message is required");
            return;
        }

        // Handle successful submission
        Toast.makeText(this, "Your message has been submitted!", Toast.LENGTH_SHORT).show();

        // Clear the input fields after submission
        nameEditText.setText("");
        emailEditText.setText("");
        subjectEditText.setText("");
        inquiryEditText.setText("");
        messageEditText.setText("");
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View view = getCurrentFocus();
        if (view instanceof EditText) {
            Rect outRect = new Rect();
            view.getGlobalVisibleRect(outRect);
            if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                view.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}
