package com.example.goalmanagement;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

// Import AlertDialog (cho hàm ví dụ)
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment để hiển thị danh sách các mục tiêu học tập.
 * 1. ĐÃ IMPLEMENT INTERFACE (NHƯ TRONG ẢNH)
 */
public class GoalsProgressFragment extends Fragment implements OnGoalClickListener {

    // Khai báo View và Adapter
    RecyclerView rvGoalsList;
    GoalProgressAdapter goalAdapter;
    List<GoalItem> goalItems; // Danh sách dữ liệu mục tiêu (Dùng class GoalItem từ file riêng)
    TextView btnAddNewGoal;

    public GoalsProgressFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_goals_progress, container, false);

        // Ánh xạ View
        rvGoalsList = view.findViewById(R.id.rv_goals_list);
        btnAddNewGoal = view.findViewById(R.id.btn_add_new_goal);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Load dữ liệu mẫu
        loadGoalData();

        // 2. SỬA LẠI TẠO ADAPTER (NHƯ TRONG ẢNH)
        // Thiết lập RecyclerView và Adapter
        if (getContext() != null && goalItems != null) {
            // Khởi tạo Adapter, truyền 'this' làm listener
            goalAdapter = new GoalProgressAdapter(getContext(), goalItems, this);
            rvGoalsList.setLayoutManager(new LinearLayoutManager(getContext()));
            rvGoalsList.setAdapter(goalAdapter);
        }

        // Xử lý nút "+ Thêm mới"
        btnAddNewGoal.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreateGoalActivity.class);
            startActivity(intent);
        });
    }

    // Hàm lấy dữ liệu thật từ Room
    private void loadGoalData() {
        goalItems = new ArrayList<>();
        new Thread(() -> {
            com.example.goalmanagement.data.AppDatabase db = com.example.goalmanagement.data.AppDatabase.getInstance(requireContext().getApplicationContext());
            java.util.List<com.example.goalmanagement.data.Goal> goals = db.goalDao().getAll();
            java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
            long now = System.currentTimeMillis();
            for (com.example.goalmanagement.data.Goal g : goals) {
                int total = db.taskDao().countByGoal(g.id);
                int completed = db.taskDao().countByGoalAndStatus(g.id, "completed");
                int percentage = total > 0 ? (int) Math.round((completed * 100.0) / total) : 0;
                long daysLeft = Math.max(0, (g.deadlineAtMillis - now) / (24L * 60 * 60 * 1000));
                String daysLeftStr = daysLeft + " ngày còn lại";
                String deadlineStr = df.format(new java.util.Date(g.deadlineAtMillis));
                String progressDetail = completed + "/" + total + " phiên";
                String status = percentage >= 100 ? "Hoàn thành" : (percentage >= 70 ? "Sắp hoàn thành" : "Đang tiến hành");
                goalItems.add(new GoalItem("ic_book", g.title, deadlineStr, percentage, daysLeftStr, progressDetail, status));
            }
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (goalAdapter == null) {
                        goalAdapter = new GoalProgressAdapter(getContext(), goalItems, this);
                        rvGoalsList.setLayoutManager(new LinearLayoutManager(getContext()));
                        rvGoalsList.setAdapter(goalAdapter);
                    } else {
                        goalAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }

    // 3. THÊM HÀM onGoalClick ĐÃ IMPLEMENT (NHƯ TRONG ẢNH)
    @Override
    public void onGoalClick(GoalItem goal) {
        // Tạo và hiển thị BottomSheet khi item được click
        GoalDetailBottomSheetFragment bottomSheet = GoalDetailBottomSheetFragment.newInstance(goal);

        // Dùng getParentFragmentManager() trong Fragment
        bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
    }

    @Override
    public void onResume() {
        super.onResume();
        loadGoalData();
    }
}
