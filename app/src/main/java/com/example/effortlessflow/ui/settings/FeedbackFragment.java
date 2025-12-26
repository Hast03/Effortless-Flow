package com.example.effortlessflow.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.effortlessflow.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class FeedbackFragment extends Fragment {

    private MaterialToolbar toolbar;
    private ChipGroup feedbackTypeChips;
    private TextInputLayout subjectLayout, messageLayout, emailLayout;
    private TextInputEditText subjectEditText, messageEditText, emailEditText;
    private MaterialButton sendButton, rateAppButton, shareAppButton;
    private Chip bugReportChip, featureRequestChip, generalChip;

    // Integrated Star Rating Views
    private MaterialCardView cardStarRating;
    private ImageView star1, star2, star3, star4, star5;
    private TextView ratingText;
    private MaterialButton submitRatingButton;
    private ImageView[] stars;
    private int currentRating = 0; // To keep track of the current rating

    // Integrated APK Generation Views
    private MaterialCardView cardApkGeneration;
    private ProgressBar progressBar;
    private TextView progressText;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_send_feedback, container, false);

        initViews(root);
        setupToolbar();
        setupClickListeners();
        setupChipSelection();
        setupStarRatingListeners();

        return root;
    }

    private void initViews(View root) {
        toolbar = root.findViewById(R.id.toolbar_feedback);
        feedbackTypeChips = root.findViewById(R.id.feedback_type_chips);
        subjectLayout = root.findViewById(R.id.feedback_subject_layout);
        messageLayout = root.findViewById(R.id.feedback_message_layout);
        emailLayout = root.findViewById(R.id.feedback_email_layout);
        subjectEditText = root.findViewById(R.id.feedback_subject_edit_text);
        messageEditText = root.findViewById(R.id.feedback_message_edit_text);
        emailEditText = root.findViewById(R.id.feedback_email_edit_text);
        sendButton = root.findViewById(R.id.button_send_feedback);
        rateAppButton = root.findViewById(R.id.btn_rate_app);
        shareAppButton = root.findViewById(R.id.btn_share_app);

        bugReportChip = root.findViewById(R.id.chip_bug_report);
        featureRequestChip = root.findViewById(R.id.chip_feature_request);
        generalChip = root.findViewById(R.id.chip_general);

        // Initialize integrated Star Rating Views
        cardStarRating = root.findViewById(R.id.card_star_rating);
        star1 = root.findViewById(R.id.star1);
        star2 = root.findViewById(R.id.star2);
        star3 = root.findViewById(R.id.star3);
        star4 = root.findViewById(R.id.star4);
        star5 = root.findViewById(R.id.star5);
        ratingText = root.findViewById(R.id.rating_text);
        submitRatingButton = root.findViewById(R.id.btn_submit_rating);
        stars = new ImageView[]{star1, star2, star3, star4, star5};

        // Initialize integrated APK Generation Views
        cardApkGeneration = root.findViewById(R.id.card_apk_generation);
        progressBar = root.findViewById(R.id.progress_bar);
        progressText = root.findViewById(R.id.progress_text);
    }

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });
    }

    private void setupClickListeners() {
        // Send feedback button
        sendButton.setOnClickListener(v -> sendFeedback());

        // Rate app button - Show the star rating card
        rateAppButton.setOnClickListener(v -> {
            cardStarRating.setVisibility(View.VISIBLE);
            cardApkGeneration.setVisibility(View.GONE); // Hide APK generation if visible
            resetStarRating(); // Reset stars when showing
        });

        // Share app button - Show the APK generation card
        shareAppButton.setOnClickListener(v -> {
            cardApkGeneration.setVisibility(View.VISIBLE);
            cardStarRating.setVisibility(View.GONE); // Hide star rating if visible
            generateAndShareAPK(); // Start APK generation process
        });
    }

    private void setupChipSelection() {
        // Set up individual chip click listeners
        bugReportChip.setOnClickListener(v -> {
            feedbackTypeChips.clearCheck();
            bugReportChip.setChecked(true);
            updateChipStyles(bugReportChip);
        });

        featureRequestChip.setOnClickListener(v -> {
            feedbackTypeChips.clearCheck();
            featureRequestChip.setChecked(true);
            updateChipStyles(featureRequestChip);
        });

        generalChip.setOnClickListener(v -> {
            feedbackTypeChips.clearCheck();
            generalChip.setChecked(true);
            updateChipStyles(generalChip);
        });

        // Set up ChipGroup listener with correct signature
        feedbackTypeChips.setOnCheckedChangeListener((group, checkedId) -> {
            // Find the checked chip by ID
            Chip checkedChip = null;
            if (checkedId == R.id.chip_bug_report) {
                checkedChip = bugReportChip;
            } else if (checkedId == R.id.chip_feature_request) {
                checkedChip = featureRequestChip;
            } else if (checkedId == R.id.chip_general) {
                checkedChip = generalChip;
            }

            // Update the chip styles
            updateChipStyles(checkedChip);
        });
    }

    private void updateChipStyles(Chip selectedChip) {
        // Reset all chips to default style
        bugReportChip.setChipBackgroundColorResource(R.color.light_gray_background);
        bugReportChip.setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_charcoal_text));

        featureRequestChip.setChipBackgroundColorResource(R.color.light_gray_background);
        featureRequestChip.setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_charcoal_text));

        generalChip.setChipBackgroundColorResource(R.color.light_gray_background);
        generalChip.setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_charcoal_text));

        // Apply selected style to the checked chip
        if (selectedChip != null) {
            selectedChip.setChipBackgroundColorResource(R.color.aqua_accent);
            selectedChip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        }
    }

    private void setupStarRatingListeners() {
        String[] ratingTexts = {"Very Bad", "Bad", "Good", "Very Good", "Excellent"};

        // Set up star click listeners
        for (int i = 0; i < stars.length; i++) {
            final int rating = i + 1;
            stars[i].setOnClickListener(v -> {
                currentRating = rating;
                updateStars(stars, currentRating);
                ratingText.setText(ratingTexts[currentRating - 1]);
                ratingText.setVisibility(View.VISIBLE);
            });
        }

        submitRatingButton.setOnClickListener(v -> {
            if (currentRating > 0) {
                Toast.makeText(getContext(),
                        "Thank you for rating: " + ratingTexts[currentRating - 1] +
                                " (" + currentRating + "/5 stars)",
                        Toast.LENGTH_LONG).show();
                cardStarRating.setVisibility(View.GONE); // Hide the rating card after submission
                resetStarRating(); // Reset the stars and text
            } else {
                Toast.makeText(getContext(), "Please select a rating", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetStarRating() {
        currentRating = 0; // Reset the stored rating
        updateStars(stars, currentRating); // Update UI to show all outline stars
        ratingText.setVisibility(View.GONE); // Hide the rating text
        ratingText.setText(""); // Clear text
    }

    private void sendFeedback() {
        // Get form values
        String subject = subjectEditText.getText().toString().trim();
        String message = messageEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();

        // Validation
        if (subject.isEmpty()) {
            subjectLayout.setError("Subject is required");
            subjectEditText.requestFocus();
            return;
        } else {
            subjectLayout.setError(null);
        }

        if (message.isEmpty()) {
            messageLayout.setError("Message is required");
            messageEditText.requestFocus();
            return;
        } else {
            messageLayout.setError(null);
        }

        // Get selected feedback type
        String feedbackType = "General";
        int selectedChipId = feedbackTypeChips.getCheckedChipId();
        if (selectedChipId == R.id.chip_bug_report) {
            feedbackType = "Bug Report";
        } else if (selectedChipId == R.id.chip_feature_request) {
            feedbackType = "Feature Request";
        }
        // Build email content
        String emailContent = buildEmailContent(feedbackType, subject, message, email);
        // Send email
        sendEmail(feedbackType, subject, emailContent);
    }

    private String buildEmailContent(String feedbackType, String subject, String message, String email) {
        StringBuilder content = new StringBuilder();
        content.append("Hi,\n\n");
        content.append("I have feedback about the Effortless Flow app:\n\n");
        content.append("Feedback Type: ").append(feedbackType).append("\n");
        content.append("App Version: 1.0.0\n");
        content.append("Device: ").append(android.os.Build.MODEL).append("\n");
        content.append("Android Version: ").append(android.os.Build.VERSION.RELEASE).append("\n\n");
        content.append("Subject: ").append(subject).append("\n\n");
        content.append("Feedback:\n").append(message).append("\n\n");

        if (!email.isEmpty()) {
            content.append("User Contact: ").append(email).append("\n\n");
        }

        content.append("---\nSent from Effortless Flow App");
        return content.toString();
    }

    private void sendEmail(String feedbackType, String subject, String emailContent) {
        try {
            // Create email intent
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"joshihast03@gmail.com"});

            // Set email subject
            // String emailSubject = "Effortless Flow Feedback - " + feedbackType + ": " + subject;
            String emailSubject = "Effortless Flow Feedback";
            // Add feedback type to the beginning of the subject if needed
            if (!feedbackType.equals("General")) {
                emailSubject = feedbackType + " - " + subject;
            }
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);

            // Set email body
            emailIntent.putExtra(Intent.EXTRA_TEXT, emailContent);
            emailIntent.putExtra(Intent.EXTRA_HTML_TEXT, emailContent.replace("\n", "<br>"));

            // Start email activity
            startActivity(Intent.createChooser(emailIntent, "Send Feedback via Email"));
            Toast.makeText(getContext(), "Opening email app...", Toast.LENGTH_SHORT).show();
            // Clear form after sending
            clearFormAfterSending();
        } catch (Exception e) {
            Log.e("FeedbackFragment", "Error sending email: " + e.getMessage());
            Toast.makeText(getContext(), "No email app found", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearFormAfterSending() {
        // Clear all input fields
        subjectEditText.setText("");
        messageEditText.setText("");
        emailEditText.setText("");

        // Reset chip selection
        feedbackTypeChips.clearCheck();
        generalChip.setChecked(true);

        // Show success message
        Toast.makeText(getContext(), "Thank you for your feedback!", Toast.LENGTH_SHORT).show();
    }

    private void updateStars(ImageView[] stars, int rating) {
        for (int i = 0; i < stars.length; i++) {
            if (i < rating) {
                stars[i].setImageResource(R.drawable.ic_star_filled);
                stars[i].setColorFilter(ContextCompat.getColor(requireContext(), R.color.aqua_accent_dark));
            } else {
                stars[i].setImageResource(R.drawable.ic_star_outline);
                stars[i].setColorFilter(ContextCompat.getColor(requireContext(), R.color.medium_gray_text));
            }
        }
    }

    private void generateAndShareAPK() {
        // Reset progress bar and text
        progressBar.setProgress(0);
        progressText.setText("Starting APK generation...");

        // Simulate APK generation process using a Handler
        android.os.Handler handler = new android.os.Handler();

        handler.postDelayed(() -> {
            progressText.setText("Preparing APK file...");
        }, 1000);

        handler.postDelayed(() -> {
            progressText.setText("Generating APK...");
            progressBar.setProgress(50);
        }, 2000);

        handler.postDelayed(() -> {
            progressText.setText("APK ready for sharing!");
            progressBar.setProgress(100);
        }, 3000);

        handler.postDelayed(() -> {
            cardApkGeneration.setVisibility(View.GONE); // Hide the card after process
            shareGeneratedAPK();
        }, 4000);
    }

    private void shareGeneratedAPK() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("*/*");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Effortless Flow - Task Manager App");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                "Check out this amazing task management app with AI-powered features!\n\n" +
                        "Effortless Flow helps you:\n" +
                        "• Organize tasks efficiently\n" +
                        "• Get AI suggestions for subtasks\n" +
                        "• Track productivity insights\n" +
                        "• Set smart reminders\n\n" +
                        "Install the attached APK to try it out!");

        // For demo purposes - in real implementation, you'd attach actual APK file
        // shareIntent.putExtra(Intent.EXTRA_STREAM, apkUri);

        try {
            startActivity(Intent.createChooser(shareIntent, "Share Effortless Flow APK"));
            Toast.makeText(getContext(), "APK ready to share!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Unable to share APK", Toast.LENGTH_SHORT).show();
        }
    }
}